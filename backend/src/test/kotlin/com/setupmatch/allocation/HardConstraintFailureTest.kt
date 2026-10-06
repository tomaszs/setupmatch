package com.setupmatch.allocation

import com.setupmatch.domain.EquipmentType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class HardConstraintFailureTest {

    @Test
    fun `fails when no equipment meets minimum condition`() {
        val slots = listOf(PolicySlot(0, EquipmentType.monitor, minCondition = 0.95))
        val candidates = listOf(
            EquipmentCandidate(
                UUID.randomUUID(),
                EquipmentType.monitor,
                "LG",
                "27",
                BigDecimal.valueOf(0.80),
                LocalDate.of(2024, 1, 1),
            ),
        )

        val outcome = HungarianAllocator.allocate(slots, candidates)
        assertTrue(outcome is AllocationOutcome.Failure)
    }
}
