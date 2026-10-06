package com.setupmatch

import com.setupmatch.support.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class HardConstraintFetchIT : IntegrationTestBase() {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `allocation ignores equipment below min condition without locking it`() {
        jdbcTemplate.update(
            """
            INSERT INTO equipment (id, type, brand, model, state, condition_score, purchase_date)
            VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'monitor', 'Dell', 'Low Monitor', 'available', 0.65, '2024-01-01')
            """.trimIndent(),
        )
        jdbcTemplate.update(
            """
            INSERT INTO equipment (id, type, brand, model, state, condition_score, purchase_date)
            VALUES ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'monitor', 'Dell', 'High Monitor', 'available', 0.92, '2024-06-01')
            """.trimIndent(),
        )

        mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-hard-fetch",
                  "policy": [
                    { "type": "monitor", "min_condition": 0.80 }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.state") { value("allocated") }
            jsonPath("$.allocated_equipments[0].equipment.model") { value("High Monitor") }
        }

        mockMvc.get("/equipments?type=monitor&state=available")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.model == 'Low Monitor')]") { isNotEmpty() }
                jsonPath("$[?(@.model == 'High Monitor')]") { isEmpty() }
            }
    }

    @Test
    fun `allocation fails when only equipment below min condition exists`() {
        jdbcTemplate.update("DELETE FROM equipment WHERE type = 'mouse'")
        jdbcTemplate.update(
            """
            INSERT INTO equipment (id, type, brand, model, state, condition_score, purchase_date)
            VALUES ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'mouse', 'Logitech', 'Worn Mouse', 'available', 0.60, '2023-01-01')
            """.trimIndent(),
        )

        mockMvc.post("/allocations") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "employee_id": "emp-hard-fail",
                  "policy": [
                    { "type": "mouse", "min_condition": 0.75 }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.state") { value("failed") }
            jsonPath("$.allocated_equipments") { isEmpty() }
        }

        mockMvc.get("/equipments?type=mouse&state=available&include_retired=false")
            .andExpect {
                status { isOk() }
                jsonPath("$[?(@.model == 'Worn Mouse')]") { isNotEmpty() }
            }
    }
}
