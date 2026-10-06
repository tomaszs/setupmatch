package com.setupmatch.allocation

import com.setupmatch.domain.EquipmentType
import java.math.BigDecimal

object PolicySlotFilters {

    /**
     * Lowest inclusive condition floor for SQL fetch per type.
     * Returns null when any slot of that type has no min_condition (fetch all available of the type).
     */
    fun minConditionFloor(slots: List<PolicySlot>): BigDecimal? {
        if (slots.any { it.minCondition == null }) {
            return null
        }

        return BigDecimal.valueOf(slots.minOf { it.minCondition!! })
    }

    fun floorsByType(slots: List<PolicySlot>): Map<EquipmentType, BigDecimal?> =
        slots.groupBy { it.type }.mapValues { (_, typeSlots) -> minConditionFloor(typeSlots) }
}
