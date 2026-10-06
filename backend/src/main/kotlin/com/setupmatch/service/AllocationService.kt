package com.setupmatch.service

import com.setupmatch.allocation.AllocationOutcome
import com.setupmatch.allocation.HungarianAllocator
import com.setupmatch.allocation.EquipmentCandidate
import com.setupmatch.api.AllocationDetailResponse
import com.setupmatch.api.AllocationSummaryResponse
import com.setupmatch.api.CreateAllocationRequest
import com.setupmatch.api.toDetailResponse
import com.setupmatch.api.toPolicySlots
import com.setupmatch.api.toSummaryResponse
import com.setupmatch.domain.AllocationLineEntity
import com.setupmatch.domain.AllocationRequestEntity
import com.setupmatch.domain.AllocationRequestRepository
import com.setupmatch.domain.AllocationState
import com.setupmatch.domain.EquipmentState
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@Service
class AllocationService(
    private val allocationRequestRepository: AllocationRequestRepository,
    private val equipmentAllocationLoader: EquipmentAllocationLoader,
) {

    @Transactional
    fun create(request: CreateAllocationRequest): AllocationDetailResponse {
        val policySlots = request.policy.toPolicySlots()

        val lockedEquipment = equipmentAllocationLoader.loadAndLockAvailable(policySlots)
        val candidates = lockedEquipment.map { equipment ->
            EquipmentCandidate(
                id = equipment.id,
                type = equipment.type,
                brand = equipment.brand,
                model = equipment.model,
                conditionScore = equipment.conditionScore,
                purchaseDate = equipment.purchaseDate,
            )
        }

        val outcome = HungarianAllocator.allocate(policySlots, candidates)
        val now = Instant.now()

        val entity = AllocationRequestEntity(
            employeeId = request.employeeId.trim(),
            policy = request.policy,
            state = AllocationState.failed,
            failureReason = null,
            updatedAt = now,
        )

        when (outcome) {
            is AllocationOutcome.Failure -> {
                entity.state = AllocationState.failed
                entity.failureReason = outcome.reason
            }

            is AllocationOutcome.Success -> {
                val equipmentById = lockedEquipment.associateBy { it.id }
                val sortedAssignments = outcome.assignments.sortedBy { it.equipmentId }

                for (assignment in sortedAssignments) {
                    val equipment = equipmentById[assignment.equipmentId]
                        ?: throw IllegalStateException("Assigned equipment not found")
                    equipment.state = EquipmentState.reserved
                    entity.lines.add(
                        AllocationLineEntity(
                            allocationRequest = entity,
                            equipment = equipment,
                            policySlotIndex = assignment.slotIndex,
                        ),
                    )
                }
                entity.state = AllocationState.allocated
            }
        }

        val saved = allocationRequestRepository.save(entity)
        val withLines = allocationRequestRepository.findByIdWithLines(saved.id)!!

        return withLines.toDetailResponse(withLines.lines)
    }

    @Transactional(readOnly = true)
    fun list(state: AllocationState?, employeeId: String?): List<AllocationSummaryResponse> {
        return allocationRequestRepository.findAllFiltered(state, employeeId?.trim())
            .map { it.toSummaryResponse() }
    }

    @Transactional(readOnly = true)
    fun get(id: UUID): AllocationDetailResponse {
        val entity = allocationRequestRepository.findByIdWithLines(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Allocation not found")

        return entity.toDetailResponse(entity.lines)
    }

    @Transactional
    fun confirm(id: UUID): AllocationDetailResponse {
        val entity = loadForUpdate(id)
        if (entity.state != AllocationState.allocated) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Allocation cannot be confirmed in state ${entity.state}")
        }

        for (line in entity.lines) {
            if (line.equipment.state != EquipmentState.reserved) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Equipment is not reserved")
            }
            line.equipment.state = EquipmentState.assigned
        }
        entity.state = AllocationState.confirmed
        entity.updatedAt = Instant.now()

        return allocationRequestRepository.save(entity).let {
            allocationRequestRepository.findByIdWithLines(it.id)!!.toDetailResponse(it.lines)
        }
    }

    @Transactional
    fun cancel(id: UUID): AllocationDetailResponse {
        val entity = loadForUpdate(id)
        if (entity.state != AllocationState.allocated) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Allocation cannot be cancelled in state ${entity.state}")
        }

        for (line in entity.lines) {
            line.equipment.state = EquipmentState.available
        }
        entity.state = AllocationState.cancelled
        entity.updatedAt = Instant.now()

        return allocationRequestRepository.save(entity).let {
            allocationRequestRepository.findByIdWithLines(it.id)!!.toDetailResponse(it.lines)
        }
    }

    private fun loadForUpdate(id: UUID): AllocationRequestEntity {
        return allocationRequestRepository.findByIdWithLines(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Allocation not found")
    }
}
