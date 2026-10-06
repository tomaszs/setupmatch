package com.setupmatch.api

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.setupmatch.domain.AllocationState
import com.setupmatch.domain.EquipmentState
import com.setupmatch.domain.EquipmentType
import com.setupmatch.domain.PolicySlotJson
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateEquipmentRequest(
    @field:NotNull val type: EquipmentType,
    @field:NotBlank @field:Size(max = 128) val brand: String,
    @field:NotBlank @field:Size(max = 128) val model: String,
    @field:NotNull
    @field:DecimalMin("0.0")
    @field:DecimalMax("1.0")
    val conditionScore: BigDecimal,
    @field:NotNull val purchaseDate: LocalDate,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class EquipmentResponse(
    val id: UUID,
    val type: EquipmentType,
    val brand: String,
    val model: String,
    val state: EquipmentState,
    val condition_score: BigDecimal,
    val purchase_date: LocalDate,
    val retire_reason: String?,
    val retired_at: Instant?,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RetireEquipmentRequest(
    @field:NotBlank @field:Size(max = 512) val reason: String,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateAllocationRequest(
    @field:NotBlank @field:Size(max = 128) val employeeId: String,
    @field:NotEmpty @field:Valid val policy: List<PolicySlotJson>,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AllocationSummaryResponse(
    val id: UUID,
    val employee_id: String,
    val state: AllocationState,
    val item_count: Int,
    val created_at: Instant,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AllocationDetailResponse(
    val id: UUID,
    val employee_id: String,
    val state: AllocationState,
    val failure_reason: String?,
    val policy: List<PolicySlotJson>,
    val allocated_equipments: List<AllocatedEquipmentResponse>,
    val created_at: Instant,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AllocatedEquipmentResponse(
    val policy_slot_index: Int,
    val equipment: EquipmentResponse,
)
