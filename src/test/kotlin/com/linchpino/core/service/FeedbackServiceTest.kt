package com.linchpino.core.service

import com.linchpino.core.dto.InterviewFeedBackRequest
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.JobSeekerFeedback
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.FeedbackRepository
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.findReferenceById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class FeedbackServiceTest {

    @Mock
    private lateinit var feedbackRepository: FeedbackRepository

    @Mock
    private lateinit var interviewRepository: InterviewRepository

    @Mock
    private lateinit var accountRepository: AccountRepository

    @InjectMocks
    private lateinit var feedbackService: FeedbackService

    @Mock
    private lateinit var securityContext: SecurityContext


    private fun mockSecurityContext() {
        SecurityContextHolder.setContext(securityContext)
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            mapOf(
                "sub" to "jane.smith@example.com",
            )
        )
        val authentication = JwtAuthenticationToken(jwt)
        `when`(securityContext.authentication).thenReturn(authentication)
    }

    @Test
    fun `createFeedback should create new feedback when it does not exist`() {
        // Given
        mockSecurityContext()

        val interview = Interview().apply {
            id = 1L
        }
        val jobSeeker = Account().apply {
            id = 1L
            email = "jane.smith@example.com"
        }
        val request = InterviewFeedBackRequest(status = 2, content = "Great interview")

        val feedbackCaptor: ArgumentCaptor<JobSeekerFeedback> = ArgumentCaptor.forClass(JobSeekerFeedback::class.java)

        `when`(interviewRepository.findReferenceById(1L)).thenReturn(interview)
        `when`(accountRepository.findByEmailIgnoreCase(jobSeeker.email)).thenReturn(jobSeeker)
        `when`(feedbackRepository.findFirstByInterviewIdAndJobSeekerId(interview.id!!, jobSeeker.id!!)).thenReturn(null)

        // When
        feedbackService.createFeedback(1L, request)

        // Then
        verify(feedbackRepository, times(1)).save(feedbackCaptor.capture())
        val savedFeedback = feedbackCaptor.value
        assertThat(savedFeedback.interviewId).isEqualTo(interview.id)
        assertThat(savedFeedback.jobSeekerId).isEqualTo(jobSeeker.id)
        assertThat(savedFeedback.satisfactionStatus).isEqualTo(request.status)
        assertThat(savedFeedback.content).isEqualTo(request.content)
        assertThat(savedFeedback.createdOn).isNotNull()
        assertThat(savedFeedback.createdBy).isNotNull()
    }

    @Test
    fun `createFeedback should update the feedback when it does already exist`() {
        // Given
        mockSecurityContext()

        val interview = Interview().apply {
            id = 1L
        }
        val jobSeeker = Account().apply {
            id = 1L
            email = "jane.smith@example.com"
        }
        val jobSeekerFeedback = JobSeekerFeedback().apply {
            id = 1L
            interviewId = interview.id
            jobSeekerId = jobSeeker.id
            satisfactionStatus = 1
            content = "Great interview"
        }
        val request = InterviewFeedBackRequest(status = 2, content = "Great interview updated")

        val feedbackCaptor: ArgumentCaptor<JobSeekerFeedback> = ArgumentCaptor.forClass(JobSeekerFeedback::class.java)

        `when`(interviewRepository.findReferenceById(1L)).thenReturn(interview)
        `when`(accountRepository.findByEmailIgnoreCase(jobSeeker.email)).thenReturn(jobSeeker)
        `when`(feedbackRepository.findFirstByInterviewIdAndJobSeekerId(interview.id!!, jobSeeker.id!!)).thenReturn(
            jobSeekerFeedback
        )

        // When
        feedbackService.createFeedback(1L, request)

        // Then
        verify(feedbackRepository, times(1)).save(feedbackCaptor.capture())
        val savedFeedback = feedbackCaptor.value
        assertThat(savedFeedback.interviewId).isEqualTo(interview.id)
        assertThat(savedFeedback.jobSeekerId).isEqualTo(jobSeeker.id)
        assertThat(savedFeedback.satisfactionStatus).isEqualTo(request.status)
        assertThat(savedFeedback.content).isEqualTo(request.content)
        assertThat(savedFeedback.modifiedOn).isNotNull()
        assertThat(savedFeedback.modifiedBy).isNotNull()
    }

    @Test
    fun `createFeedback should throw exception when job seeker not found`() {
        // Given
        mockSecurityContext()

        val interview = Interview().apply {
            id = 1L
        }
        val request = InterviewFeedBackRequest(status = 2, content = "Great interview")

        `when`(interviewRepository.findReferenceById(1L)).thenReturn(interview)
        `when`(accountRepository.findByEmailIgnoreCase("jane.smith@example.com")).thenReturn(null)

        // When
        val exception = Assertions.assertThrows(LinchpinException::class.java) {
            feedbackService.createFeedback(1L, request)
        }

        // Then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.ENTITY_NOT_FOUND)
    }

    @Test
    fun `test create feedback should throw exception if interview not found`() {
        // Given
        val request = InterviewFeedBackRequest(status = 2, content = "Great interview")
        `when`(interviewRepository.findReferenceById(1L)).thenThrow(JpaObjectRetrievalFailureException::class.java)

        // When
        val exception = Assertions.assertThrows(LinchpinException::class.java) {
            feedbackService.createFeedback(1L, request)
        }

        // Then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.ENTITY_NOT_FOUND)
    }

}
