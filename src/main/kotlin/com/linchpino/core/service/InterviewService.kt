package com.linchpino.core.service

import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.dto.InterviewListResponse
import com.linchpino.core.dto.InterviewValidityResponse
import com.linchpino.core.dto.toCreateInterviewResult
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.interviewPartiesFullName
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.InterviewLogType
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.findReferenceById
import com.linchpino.core.security.email
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Service
@Transactional
class InterviewService(
    private val interviewRepository: InterviewRepository,
    private val accountRepository: AccountRepository,
    private val jobPositionRepository: JobPositionRepository,
    private val interviewTypeRepository: InterviewTypeRepository,
    private val accountService: AccountService,
    private val emailService: EmailService,
    private val calendarService: CalendarService,
    private val scheduleService: ScheduleService,
    private val interviewLogService: InterviewLogService
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
        emailService.sendingInterviewInvitationEmailToJobSeeker(interview)
        interviewLogService.save(InterviewLogType.CREATED,jobSeekerAccount.id)
        return interview.toCreateInterviewResult()
    }

    fun populateInterviewObject(
        createInterviewRequest: CreateInterviewRequest,
        jobSeekerAcc: Account
    ): Interview {
        val position = jobPositionRepository.findReferenceById(createInterviewRequest.jobPositionId)
        val mentorAcc = accountRepository.findReferenceById(createInterviewRequest.mentorAccountId)
        val typeInterview = interviewTypeRepository.findReferenceById(createInterviewRequest.interviewTypeId)
        val mentorTimeSlot = scheduleService.availableTimeSlot(mentorAcc, createInterviewRequest)

        val googleMeetCode = calendarService.googleMeetCode(
            listOf(mentorAcc.email, jobSeekerAcc.email),
            "${typeInterview.name} with ${mentorAcc.firstName} and ${jobSeekerAcc.firstName ?: "jobseeker"}",
            Pair(mentorTimeSlot.fromTime, mentorTimeSlot.toTime)
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

    fun checkValidity(id: Long, authentication: Authentication): InterviewValidityResponse {
        val start = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC)
        val end = start.plusMinutes(5)
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
                accountRepository.findByEmailIgnoreCase(email)?.id?.let {
                    id -> interviewLogService.save(InterviewLogType.JOINED,id)
                }
                InterviewValidityResponse(
                    it?.fromTime, it?.toTime, true, "https://meet.google.com/${interview.meetCode}"
                )
            }
        }
    }

    @Transactional(readOnly = true)
    fun jobSeekerUpcomingInterviews(authentication: Authentication, page: Pageable): Page<InterviewListResponse> {
        return interviewRepository.findJobSeekerUpcomingInterviews(authentication.email(), page)
    }

    @Transactional(readOnly = true)
    fun jobSeekerPastInterviews(authentication: Authentication, page: Pageable): Page<InterviewListResponse> {
        return interviewRepository.findJobSeekerPastInterviews(authentication.email(), page)
    }


    @Scheduled(fixedRate = 3600_000)
    fun remindInterviewParties() {
        interviewRepository
            .findInterviewsWithin(ZonedDateTime.now(), ZonedDateTime.now().plusHours(24))
            .map {
                val (mentorFullName, jobSeekerFullName) = it.interviewPartiesFullName()

                Reminder(
                    it.mentorAccount!!.email to mapOf(
                        "fullName" to mentorFullName,
                        "intervieweeName" to jobSeekerFullName,
                        "date" to it.timeSlot?.fromTime?.toLocalDate(),
                        "time" to it.timeSlot?.fromTime?.toLocalTime(),
                        "timezone" to it.timeSlot?.fromTime?.zone,
                    ), it.jobSeekerAccount!!.email to mapOf(
                        "fullName" to jobSeekerFullName,
                        "date" to it.timeSlot?.fromTime?.toLocalDate(),
                        "time" to it.timeSlot?.fromTime?.toLocalTime(),
                        "timezone" to it.timeSlot?.fromTime?.zone,
                    )
                )
            }.forEach {
                emailService.sendEmail(
                    it.jobSeekerData.first,
                    "Reminder: Your Upcoming Interview on Linchpino",
                    "interviewee-reminder.html",
                    it.jobSeekerData.second,
                )

                emailService.sendEmail(
                    it.mentorData.first,
                    "Reminder: Interview Scheduled on Linchpino",
                    "interviewer-reminder.html",
                    it.mentorData.second,
                )
            }
    }

    private data class Reminder(val mentorData:Pair<String,Map<String,Serializable?>>,val jobSeekerData:Pair<String,Map<String,Serializable?>>)
}
