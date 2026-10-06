package com.setupmatch.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "allocation_line")
class AllocationLineEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocation_request_id", nullable = false)
    var allocationRequest: AllocationRequestEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipment_id", nullable = false)
    var equipment: EquipmentEntity,

    @Column(name = "policy_slot_index", nullable = false)
    var policySlotIndex: Int,
)
