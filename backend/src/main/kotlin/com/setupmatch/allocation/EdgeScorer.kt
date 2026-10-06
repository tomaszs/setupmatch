package com.setupmatch.allocation

import com.setupmatch.domain.EquipmentType
import java.math.BigDecimal
import java.time.LocalDate

object EdgeScorer {

    private const val INCOMPATIBLE = -1_000_000.0

    fun computeWeight(slot: PolicySlot, candidate: EquipmentCandidate, recencyRank: Int, recencyPoolSize: Int): Double {
        if (!passesHardConstraints(slot, candidate)) {
            return INCOMPATIBLE
        }

        val base = candidate.conditionScore.toDouble() * 100.0
        val brandBonus = if (
            !slot.preferredBrand.isNullOrBlank() &&
            candidate.brand.equals(slot.preferredBrand.trim(), ignoreCase = true)
        ) {
            50.0
        } else {
            0.0
        }
        val recencyBonus = if (recencyPoolSize <= 1) {
            0.0
        } else {
            25.0 * recencyRank.toDouble() / (recencyPoolSize - 1).toDouble()
        }
        val tieBreak = candidate.id.toString().hashCode().toDouble() * 1e-9

        return base + brandBonus + recencyBonus + tieBreak
    }

    fun passesHardConstraints(slot: PolicySlot, candidate: EquipmentCandidate): Boolean {
        if (candidate.type != slot.type) {
            return false
        }

        val minCondition = slot.minCondition
        if (minCondition != null && candidate.conditionScore.toDouble() < minCondition) {
            return false
        }

        return true
    }

    fun recencyRanks(candidates: List<EquipmentCandidate>, type: EquipmentType): Map<java.util.UUID, Int> {
        val ofType = candidates.filter { it.type == type }.sortedBy { it.purchaseDate }
        return ofType.mapIndexed { index, candidate -> candidate.id to index }.toMap()
    }

    fun totalAssignmentWeight(
        slots: List<PolicySlot>,
        candidates: List<EquipmentCandidate>,
        assignments: List<SlotAssignment>,
    ): Double {
        val slotByIndex = slots.associateBy { it.index }
        val candidateById = candidates.associateBy { it.id }

        return assignments.sumOf { assignment ->
            val slot = slotByIndex[assignment.slotIndex]
                ?: throw IllegalArgumentException("Unknown slot index ${assignment.slotIndex}")
            val candidate = candidateById[assignment.equipmentId]
                ?: throw IllegalArgumentException("Unknown equipment ${assignment.equipmentId}")
            val ranks = recencyRanks(candidates, slot.type)
            val rank = ranks[candidate.id] ?: 0
            computeWeight(slot, candidate, rank, ranks.size)
        }
    }

    fun buildFailureReason(slots: List<PolicySlot>, candidates: List<EquipmentCandidate>): String {
        val missingTypes = slots.map { it.type }.distinct().filter { type ->
            candidates.none { it.type == type }
        }
        if (missingTypes.isNotEmpty()) {
            return "No available equipment of type ${missingTypes.first()}"
        }

        for (slot in slots) {
            val eligible = candidates.filter { passesHardConstraints(slot, it) }
            if (eligible.isEmpty()) {
                val min = slot.minCondition
                if (min != null) {
                    return "Cannot satisfy minimum condition $min for type ${slot.type} (no eligible units)"
                }

                return "No available equipment of type ${slot.type}"
            }
        }

        val typeCounts = slots.groupingBy { it.type }.eachCount()
        for ((type, needed) in typeCounts) {
            val eligibleCount = candidates.count { it.type == type && slots.any { slot -> passesHardConstraints(slot, it) } }
            if (eligibleCount < needed) {
                return "Not enough distinct equipment to fulfill policy (need $needed $type, found $eligibleCount eligible)"
            }
        }

        return "No valid assignment exists for this policy (competing constraints)"
    }
}
