package com.setupmatch

import com.setupmatch.support.IntegrationTestBase
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.UUID

class QaScenariosIT : IntegrationTestBase() {

    @Test
    fun `scenario A cold start seed data`() {
        mockMvc.get("/equipments")
            .andExpect {
                status { isOk() }
                jsonPath("$") { isArray() }
                jsonPath("$.length()") { value(greaterThanOrEqualTo(12)) }
                jsonPath("$[0].condition_score") { exists() }
                jsonPath("$[?(@.state == 'retired')]") { isEmpty() }
            }
    }

    @Test
    fun `scenario B register equipment validation`() {
        mockMvc.post("/equipments") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "type": "monitor",
                  "brand": "Dell",
                  "model": "U2724D",
                  "condition_score": 0.85,
                  "purchase_date": "2024-10-01"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.state") { value("available") }
            jsonPath("$.brand") { value("Dell") }
        }

        mockMvc.post("/equipments") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "type": "monitor",
                  "brand": "Dell",
                  "model": "Bad",
                  "condition_score": 1.5,
                  "purchase_date": "2024-10-01"
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.field_errors.conditionScore") { exists() }
        }

        mockMvc.post("/equipments") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "type": "monitor",
                  "brand": "Dell",
                  "model": "Missing date",
                  "condition_score": 0.85
                }
            """.trimIndent()
        }.andExpect {
            status { isBadRequest() }
        }

        mockMvc.get("/equipments?type=monitor&state=available")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.model == 'U2724D')]") { isNotEmpty() }
            }
    }

    @Test
    fun `scenario C successful allocation confirm flow`() {
        val createResponse = mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-qa-1",
                  "policy": [
                    {
                      "type": "main_computer",
                      "min_condition": 0.8,
                      "preferred_brand": "Apple"
                    }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.state") { value("allocated") }
            jsonPath("$.allocated_equipments") { isArray() }
            jsonPath("$.allocated_equipments.length()") { value(1) }
        }.andReturn().response.contentAsString

        val allocationId = extractJsonField(createResponse, "id")
        val equipmentId = extractNestedEquipmentId(createResponse)

        mockMvc.get("/equipments")
            .andExpect {
                status { isOk() }
                content { string(containsString("\"id\":\"$equipmentId\"")) }
                content { string(containsString("\"state\":\"reserved\"")) }
            }

        mockMvc.post("/allocations/$allocationId/confirm")
            .andExpect {
                status { isOk() }
                jsonPath("$.state") { value("confirmed") }
            }

        mockMvc.get("/equipments")
            .andExpect {
                status { isOk() }
                content { string(containsString("\"id\":\"$equipmentId\"")) }
                content { string(containsString("\"state\":\"assigned\"")) }
            }
    }

    @Test
    fun `scenario D failed allocation leaves inventory available`() {
        mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-qa-fail",
                  "policy": [
                    { "type": "monitor", "min_condition": 0.95 },
                    { "type": "monitor", "min_condition": 0.95 }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.state") { value("failed") }
            jsonPath("$.failure_reason") { isNotEmpty() }
            jsonPath("$.allocated_equipments") { isEmpty() }
        }

        mockMvc.get("/equipments")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.state == 'reserved')]") { isEmpty() }
            }

        mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-qa-fail",
                  "policy": [
                    { "type": "keyboard" }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.state") { value("allocated") }
        }
    }

    @Test
    fun `scenario E cancel releases equipment`() {
        val createResponse = mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-qa-cancel",
                  "policy": [
                    { "type": "keyboard" }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.state") { value("allocated") }
        }.andReturn().response.contentAsString

        val allocationId = extractJsonField(createResponse, "id")
        val equipmentId = extractNestedEquipmentId(createResponse)

        mockMvc.post("/allocations/$allocationId/cancel")
            .andExpect {
                status { isOk() }
                jsonPath("$.state") { value("cancelled") }
            }

        mockMvc.get("/equipments")
            .andExpect {
                status { isOk() }
                content { string(containsString("\"id\":\"$equipmentId\"")) }
                content { string(containsString("\"state\":\"available\"")) }
            }

        mockMvc.post("/allocations/$allocationId/cancel")
            .andExpect {
                status { isConflict() }
            }
    }

    @Test
    fun `scenario F competing monitors allocate globally`() {
        mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-qa-monitors",
                  "policy": [
                    { "type": "monitor", "min_condition": 0.8 },
                    { "type": "monitor" }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.state") { value("allocated") }
            jsonPath("$.allocated_equipments.length()") { value(2) }
            jsonPath("$.allocated_equipments[0].equipment.id") { exists() }
            jsonPath("$.allocated_equipments[1].equipment.id") { exists() }
        }
    }

    @Test
    fun `scenario G retire lifecycle`() {
        val retireTarget = mockMvc.post("/equipments") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "type": "mouse",
                  "brand": "QA",
                  "model": "Retire Mouse",
                  "condition_score": 0.6,
                  "purchase_date": "2024-01-01"
                }
            """.trimIndent()
        }.andReturn().response.contentAsString

        val mouseId = extractJsonField(retireTarget, "id")

        mockMvc.post("/equipments/$mouseId/retire") {
            contentType = MediaType.APPLICATION_JSON
            content = """{ "reason": "QA retire test" }"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.state") { value("retired") }
            jsonPath("$.retire_reason") { value("QA retire test") }
        }

        mockMvc.get("/equipments?include_retired=true")
            .andExpect {
                status { isOk() }
                content { string(containsString("\"id\":\"$mouseId\"")) }
                content { string(containsString("\"state\":\"retired\"")) }
            }

        mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-qa-retire",
                  "policy": [
                    { "type": "mouse" }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.state") { value("allocated") }
            jsonPath("$.allocated_equipments[0].equipment.id") { value(not(equalTo(mouseId))) }
        }

        val keyboardAllocation = mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-qa-retire-reserved",
                  "policy": [
                    { "type": "keyboard" }
                  ]
                }
            """.trimIndent()
        }.andReturn().response.contentAsString

        val reservedEquipmentId = extractNestedEquipmentId(keyboardAllocation)

        mockMvc.post("/equipments/$reservedEquipmentId/retire") {
            contentType = MediaType.APPLICATION_JSON
            content = """{ "reason": "Should fail" }"""
        }.andExpect {
            status { isConflict() }
        }

        val assignedAllocation = mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-qa-retire-assigned",
                  "policy": [
                    { "type": "mouse" }
                  ]
                }
            """.trimIndent()
        }.andReturn().response.contentAsString

        val assignedAllocationId = extractJsonField(assignedAllocation, "id")
        val assignedEquipmentId = extractNestedEquipmentId(assignedAllocation)

        mockMvc.post("/allocations/$assignedAllocationId/confirm")

        mockMvc.post("/equipments/$assignedEquipmentId/retire") {
            contentType = MediaType.APPLICATION_JSON
            content = """{ "reason": "Should fail assigned" }"""
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `scenario H API edge cases`() {
        mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """{ "employee_id": "emp-edge", "policy": [] }"""
        }.andExpect {
            status { isBadRequest() }
        }

        val failedAllocation = mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-edge",
                  "policy": [
                    { "type": "monitor", "min_condition": 0.99 },
                    { "type": "monitor", "min_condition": 0.99 }
                  ]
                }
            """.trimIndent()
        }.andReturn().response.contentAsString

        val failedId = extractJsonField(failedAllocation, "id")

        mockMvc.post("/allocations/$failedId/confirm")
            .andExpect {
                status { isConflict() }
            }

        mockMvc.get("/allocations/${UUID.randomUUID()}")
            .andExpect {
                status { isNotFound() }
            }

        mockMvc.get("/equipments?type=main_computer&state=available")
            .andExpect {
                status { isOk() }
                jsonPath("$") { isArray() }
                jsonPath("$[?(@.type == 'main_computer')]") { isNotEmpty() }
            }

        mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-edge-filter",
                  "policy": [
                    { "type": "keyboard" }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
        }

        mockMvc.get("/allocations?employee_id=emp-edge-filter")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.employee_id == 'emp-edge-filter')]") { isNotEmpty() }
            }

        val successAllocation = mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-edge-double",
                  "policy": [
                    { "type": "mouse" }
                  ]
                }
            """.trimIndent()
        }.andReturn().response.contentAsString

        val successId = extractJsonField(successAllocation, "id")

        mockMvc.post("/allocations/$successId/confirm")
            .andExpect {
                status { isOk() }
            }

        mockMvc.post("/allocations/$successId/confirm")
            .andExpect {
                status { isConflict() }
            }
    }

    private fun extractJsonField(json: String, field: String): String {
        val regex = """"$field"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1) ?: error("$field not found in response")
    }

    private fun extractNestedEquipmentId(json: String): String {
        val regex = """"equipment"\s*:\s*\{[^}]*"id"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1) ?: error("equipment id not found in response")
    }
}
