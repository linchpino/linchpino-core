package com.linchpino.core.service

import com.linchpino.core.dto.InterviewFeedBackRequest
import com.linchpino.core.entity.JobSeekerFeedback
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.FeedbackRepository
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.findReferenceById
import com.linchpino.core.security.email
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Service
@Transactional
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val interviewRepository: InterviewRepository,
    private val accountRepository: AccountRepository
) {

    fun createFeedback(interviewId: Long, request: InterviewFeedBackRequest) {
        val interview = interviewRepository.findReferenceById(interviewId)
        val loggedInUser = SecurityContextHolder.getContextHolderStrategy().context.authentication.email()
        val jobSeeker = accountRepository.findByEmailIgnoreCase(loggedInUser) ?: throw LinchpinException(
            ErrorCode.ENTITY_NOT_FOUND,
            "job seeker not found"
        )
        val feedback =
            feedbackRepository.findFirstByInterviewIdAndJobSeekerId(interview.id!!, jobSeeker.id!!)?.apply {
                satisfactionStatus = request.status
                content = request.content
                modifiedOn = ZonedDateTime.now()
                modifiedBy = jobSeeker.id
            } ?: JobSeekerFeedback().apply {
                this.interviewId = interview.id
                jobSeekerId = jobSeeker.id
                satisfactionStatus = request.status
                content = request.content
                createdOn = ZonedDateTime.now()
                createdBy = jobSeeker.id
            }
        feedbackRepository.save(feedback)
    }
}
