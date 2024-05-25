package com.linchpino.core.service

import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.dto.InterviewListResponse
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.repository.RoleRepository
import com.linchpino.core.repository.findReferenceById
import com.linchpino.core.security.email
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
    private val roleRepository: RoleRepository
) {

    fun createInterview(request: CreateInterviewRequest): CreateInterviewResult {
        val jobSeekerAccount = isJobSeekerHasAccount(request.jobSeekerEmail)
        val interview = populateInterviewObject(request, jobSeekerAccount)
        interviewRepository.save(interview)
        return interviewResult(interview)
    }

    private fun isJobSeekerHasAccount(jobSeekerEmail: String): Account {
        return accountRepository.findByEmailIgnoreCase(jobSeekerEmail) ?: createSilentAccForJobSeeker(jobSeekerEmail)
    }

    private fun createSilentAccForJobSeeker(email: String): Account {
        val jobSeekerRole = roleRepository.getReferenceById(AccountTypeEnum.JOB_SEEKER.value)
        return accountRepository.save(Account().apply {
            this.email = email
            status = AccountStatusEnum.DEACTIVATED
            addRole(jobSeekerRole)
        })
    }

    fun populateInterviewObject(createInterviewRequest: CreateInterviewRequest, jobSeekerAcc: Account): Interview {
        val position = jobPositionRepository.findReferenceById(createInterviewRequest.jobPositionId)
        val mentorAcc = accountRepository.findReferenceById(createInterviewRequest.mentorAccId)
        val typeInterview = interviewTypeRepository.findReferenceById(createInterviewRequest.interviewTypeId)
        val mentorTimeSlot = mentorTimeSlotRepository.findReferenceById(createInterviewRequest.timeSlotId)

        return Interview().apply {
            jobPosition = position
            interviewType = typeInterview
            timeSlot = mentorTimeSlot
            mentorAccount = mentorAcc
            jobSeekerAccount = jobSeekerAcc
        }
    }

    private fun interviewResult(entity: Interview): CreateInterviewResult {
        return CreateInterviewResult(
            entity.id,
            entity.jobPosition?.id,
            entity.interviewType?.id,
            entity.timeSlot?.id,
            entity.mentorAccount?.id,
            entity.jobSeekerAccount?.email,
        )
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
