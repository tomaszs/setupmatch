package com.setupmatch.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface AllocationRequestRepository : JpaRepository<AllocationRequestEntity, UUID> {

    @Query(
        """
        SELECT a FROM AllocationRequestEntity a
        LEFT JOIN FETCH a.lines l
        LEFT JOIN FETCH l.equipment
        WHERE a.id = :id
        """,
    )
    fun findByIdWithLines(@Param("id") id: UUID): AllocationRequestEntity?

    @Query(
        """
        SELECT DISTINCT a FROM AllocationRequestEntity a
        LEFT JOIN FETCH a.lines
        WHERE (:state IS NULL OR a.state = :state)
        AND (:employeeId IS NULL OR a.employeeId = :employeeId)
        ORDER BY a.createdAt DESC
        """,
    )
    fun findAllFiltered(
        @Param("state") state: AllocationState?,
        @Param("employeeId") employeeId: String?,
    ): List<AllocationRequestEntity>
}
