package com.setupmatch.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "equipment")
class EquipmentEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: EquipmentType,

    @Column(nullable = false)
    var brand: String,

    @Column(nullable = false)
    var model: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: EquipmentState = EquipmentState.available,

    @Column(name = "condition_score", nullable = false)
    var conditionScore: BigDecimal,

    @Column(name = "purchase_date", nullable = false)
    var purchaseDate: LocalDate,

    @Column(name = "retire_reason")
    var retireReason: String? = null,

    @Column(name = "retired_at")
    var retiredAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)
