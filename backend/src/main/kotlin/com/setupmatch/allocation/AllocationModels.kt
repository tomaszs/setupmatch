package com.setupmatch.allocation

import com.setupmatch.domain.EquipmentType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class PolicySlot(
    val index: Int,
    val type: EquipmentType,
    val minCondition: Double? = null,
    val preferredBrand: String? = null,
)

data class EquipmentCandidate(
    val id: UUID,
    val type: EquipmentType,
    val brand: String,
    val model: String,
    val conditionScore: BigDecimal,
    val purchaseDate: LocalDate,
)

sealed class AllocationOutcome {
    data class Success(val assignments: List<SlotAssignment>) : AllocationOutcome()

    data class Failure(val reason: String) : AllocationOutcome()
}

data class SlotAssignment(
    val slotIndex: Int,
    val equipmentId: UUID,
)
