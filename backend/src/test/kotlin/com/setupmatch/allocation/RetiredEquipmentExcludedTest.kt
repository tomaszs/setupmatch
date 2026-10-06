package com.setupmatch.allocation

import com.setupmatch.domain.EquipmentType
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class RetiredEquipmentExcludedTest {

    @Test
    fun `allocator only receives available candidates from service layer`() {
        val slots = listOf(PolicySlot(0, EquipmentType.mouse))
        val available = EquipmentCandidate(
            UUID.randomUUID(),
            EquipmentType.mouse,
            "Logitech",
            "MX",
            BigDecimal.valueOf(0.7),
            LocalDate.of(2023, 1, 1),
        )

        val outcome = HungarianAllocator.allocate(slots, listOf(available))
        assertTrue(outcome is AllocationOutcome.Success)
    }
}
