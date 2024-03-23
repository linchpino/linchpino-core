package com.linchpino.core.controller.interview

import com.fasterxml.jackson.databind.ObjectMapper
import com.linchpino.core.PostgresContainerConfig
import com.linchpino.core.dto.InterviewRequest
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
        InterviewRequest(1, 1, 1, param)
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
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobPositionId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.interviewTypeId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timeSlotId").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.jobSeekerEmail").value("john.doe@example.com"))
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
