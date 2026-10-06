package com.setupmatch.api

import com.setupmatch.domain.AllocationLineEntity
import com.setupmatch.domain.AllocationRequestEntity
import com.setupmatch.domain.AllocationState
import com.setupmatch.domain.EquipmentEntity
import com.setupmatch.domain.PolicySlotJson

fun EquipmentEntity.toResponse(): EquipmentResponse = EquipmentResponse(
    id = id,
    type = type,
    brand = brand,
    model = model,
    state = state,
    condition_score = conditionScore,
    purchase_date = purchaseDate,
    retire_reason = retireReason,
    retired_at = retiredAt,
)

fun AllocationRequestEntity.toDetailResponse(lines: List<AllocationLineEntity>): AllocationDetailResponse {
    return AllocationDetailResponse(
        id = id,
        employee_id = employeeId,
        state = state,
        failure_reason = failureReason,
        policy = policy,
        allocated_equipments = lines.map { line ->
            AllocatedEquipmentResponse(
                policy_slot_index = line.policySlotIndex,
                equipment = line.equipment.toResponse(),
            )
        },
        created_at = createdAt,
    )
}

fun AllocationRequestEntity.toSummaryResponse(): AllocationSummaryResponse = AllocationSummaryResponse(
    id = id,
    employee_id = employeeId,
    state = state,
    item_count = lines.size,
    created_at = createdAt,
)

fun List<PolicySlotJson>.toPolicySlots(): List<com.setupmatch.allocation.PolicySlot> =
    mapIndexed { index, slot ->
        com.setupmatch.allocation.PolicySlot(
            index = index,
            type = slot.type,
            minCondition = slot.min_condition,
            preferredBrand = slot.preferred_brand?.trim()?.takeIf { it.isNotEmpty() },
        )
    }

fun AllocationState.isTerminal(): Boolean =
    this == AllocationState.failed || this == AllocationState.confirmed || this == AllocationState.cancelled
