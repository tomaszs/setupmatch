package com.setupmatch

import com.setupmatch.support.IntegrationTestBase
import org.hamcrest.Matchers.everyItem
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class EquipmentListIT : IntegrationTestBase() {

    @Test
    fun `list excludes retired equipment by default`() {
        mockMvc.get("/equipments")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.state == 'retired')]") { isEmpty() }
            }
    }

    @Test
    fun `list includes retired equipment when requested`() {
        mockMvc.get("/equipments?include_retired=true")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.state == 'retired')]") { isNotEmpty() }
            }
    }

    @Test
    fun `list filters by state and type`() {
        mockMvc.post("/equipments") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "type": "keyboard",
                  "brand": "Logitech",
                  "model": "MX Keys IT",
                  "condition_score": 0.9,
                  "purchase_date": "2024-10-01"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
        }

        mockMvc.get("/equipments?state=available&type=keyboard")
            .andExpect {
                status { isOk() }
                jsonPath("$[*].state") { everyItem(org.hamcrest.Matchers.equalTo("available")) }
                jsonPath("$[*].type") { everyItem(org.hamcrest.Matchers.equalTo("keyboard")) }
                jsonPath("$[?(@.model == 'MX Keys IT')]") { isNotEmpty() }
            }

        mockMvc.get("/equipments?state=available&type=monitor")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.model == 'MX Keys IT')]") { isEmpty() }
                jsonPath("$[*].type") { everyItem(org.hamcrest.Matchers.equalTo("monitor")) }
            }
    }

    @Test
    fun `list returns equipment sorted by created_at ascending`() {
        val firstResponse = mockMvc.get("/equipments?type=mouse&state=available")
            .andReturn().response.contentAsString

        val firstId = extractFirstId(firstResponse)

        mockMvc.post("/equipments") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "type": "mouse",
                  "brand": "Logitech",
                  "model": "MX Master IT Sort",
                  "condition_score": 0.88,
                  "purchase_date": "2024-10-02"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
        }

        val secondResponse = mockMvc.get("/equipments?type=mouse&state=available")
            .andReturn().response.contentAsString

        val firstIndex = secondResponse.indexOf(firstId)
        val secondIndex = secondResponse.indexOf("MX Master IT Sort")

        assert(firstIndex >= 0)
        assert(secondIndex >= 0)
        assert(firstIndex < secondIndex)
    }

    private fun extractFirstId(json: String): String {
        val idRegex = """"id"\s*:\s*"([^"]+)"""".toRegex()
        return idRegex.find(json)?.groupValues?.get(1) ?: error("equipment id not found")
    }
}
