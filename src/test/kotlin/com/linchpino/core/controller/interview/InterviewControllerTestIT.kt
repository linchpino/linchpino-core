package com.linchpino.core.controller.interview

import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.InterviewRequest
import com.linchpino.core.dto.SilenceAccountRequest
import com.linchpino.core.dto.SilenceAccountResult
import com.linchpino.core.enums.AccountStatus
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
@Transactional
@Import(PostgresContainerConfig::class)
class InterviewControllerTestIT {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private fun dataProvider(param: String) {
        InterviewRequest(1, 1, 1, SilenceAccountRequest(param, 1))
    }

    private fun accountDataProvider(): SilenceAccountResult {
        return SilenceAccountResult(
            "john.doe@example.com",
            AccountStatus.DEACTIVATED
        )
    }

    @Test
    fun `test creating a new interview for jobSeeker`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(dataProvider("john.doe@example.com")))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.JobPositionId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.InterviewTypeId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.MentorTimeSlotId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobSeekerAccount").value(accountDataProvider()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber)
    }

    @Test
    fun `test creating a new interview with blanket email address results in bad request`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/interviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(dataProvider("")))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
