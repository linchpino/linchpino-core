package com.linchpino.core.service

import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.dto.InterviewListResponse
import com.linchpino.core.dto.toCreateInterviewResult
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.repository.findReferenceById
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class InterviewService(
    private val interviewRepository: InterviewRepository,
    private val accountRepository: AccountRepository,
    private val jobPositionRepository: JobPositionRepository,
    private val interviewTypeRepository: InterviewTypeRepository,
    private val mentorTimeSlotRepository: MentorTimeSlotRepository,
    private val accountService: AccountService,
    private val timeSlotService: TimeSlotService,
    private val emailService: EmailService,
) {

    fun createInterview(request: CreateInterviewRequest): CreateInterviewResult {
        val jobSeekerAccount = accountRepository.findByEmailIgnoreCase(request.jobSeekerEmail)
            ?: accountRepository.findReferenceById(
                accountService.createAccount(
                    CreateAccountRequest(
                        firstName = "",
                        lastName = "",
                        email = request.jobSeekerEmail,
                        password = null,
                        status = AccountStatusEnum.DEACTIVATED,
                        type = AccountTypeEnum.JOB_SEEKER.value
                    )
                ).id
            )

        val interview = populateInterviewObject(request, jobSeekerAccount)
        interviewRepository.save(interview)
        interview.mentorAccount?.let { timeSlotService.updateTimeSlotAfterCreateInterview(it) }
        emailService.sendingInterviewEmailToJobSeeker(interview)

        return interview.toCreateInterviewResult()
    }

    internal fun populateInterviewObject(
        createInterviewRequest: CreateInterviewRequest,
        jobSeekerAcc: Account
    ): Interview {
        val position = jobPositionRepository.findReferenceById(createInterviewRequest.jobPositionId)
        val mentorAcc = accountRepository.findReferenceById(createInterviewRequest.mentorAccountId)
        val typeInterview = interviewTypeRepository.findReferenceById(createInterviewRequest.interviewTypeId)
        val isTimeSlotBooked = interviewRepository.isTimeSlotBooked(createInterviewRequest.timeSlotId)
        if (isTimeSlotBooked)
            throw LinchpinException(
                ErrorCode.TIMESLOT_IS_BOOKED,
                "this time slot is already booked : ${createInterviewRequest.timeSlotId}"
            )
        val mentorTimeSlot = mentorTimeSlotRepository.findReferenceById(createInterviewRequest.timeSlotId)

        return Interview().apply {
            jobPosition = position
            interviewType = typeInterview
            timeSlot = mentorTimeSlot
            mentorAccount = mentorAcc
            jobSeekerAccount = jobSeekerAcc
        }
    }

    @Transactional(readOnly = true)
    fun upcomingInterviews(authentication: Authentication, page: Pageable): Page<InterviewListResponse> {
        return interviewRepository.findUpcomingInterviews(authentication.email(), page)
    }

    @Transactional(readOnly = true)
    fun pastInterviews(authentication: Authentication, page: Pageable): Page<InterviewListResponse> {
        return interviewRepository.findPastInterviews(authentication.email(), page)
    }

}
