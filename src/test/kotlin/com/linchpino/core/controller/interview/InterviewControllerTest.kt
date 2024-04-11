package com.linchpino.core.controller.interview

import com.linchpino.core.controller.InterviewController
import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.service.InterviewService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus

@ExtendWith(MockitoExtension::class)
class InterviewControllerTest {

    @InjectMocks
    private lateinit var controller: InterviewController

    @Mock
    private lateinit var service: InterviewService

    @Test
    fun `test create new interview`() {
        // Given
        val createInterviewRequest = CreateInterviewRequest(1, 1, 1, 1, "john.doe@example.com")
        val expectedResponse = CreateInterviewResult(
            1,
            1,
            1,
            1,
            1,
            "john.doe@example.com"
        )

        `when`(service.createInterview(createInterviewRequest)).thenReturn(expectedResponse)

        // When
        val result = controller.newInterview(createInterviewRequest)

        // Then
        assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(result.body).isEqualTo(expectedResponse)
    }
}
