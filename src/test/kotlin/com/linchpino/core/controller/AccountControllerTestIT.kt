package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.CreateAccountRequest
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
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
	fun `test creating jobSeeker account`() {
		val createAccountRequest = CreateAccountRequest("John", "Doe", "john.doe@example.com", "password123", 1)

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
			.andExpect(MockMvcResultMatchers.jsonPath("$.type").value("JOB_SEEKER"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.status").value("DEACTIVATED"))
	}

	@Test
	fun `test creating account with blank firstName results in bad request`() {
		val invalidRequest = CreateAccountRequest("", "Doe", "john.doe@example.com", "secret", 1)
		mockMvc.perform(
			MockMvcRequestBuilders.post("/api/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapper().writeValueAsString(invalidRequest))
		)
			.andExpect(MockMvcResultMatchers.status().isBadRequest)
			.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid Param"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap", hasSize<Int>(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap[0].field").value("firstName"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap[0].message").value("firstname is required"))
	}

	@Test
	fun `test creating account with blank lastName results in bad request`() {
		val invalidRequest = CreateAccountRequest("John", "", "john.doe@example.com", "secret", 1)

		mockMvc.perform(
			MockMvcRequestBuilders.post("/api/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapper().writeValueAsString(invalidRequest))
		)
			.andExpect(MockMvcResultMatchers.status().isBadRequest)
			.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid Param"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap", hasSize<Int>(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap[0].field").value("lastName"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap[0].message").value("lastname is required"))
	}

	@Test
	fun `test creating account with invalid email results in bad request`() {
		val invalidRequest = CreateAccountRequest("John", "Doe", "john.doe_example.com", "secret", 1)

		mockMvc.perform(
			MockMvcRequestBuilders.post("/api/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapper().writeValueAsString(invalidRequest))
		)
			.andExpect(MockMvcResultMatchers.status().isBadRequest)
			.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid Param"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap", hasSize<Int>(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap[0].field").value("email"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap[0].message").value("email is not valid"))
	}

	@Test
	fun `test creating account with multiple invalid fields results in bad request`() {
		val invalidRequest = CreateAccountRequest("", "Doe", "john.doe_example.com", "secret", 1)

		mockMvc.perform(
			MockMvcRequestBuilders.post("/api/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapper().writeValueAsString(invalidRequest))
		)
			.andExpect(MockMvcResultMatchers.status().isBadRequest)
			.andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid Param"))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap").isArray)
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap[*].field", hasItem("email")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap[*].message", hasItem("email is not valid")))
			.andExpect(MockMvcResultMatchers.jsonPath("$.validationErrorMap[*].field", hasItem("firstName")))
			.andExpect(
				MockMvcResultMatchers.jsonPath(
					"$.validationErrorMap[*].message",
					hasItem("firstname is required")
				)
			)
	}
}
