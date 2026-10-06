package com.setupmatch.api

import com.setupmatch.domain.EquipmentState
import com.setupmatch.domain.EquipmentType
import com.setupmatch.service.EquipmentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/equipments")
class EquipmentController(
    private val equipmentService: EquipmentService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateEquipmentRequest): EquipmentResponse =
        equipmentService.create(request)

    @GetMapping
    fun list(
        @RequestParam(required = false) state: EquipmentState?,
        @RequestParam(required = false) type: EquipmentType?,
        @RequestParam(name = "include_retired", defaultValue = "false") includeRetired: Boolean,
    ): List<EquipmentResponse> = equipmentService.list(state, type, includeRetired)

    @PostMapping("/{id}/retire")
    fun retire(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RetireEquipmentRequest,
    ): EquipmentResponse = equipmentService.retire(id, request)
}
