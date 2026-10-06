package com.setupmatch.support

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = ["/sql/reset-test-data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
abstract class IntegrationTestBase {

    @Autowired
    protected lateinit var mockMvc: MockMvc
}
