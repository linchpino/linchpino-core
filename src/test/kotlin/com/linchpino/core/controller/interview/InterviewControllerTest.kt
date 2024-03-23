package com.linchpino.core.controller.interview

import com.linchpino.core.controller.InterviewController
import com.linchpino.core.dto.InterviewRequest
import com.linchpino.core.dto.InterviewResult
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
        val interviewRequest = InterviewRequest(1, 1, 1, "john.doe@example.com")
        val expectedResponse = InterviewResult(
            1,
            1,
            1,
            1,
            "john.doe@example.com"
        )

        `when`(service.newInterview(interviewRequest)).thenReturn(expectedResponse)

        // When
        val result = controller.newInterview(interviewRequest)

        // Then
        assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(result.body).isEqualTo(expectedResponse)
    }
}
