package com.setupmatch

import com.setupmatch.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class CancelReleasesEquipmentIT : IntegrationTestBase() {

    @Test
    fun `cancel releases equipment to available`() {
        val createBody = """
            {
              "employee_id": "emp-cancel-1",
              "policy": [
                { "type": "mouse" }
              ]
            }
        """.trimIndent()

        val createResponse = mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = createBody
        }.andReturn().response.contentAsString

        val equipmentIdRegex = """"equipment"\s*:\s*\{[^}]*"id"\s*:\s*"([^"]+)"""".toRegex()
        val equipmentId = equipmentIdRegex.find(createResponse)?.groupValues?.get(1)
            ?: error("equipment id not found")

        val allocationIdRegex = """"id"\s*:\s*"([^"]+)"""".toRegex()
        val allocationId = allocationIdRegex.find(createResponse)?.groupValues?.get(1)
            ?: error("allocation id not found")

        mockMvc.post("/allocations/$allocationId/cancel")

        val equipmentList = mockMvc.get("/equipments")
            .andReturn().response.contentAsString

        assert(equipmentList.contains(equipmentId))
        assert(equipmentList.contains("\"state\":\"available\"") || equipmentList.contains("\"state\": \"available\""))
    }
}
