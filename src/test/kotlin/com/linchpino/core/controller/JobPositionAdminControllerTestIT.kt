package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.JobPositionCreateRequest
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.enums.AccountTypeEnum.ADMIN
import com.linchpino.core.enums.AccountTypeEnum.GUEST
import com.linchpino.core.enums.AccountTypeEnum.JOB_SEEKER
import com.linchpino.core.enums.AccountTypeEnum.MENTOR
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.security.WithMockJwt
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@Import(PostgresContainerConfig::class)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JobPositionAdminControllerTestIT {

    @Autowired
    private lateinit var jobPositionRepository: JobPositionRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @WithMockJwt("john.doe@example.com", roles = [ADMIN])
    @Test
    fun `test delete job position`() {
        // Given
        val jobPosition = JobPosition().apply { title = "Java Developer" }
        jobPositionRepository.save(jobPosition)

        val jobIdToDelete = jobPosition.id

        // When and Then
        mockMvc.perform(delete("/api/admin/jobposition/{id}", jobIdToDelete))
            .andExpect(status().isNoContent)
            .andReturn()
    }

    @WithMockJwt("john.doe@example.com", roles = [ADMIN])
    @Test
    fun `test update job position`() {
        // Given
        val jobPosition = JobPosition().apply { title = "Java Developer" }
        jobPositionRepository.save(jobPosition)

        val jobIdToUpdate = jobPosition.id
        val request = JobPositionCreateRequest("Tech Lead")

        // Perform the update request
        mockMvc.perform(
            put("/api/admin/jobposition/{id}", jobIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andReturn()

        Assertions.assertThat(jobPositionRepository.findByIdOrNull(jobIdToUpdate)?.title).isEqualTo("Tech Lead")
    }


    @WithMockJwt("john.doe@example.com", roles = [ADMIN])
    @Test
    fun `test get job position`() {
        // Given
        val jobPosition = JobPosition().apply { title = "Java Developer" }
        jobPositionRepository.save(jobPosition)
        val jobIdToFetch = jobPosition.id

        // Perform the get request
        mockMvc.perform(get("/api/admin/jobposition/{id}", jobIdToFetch))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(jobPosition.id)) // Validate id field
            .andExpect(jsonPath("$.title").value(jobPosition.title)) // Validate title field
    }

    @Test
    fun `test admin crud actions returns 401 if not authenticated`() {

        mockMvc.perform(get("/api/admin/jobposition/{id}", 1))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(put("/api/admin/jobposition/{id}", 1))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(delete("/api/admin/jobposition/{id}", 1))
            .andExpect(status().isUnauthorized)
    }

    @WithMockJwt("john.doe@example.com", roles = [GUEST, JOB_SEEKER, MENTOR])
    @Test
    fun `test admin crud actions returns 403 if not admin`() {

        mockMvc.perform(get("/api/admin/jobposition/{id}", 1))
            .andExpect(status().isForbidden)

        mockMvc.perform(put("/api/admin/jobposition/{id}", 1))
            .andExpect(status().isForbidden)

        mockMvc.perform(delete("/api/admin/jobposition/{id}", 1))
            .andExpect(status().isForbidden)
    }

    @WithMockJwt("john.doe@example.com", roles = [ADMIN])
    @Test
    fun `test create jobPosition`() {
        // Given
        val request = JobPositionCreateRequest("Java Developer")

        // When & Then
        mockMvc.perform(
            post("/api/admin/jobposition").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value(request.title))
    }


    @WithMockJwt(
        "john.doe@example.com",
        roles = [MENTOR, GUEST, JOB_SEEKER]
    )
    @Test
    fun `test create job position fails with 403 if user is not admin`() {
        // Given
        val request = JobPositionCreateRequest("Java Developer")

        // When & Then
        mockMvc.perform(
            post("/api/admin/jobposition").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }


    @WithMockJwt("john.doe@example.com", roles = [ADMIN])
    @Test
    fun `test create job position fails with 400 if title is not provided`() {
        // Given
        val request = JobPositionCreateRequest("")

        // When & Then
        mockMvc.perform(
            post("/api/admin/jobposition").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
    }

    @WithMockJwt("john.doe@example.com", roles = [ADMIN])
    @Test
    fun `test create jobPosition fails if job position with the same name exists`() {
        // Given
        val jobPosition = JobPosition().apply { title = "Java Developer" }
        jobPositionRepository.save(jobPosition)

        val request = JobPositionCreateRequest(jobPosition.title)

        // When & Then
        mockMvc.perform(
            post("/api/admin/jobposition").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Unique title violated for JobPosition"))
    }
}
