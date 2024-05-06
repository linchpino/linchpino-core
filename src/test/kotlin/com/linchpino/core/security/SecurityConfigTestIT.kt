package com.linchpino.core.security

import com.linchpino.core.PostgresContainerConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional // Ensure rollback after each test
@Import(PostgresContainerConfig::class)
class SecurityConfigTestIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `test CORS with fake origin header`() {

        mockMvc.perform(
            get("/api/jobposition/search?name=Engineer")
                .header("Origin", "http://blah.blah")
        )
            .andExpect(status().isOk)
            .andExpect(
                header().string(
                    "Access-Control-Allow-Origin",
                    "*"
                )
            )
    }

}
