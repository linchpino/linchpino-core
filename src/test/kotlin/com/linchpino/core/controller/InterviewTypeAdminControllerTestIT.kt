package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.InterviewTypeCreateRequest
import com.linchpino.core.dto.InterviewTypeUpdateRequest
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.security.WithMockJwt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@Import(PostgresContainerConfig::class)
class InterviewTypeAdminControllerTestIT {
    @Autowired
    private lateinit var interviewTypeRepository: InterviewTypeRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jobPositionRepository: JobPositionRepository

    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test create interviewType`() {
        val position = JobPosition().apply {
            title = "Java Developer"
        }
        jobPositionRepository.save(position)

        val request = InterviewTypeCreateRequest("Mock interview", position.id!!)

        mockMvc.perform(
            post("/api/admin/interviewtypes").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value(request.name))
    }

    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test create interviewType fails with 404 if jobPositionId is not valid`() {
        val request = InterviewTypeCreateRequest("Mock interview", 1)
        mockMvc.perform(
            post("/api/admin/interviewtypes").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("JobPosition entity not found"))
    }

    @WithMockJwt(
        "john.doe@example.com",
        roles = [AccountTypeEnum.MENTOR, AccountTypeEnum.GUEST, AccountTypeEnum.JOB_SEEKER]
    )
    @Test
    fun `test create interviewType fails with 403 if user is not admin`() {
        val request = InterviewTypeCreateRequest("Mock interview", 1)
        mockMvc.perform(
            post("/api/admin/interviewtypes").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test create interviewType fails with 400 if name is not provided`() {
        val request = InterviewTypeCreateRequest("", 1)
        mockMvc.perform(
            post("/api/admin/interviewtypes").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
    }

    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test get interview type returns successfully`() {
        val interviewType = InterviewType().apply {
            name = "Mock Interview"
        }
        interviewTypeRepository.save(interviewType)
        mockMvc.perform(
            get("/api/admin/interviewtypes/{id}", interviewType.id)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value(interviewType.name))
    }

    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test get interview type returns 404 if entity not found`() {

        mockMvc.perform(
            get("/api/admin/interviewtypes/{id}", 1)
        )
            .andExpect(status().isNotFound)
    }


    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test delete interview type`() {
        val interviewType = InterviewType().apply {
            name = "Mock Interview"
        }
        interviewTypeRepository.save(interviewType)
        mockMvc.perform(
            delete("/api/admin/interviewtypes/{id}", interviewType.id)
        )
            .andExpect(status().isNoContent)

        assertThat(interviewTypeRepository.findByIdOrNull(interviewType.id)).isNull()
    }

    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test update interview type`() {
        val interviewType = InterviewType().apply {
            name = "Mock Interview"
        }
        interviewTypeRepository.save(interviewType)

        val request = InterviewTypeUpdateRequest("newTitle")

        mockMvc.perform(
            put("/api/admin/interviewtypes/{id}", interviewType.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isOk)

        assertThat(interviewTypeRepository.findByIdOrNull(interviewType.id)?.name).isEqualTo(request.name)
    }

    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test update interview type returns 404 if entity not found`() {

        val request = InterviewTypeUpdateRequest("newTitle")

        mockMvc.perform(
            put("/api/admin/interviewtypes/{id}", 100)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isNotFound)

    }

}
