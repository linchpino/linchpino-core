package com.linchpino.core.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.InterviewTypeCreateRequest
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.security.WithMockJwt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@Import(PostgresContainerConfig::class)
class InterviewTypeControllerTestIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repository: InterviewTypeRepository

    @Autowired
    private lateinit var jobPositionRepository: JobPositionRepository

    @BeforeEach
    fun setUp() {
        val interviewType1 = InterviewType().apply { name = "Mock Interview" }
        val interviewType2 = InterviewType().apply { name = "Resume Review" }
        val interviewType3 = InterviewType().apply { name = "Edit Resume" }
        val interviewType4 = InterviewType().apply { name = "Data Science Roadmap" }
        val interviewType5 = InterviewType().apply { name = "Developer Roadmap" }


        repository.saveAll(listOf(interviewType1, interviewType2, interviewType3, interviewType4, interviewType5))
    }

    @Test
    fun `test searchByName returns page of interview types`() {

        mockMvc.perform(get("/api/interviewtypes/search?name=mock"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Mock Interview"))
    }

    @Test
    fun `test searchByName is case insensitive`() {

        mockMvc.perform(get("/api/interviewtypes/search?name=iNterView"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Mock Interview"))
    }

    @Test
    fun `test searchByName returns empty result when interviewType is not in the database`() {

        mockMvc.perform(get("/api/interviewtypes/search?name=notInDatabase"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(0))
    }

    @Test
    fun `test searchByName without providing name returns page of interview types`() {

        mockMvc.perform(get("/api/interviewtypes/search"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.content[0].title").value("Mock Interview"))
            .andExpect(jsonPath("$.content[1].title").value("Resume Review"))
            .andExpect(jsonPath("$.content[2].title").value("Edit Resume"))
            .andExpect(jsonPath("$.content[3].title").value("Data Science Roadmap"))
            .andExpect(jsonPath("$.content[4].title").value("Developer Roadmap"))
    }

    @Test
    fun `test searchByName without providing name returns page of interviewTypes based on provided page size`() {

        mockMvc.perform(get("/api/interviewtypes/search").queryParam("size", "2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("Mock Interview"))
            .andExpect(jsonPath("$.content[1].title").value("Resume Review"))
    }

    @Test
    fun `test sorting result`() {
        mockMvc.perform(get("/api/interviewtypes/search?name=resume&sort=name,desc"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[1].title").value("Edit Resume"))
            .andExpect(jsonPath("$.content[0].title").value("Resume Review"))
    }

    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test create interviewType`() {
        val position = JobPosition().apply {
            title = "Java Developer"
        }
        jobPositionRepository.save(position)

        val request = InterviewTypeCreateRequest("Mock interview", position.id!!)

        mockMvc.perform(
            post("/api/interviewtypes").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isCreated)
    }

    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test create interviewType fails with 404 if jobPositionId is not valid`() {
        val request = InterviewTypeCreateRequest("Mock interview", 1)
        mockMvc.perform(
            post("/api/interviewtypes").contentType(MediaType.APPLICATION_JSON)
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
            post("/api/interviewtypes").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @WithMockJwt("john.doe@example.com", roles = [AccountTypeEnum.ADMIN])
    @Test
    fun `test create interviewType fails with 400 if name is not provided`() {
        val request = InterviewTypeCreateRequest("", 1)
        mockMvc.perform(
            post("/api/interviewtypes").contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid Param"))
    }


}
