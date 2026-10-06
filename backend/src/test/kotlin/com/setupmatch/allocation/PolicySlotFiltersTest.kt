package com.setupmatch.allocation

import com.setupmatch.domain.EquipmentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PolicySlotFiltersTest {

    @Test
    fun `min condition floor uses lowest min when all slots specify one`() {
        val floor = PolicySlotFilters.minConditionFloor(
            listOf(
                PolicySlot(0, EquipmentType.monitor, minCondition = 0.8),
                PolicySlot(1, EquipmentType.monitor, minCondition = 0.7),
            ),
        )

        assertEquals(BigDecimal.valueOf(0.7), floor)
    }

    @Test
    fun `min condition floor is null when any slot has no min`() {
        val floor = PolicySlotFilters.minConditionFloor(
            listOf(
                PolicySlot(0, EquipmentType.monitor, minCondition = 0.8),
                PolicySlot(1, EquipmentType.monitor),
            ),
        )

        assertNull(floor)
    }
}
