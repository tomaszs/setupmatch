package com.setupmatch.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "allocation_request")
class AllocationRequestEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "employee_id", nullable = false)
    var employeeId: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var policy: List<PolicySlotJson>,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: AllocationState,

    @Column(name = "failure_reason")
    var failureReason: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "allocationRequest", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val lines: MutableList<AllocationLineEntity> = mutableListOf(),
)

data class PolicySlotJson(
    val type: EquipmentType,
    val min_condition: Double? = null,
    val preferred_brand: String? = null,
)
