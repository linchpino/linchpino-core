package com.linchpino.core.controller

import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.dto.InterviewFeedBackRequest
import com.linchpino.core.dto.InterviewListResponse
import com.linchpino.core.dto.InterviewValidityResponse
import com.linchpino.core.service.FeedbackService
import com.linchpino.core.service.InterviewService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.Instant
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class InterviewControllerTest {
    @InjectMocks
    private lateinit var controller: InterviewController

    @Mock
    private lateinit var service: InterviewService

    @Mock
    private lateinit var feedbackService: FeedbackService

    @Test
    fun `test create new interview`() {
        val startTime = ZonedDateTime.now()
        val endTime = startTime.plusMinutes(45)
        val createInterviewRequest = CreateInterviewRequest(1, 1, startTime, endTime, 1, "john.doe@example.com")
        val expectedResponse = CreateInterviewResult(
            1,
            1,
            1,
            1,
            1,
            "john.doe@example.com"
        )

        `when`(service.createInterview(createInterviewRequest)).thenReturn(expectedResponse)

        val result = controller.newInterview(createInterviewRequest)

        assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(result.body).isEqualTo(expectedResponse)
    }

    @Test
    fun `test upcoming interviews calls service with correct arguments`() {
        // Given
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            mapOf(
                "sub" to "john.doe@example.com",
                "scope" to "MENTOR JOB_SEEKER"
            )
        )
        val pageable = Pageable.ofSize(10)

        val authentication = JwtAuthenticationToken(jwt)

        val expected = PageImpl(
            mutableListOf(
                InterviewListResponse(1L, "John Doe", ZonedDateTime.now(), ZonedDateTime.now(), "InterviewType")
            )
        )

        `when`(service.upcomingInterviews(authentication, pageable)).thenReturn(expected)

        // When
        val response = controller.upcomingInterviews(authentication, pageable)

        // Then
        assertThat(response).isEqualTo(expected)
        verify(service, times(1)).upcomingInterviews(authentication, pageable)
    }

    @Test
    fun `test past interviews calls service with correct arguments`() {
        // Given
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            mapOf(
                "sub" to "john.doe@example.com",
                "scope" to "MENTOR JOB_SEEKER"
            )
        )
        val pageable = Pageable.ofSize(10)

        val authentication = JwtAuthenticationToken(jwt)

        val expected = PageImpl(
            mutableListOf(
                InterviewListResponse(1L, "John Doe", ZonedDateTime.now(), ZonedDateTime.now(), "InterviewType")
            )
        )

        `when`(service.pastInterviews(authentication, pageable)).thenReturn(expected)

        // When
        val response = controller.pastInterviews(authentication, pageable)

        // Then
        assertThat(response).isEqualTo(expected)
        verify(service, times(1)).pastInterviews(authentication, pageable)
    }

    @Test
    fun `test interview validity calls service`() {
        // Given
        val expected =
            InterviewValidityResponse(ZonedDateTime.now(), ZonedDateTime.now().plusMinutes(60), true, "meet link")
        `when`(service.checkValidity(1)).thenReturn(expected)

        // When
        val result = controller.checkInterviewValidity(1)

        // Then
        verify(service, times(1)).checkValidity(1)
        assertThat(result.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(result.body).isEqualTo(expected)
    }

    @Test
    fun `test feedback calls service with proper arguments`() {
        // Given
        val request = InterviewFeedBackRequest(2, "content")
        val id = 1L

        // When
        controller.feedback(id, request)

        // Then
        verify(feedbackService, times(1)).createFeedback(id,request)
    }

    @Test
    fun `test upcoming interviews for job seeker calls service with correct arguments`() {
        // Given
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            mapOf(
                "sub" to "john.doe@example.com",
                "scope" to "MENTOR JOB_SEEKER"
            )
        )
        val pageable = Pageable.ofSize(10)

        val authentication = JwtAuthenticationToken(jwt)

        val expected = PageImpl(
            mutableListOf(
                InterviewListResponse(1L, "John Doe", ZonedDateTime.now(), ZonedDateTime.now(), "InterviewType")
            )
        )

        `when`(service.jobSeekerUpcomingInterviews(authentication, pageable)).thenReturn(expected)

        // When
        val response = controller.jobSeekerUpcomingInterviews(authentication, pageable)

        // Then
        assertThat(response).isEqualTo(expected)
        verify(service, times(1)).jobSeekerUpcomingInterviews(authentication, pageable)
    }

    @Test
    fun `test past interviews for job seeker calls service with correct arguments`() {
        // Given
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            mapOf(
                "sub" to "john.doe@example.com",
                "scope" to "MENTOR JOB_SEEKER"
            )
        )
        val pageable = Pageable.ofSize(10)

        val authentication = JwtAuthenticationToken(jwt)

        val expected = PageImpl(
            mutableListOf(
                InterviewListResponse(1L, "John Doe", ZonedDateTime.now(), ZonedDateTime.now(), "InterviewType")
            )
        )

        `when`(service.jobSeekerPastInterviews(authentication, pageable)).thenReturn(expected)

        // When
        val response = controller.jobSeekerPastInterviews(authentication, pageable)

        // Then
        assertThat(response).isEqualTo(expected)
        verify(service, times(1)).jobSeekerPastInterviews(authentication, pageable)
    }
}
