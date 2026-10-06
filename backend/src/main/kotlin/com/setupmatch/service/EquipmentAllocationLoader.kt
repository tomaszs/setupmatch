package com.setupmatch.service

import com.setupmatch.allocation.PolicySlot
import com.setupmatch.allocation.PolicySlotFilters
import com.setupmatch.domain.EquipmentEntity
import com.setupmatch.domain.EquipmentState
import com.setupmatch.domain.EquipmentType
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import jakarta.persistence.criteria.Predicate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class EquipmentAllocationLoader(
    private val entityManager: EntityManager,
) {

    fun loadAndLockAvailable(slots: List<PolicySlot>): List<EquipmentEntity> {
        if (slots.isEmpty()) {
            return emptyList()
        }

        val floorsByType = PolicySlotFilters.floorsByType(slots)
        val criteriaBuilder = entityManager.criteriaBuilder
        val query = criteriaBuilder.createQuery(EquipmentEntity::class.java)
        val root = query.from(EquipmentEntity::class.java)

        val typePredicates = floorsByType.map { (type, minCondition) ->
            typePredicate(criteriaBuilder, root, type, minCondition)
        }

        query.select(root).where(
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get<EquipmentState>("state"), EquipmentState.available),
                criteriaBuilder.or(*typePredicates.toTypedArray()),
            ),
        ).orderBy(criteriaBuilder.asc(root.get<UUID>("id")))

        return entityManager.createQuery(query)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .resultList
    }

    private fun typePredicate(
        criteriaBuilder: jakarta.persistence.criteria.CriteriaBuilder,
        root: jakarta.persistence.criteria.Root<EquipmentEntity>,
        type: EquipmentType,
        minCondition: BigDecimal?,
    ): Predicate {
        val typeMatch = criteriaBuilder.equal(root.get<EquipmentType>("type"), type)
        if (minCondition == null) {
            return typeMatch
        }

        return criteriaBuilder.and(
            typeMatch,
            criteriaBuilder.greaterThanOrEqualTo(root.get<BigDecimal>("conditionScore"), minCondition),
        )
    }
}
