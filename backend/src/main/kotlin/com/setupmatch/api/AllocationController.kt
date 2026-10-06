package com.setupmatch.api

import com.setupmatch.domain.AllocationState
import com.setupmatch.service.AllocationService
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
@RequestMapping("/allocations")
class AllocationController(
    private val allocationService: AllocationService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateAllocationRequest): AllocationDetailResponse =
        allocationService.create(request)

    @GetMapping
    fun list(
        @RequestParam(required = false) state: AllocationState?,
        @RequestParam(name = "employee_id", required = false) employeeId: String?,
    ): List<AllocationSummaryResponse> = allocationService.list(state, employeeId)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): AllocationDetailResponse = allocationService.get(id)

    @PostMapping("/{id}/confirm")
    fun confirm(@PathVariable id: UUID): AllocationDetailResponse = allocationService.confirm(id)

    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id: UUID): AllocationDetailResponse = allocationService.cancel(id)
}
