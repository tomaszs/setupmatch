package com.setupmatch.domain

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface EquipmentRepository : JpaRepository<EquipmentEntity, UUID> {

    @Query(
        """
        SELECT e FROM EquipmentEntity e
        WHERE (:state IS NULL OR e.state = :state)
        AND (:type IS NULL OR e.type = :type)
        AND (:includeRetired = true OR e.state != com.setupmatch.domain.EquipmentState.retired)
        ORDER BY e.createdAt ASC
        """,
    )
    fun findAllFiltered(
        @Param("state") state: EquipmentState?,
        @Param("type") type: EquipmentType?,
        @Param("includeRetired") includeRetired: Boolean,
    ): List<EquipmentEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EquipmentEntity e WHERE e.id = :id")
    fun findByIdForUpdate(@Param("id") id: UUID): EquipmentEntity?
}
