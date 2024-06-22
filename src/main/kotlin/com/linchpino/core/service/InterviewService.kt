package com.linchpino.core.service

import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.dto.InterviewListResponse
import com.linchpino.core.dto.InterviewValidityResponse
import com.linchpino.core.dto.toCreateInterviewResult
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.repository.findReferenceById
import com.linchpino.core.security.email
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset
import java.time.ZonedDateTime

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
    private val meetService: MeetService
) {

    fun createInterview(request: CreateInterviewRequest): CreateInterviewResult {
        val jobSeekerAccount = accountRepository.findByEmailIgnoreCase(request.jobSeekerEmail)
            ?: accountRepository.findReferenceById(
                accountService.createAccount(
                    CreateAccountRequest(
                        firstName = null,
                        lastName = null,
                        email = request.jobSeekerEmail,
                        password = null,
                        status = AccountStatusEnum.DEACTIVATED,
                        type = AccountTypeEnum.JOB_SEEKER.value
                    )
                ).id
            )

        val interview = populateInterviewObject(request, jobSeekerAccount)
        interviewRepository.save(interview)
        interview.timeSlot?.let {
            timeSlotService.updateTimeSlotStatus(it, MentorTimeSlotEnum.ALLOCATED)
        }
        emailService.sendingInterviewInvitationEmailToJobSeeker(interview)

        return interview.toCreateInterviewResult()
    }

    fun populateInterviewObject(
        createInterviewRequest: CreateInterviewRequest,
        jobSeekerAcc: Account
    ): Interview {
        val position = jobPositionRepository.findReferenceById(createInterviewRequest.jobPositionId)
        val mentorAcc = accountRepository.findReferenceById(createInterviewRequest.mentorAccountId)
        val typeInterview = interviewTypeRepository.findReferenceById(createInterviewRequest.interviewTypeId)
        val mentorTimeSlot = mentorTimeSlotRepository.findReferenceById(createInterviewRequest.timeSlotId)
        val googleMeetCode = meetService.createGoogleWorkSpace()
        if (mentorTimeSlot.status == MentorTimeSlotEnum.ALLOCATED)
            throw LinchpinException(
                ErrorCode.TIMESLOT_IS_BOOKED,
                "this time slot is already booked : ${createInterviewRequest.timeSlotId}"
            )

        return Interview().apply {
            jobPosition = position
            interviewType = typeInterview
            timeSlot = mentorTimeSlot
            mentorAccount = mentorAcc
            jobSeekerAccount = jobSeekerAcc
            meetCode = googleMeetCode
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

    @Transactional(readOnly = true)
    fun checkValidity(id: Long): InterviewValidityResponse {
        val start = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC)
        val end = start.plusMinutes(5)
        val authentication = SecurityContextHolder.getContextHolderStrategy().context.authentication
        val email = authentication.email()
        val interview = interviewRepository.findByInterviewIdAndAccountEmail(id, email) ?: throw LinchpinException(
            ErrorCode.ENTITY_NOT_FOUND,
            "interview with $id and time range $start and $end not found",
            Interview::class.java.simpleName
        )

        return interview.timeSlot.let {
            if (it?.fromTime?.isBefore(start) == true || it?.fromTime?.isAfter(end) == true) {
                InterviewValidityResponse(it.fromTime, it.toTime, false, "")
            } else {
                InterviewValidityResponse(
                    it?.fromTime, it?.toTime, true, "https://meet.google.com/${interview.meetCode}"
                )
            }
        }
    }

}
