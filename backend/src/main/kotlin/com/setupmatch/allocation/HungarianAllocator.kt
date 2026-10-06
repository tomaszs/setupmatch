package com.setupmatch.allocation

import com.setupmatch.domain.EquipmentType

/**
 * Maximum-weight bipartite matching per equipment type (Hungarian algorithm).
 * Slots only compete within the same type, so type groups can run in parallel.
 */
object HungarianAllocator {

    private const val INCOMPATIBLE = -1_000_000.0

    fun allocate(slots: List<PolicySlot>, candidates: List<EquipmentCandidate>): AllocationOutcome {
        if (slots.isEmpty()) {
            return AllocationOutcome.Failure("Policy must contain at least one slot")
        }

        if (slots.size > candidates.size) {
            return AllocationOutcome.Failure(EdgeScorer.buildFailureReason(slots, candidates))
        }

        val candidatesByType = candidates.groupBy { it.type }
        val slotsByType = slots.groupBy { it.type }

        val outcomes = slotsByType.entries.parallelStream().map { (type, typeSlots) ->
            allocateTypeGroup(type, typeSlots, candidatesByType)
        }.toList()

        val failure = outcomes.firstOrNull { it is AllocationOutcome.Failure }
        if (failure != null) {
            return failure
        }

        val assignments = outcomes
            .filterIsInstance<AllocationOutcome.Success>()
            .flatMap { it.assignments }

        if (assignments.size != slots.size) {
            return AllocationOutcome.Failure(EdgeScorer.buildFailureReason(slots, candidates))
        }

        return AllocationOutcome.Success(assignments)
    }

    private fun allocateTypeGroup(
        type: EquipmentType,
        typeSlots: List<PolicySlot>,
        candidatesByType: Map<EquipmentType, List<EquipmentCandidate>>,
    ): AllocationOutcome {
        val typeCandidates = candidatesByType[type] ?: emptyList()
        return allocateGroup(typeSlots, typeCandidates)
    }

    internal fun allocateGroup(slots: List<PolicySlot>, candidates: List<EquipmentCandidate>): AllocationOutcome {
        if (slots.isEmpty()) {
            return AllocationOutcome.Success(emptyList())
        }

        if (slots.size > candidates.size) {
            return AllocationOutcome.Failure(EdgeScorer.buildFailureReason(slots, candidates))
        }

        val type = slots.first().type
        val typeCandidates = candidates.filter { it.type == type }
        val recencyRanks = EdgeScorer.recencyRanks(typeCandidates, type)
        val poolSize = recencyRanks.size

        val weights = Array(slots.size) { row ->
            val slot = slots[row]
            DoubleArray(typeCandidates.size) { col ->
                val candidate = typeCandidates[col]
                val rank = recencyRanks[candidate.id] ?: 0
                EdgeScorer.computeWeight(slot, candidate, rank, poolSize)
            }
        }

        val columnForRow = HungarianMatcher.maxWeightAssignment(weights)
        val assignments = mutableListOf<SlotAssignment>()

        for (row in slots.indices) {
            val col = columnForRow[row]
            if (col < 0 || col >= typeCandidates.size) {
                return AllocationOutcome.Failure(EdgeScorer.buildFailureReason(slots, candidates))
            }

            val edgeWeight = weights[row][col]
            if (edgeWeight <= INCOMPATIBLE + 1) {
                return AllocationOutcome.Failure(EdgeScorer.buildFailureReason(slots, candidates))
            }

            assignments.add(SlotAssignment(slotIndex = slots[row].index, equipmentId = typeCandidates[col].id))
        }

        return AllocationOutcome.Success(assignments)
    }
}
