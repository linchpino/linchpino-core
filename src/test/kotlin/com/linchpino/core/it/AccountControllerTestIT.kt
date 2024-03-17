package com.linchpino.core.it

import com.linchpino.core.PostgresContainerConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.dto.CreateAccountRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Ensure rollback after each test
@Import(PostgresContainerConfig::class)
class AccountControllerTestIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `test creating new account`() {
        val createAccountRequest = CreateAccountRequest("John", "Doe", "john.doe@example.com", "password123")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(createAccountRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("John"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Doe"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.email").value("john.doe@example.com"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
			.andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
    }
}
