package com.setupmatch.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val fieldErrors = ex.bindingResult.fieldErrors.associate { field ->
            field.field to (field.defaultMessage ?: "invalid")
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiError(message = "Validation failed", field_errors = fieldErrors),
        )
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleStatus(ex: ResponseStatusException): ResponseEntity<ApiError> {
        return ResponseEntity.status(ex.statusCode).body(ApiError(message = ex.reason ?: "Request failed"))
    }
}
