package com.setupmatch.allocation

import com.setupmatch.domain.EquipmentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class CompetingMonitorsTest {

    @Test
    fun `optimal matching satisfies both monitor slots`() {
        val monitorHigh = candidate("22222222-2222-2222-2222-222222220002", 0.85, LocalDate.of(2024, 6, 1))
        val monitorMid = candidate("22222222-2222-2222-2222-222222220003", 0.75, LocalDate.of(2024, 3, 1))
        val monitorLow = candidate("22222222-2222-2222-2222-222222220004", 0.70, LocalDate.of(2024, 1, 1))

        val slots = listOf(
            PolicySlot(0, EquipmentType.monitor, minCondition = 0.8),
            PolicySlot(1, EquipmentType.monitor),
        )

        val outcome = HungarianAllocator.allocate(slots, listOf(monitorHigh, monitorMid, monitorLow))
        assertTrue(outcome is AllocationOutcome.Success)
        val success = outcome as AllocationOutcome.Success
        val bySlot = success.assignments.associateBy { it.slotIndex }

        assertEquals(monitorHigh.id, bySlot[0]?.equipmentId)
        assertEquals(monitorMid.id, bySlot[1]?.equipmentId)
    }

    private fun candidate(id: String, score: Double, purchaseDate: LocalDate) = EquipmentCandidate(
        id = UUID.fromString(id),
        type = EquipmentType.monitor,
        brand = "LG",
        model = "Test",
        conditionScore = BigDecimal.valueOf(score),
        purchaseDate = purchaseDate,
    )
}
