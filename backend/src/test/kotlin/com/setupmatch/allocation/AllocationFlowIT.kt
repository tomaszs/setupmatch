package com.setupmatch

import com.setupmatch.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class AllocationFlowIT : IntegrationTestBase() {

    @Test
    fun `create confirm assigns equipment`() {
        val createBody = """
            {
              "employee_id": "emp-it-1",
              "policy": [
                { "type": "keyboard" }
              ]
            }
        """.trimIndent()

        val createResponse = mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = createBody
        }.andReturn().response.contentAsString

        val idRegex = """"id"\s*:\s*"([^"]+)"""".toRegex()
        val allocationId = idRegex.find(createResponse)?.groupValues?.get(1)
            ?: error("allocation id not found")

        mockMvc.post("/allocations/$allocationId/confirm")

        val detail = mockMvc.get("/allocations/$allocationId")
            .andReturn().response.contentAsString

        assert(detail.contains("confirmed"))
        assert(detail.contains("assigned"))
    }
}
