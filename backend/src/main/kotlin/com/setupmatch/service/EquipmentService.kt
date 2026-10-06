package com.setupmatch.service

import com.setupmatch.api.CreateEquipmentRequest
import com.setupmatch.api.EquipmentResponse
import com.setupmatch.api.RetireEquipmentRequest
import com.setupmatch.api.toResponse
import com.setupmatch.domain.EquipmentEntity
import com.setupmatch.domain.EquipmentRepository
import com.setupmatch.domain.EquipmentState
import com.setupmatch.domain.EquipmentType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@Service
class EquipmentService(
    private val equipmentRepository: EquipmentRepository,
) {

    @Transactional
    fun create(request: CreateEquipmentRequest): EquipmentResponse {
        val entity = EquipmentEntity(
            type = request.type,
            brand = request.brand.trim(),
            model = request.model.trim(),
            conditionScore = request.conditionScore,
            purchaseDate = request.purchaseDate,
            state = EquipmentState.available,
        )

        return equipmentRepository.save(entity).toResponse()
    }

    @Transactional(readOnly = true)
    fun list(state: EquipmentState?, type: EquipmentType?, includeRetired: Boolean): List<EquipmentResponse> {
        return equipmentRepository.findAllFiltered(state, type, includeRetired)
            .map { it.toResponse() }
    }

    @Transactional
    fun retire(id: UUID, request: RetireEquipmentRequest): EquipmentResponse {
        val equipment = equipmentRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found")
        }

        when (equipment.state) {
            EquipmentState.retired -> throw ResponseStatusException(HttpStatus.CONFLICT, "Equipment is already retired")
            EquipmentState.reserved -> throw ResponseStatusException(HttpStatus.CONFLICT, "Cannot retire reserved equipment")
            EquipmentState.assigned -> throw ResponseStatusException(HttpStatus.CONFLICT, "Cannot retire assigned equipment")
            EquipmentState.available -> {
                equipment.state = EquipmentState.retired
                equipment.retireReason = request.reason.trim()
                equipment.retiredAt = Instant.now()
            }
        }

        return equipmentRepository.save(equipment).toResponse()
    }
}
