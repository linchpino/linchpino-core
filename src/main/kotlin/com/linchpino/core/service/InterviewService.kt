package com.linchpino.core.service

import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.CreateInterviewResult
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.enums.AccountStatus
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Slf4j
@Transactional
class InterviewService(
    private val interviewRepository: InterviewRepository,
    private val accountRepository: AccountRepository,
    private val jobPositionRepository: JobPositionRepository,
    private val interviewTypeRepository: InterviewTypeRepository,
    private val mentorTimeSlotRepository: MentorTimeSlotRepository,
) {

    fun createInterview(request: CreateInterviewRequest): CreateInterviewResult {
        val jobSeekerAccount = isJobSeekerHasAccount(request.jobSeekerEmail)
        val interview = populateInterviewObject(request, jobSeekerAccount)
        interviewRepository.save(interview)
        return interviewResult(interview)
    }

    private fun isJobSeekerHasAccount(jobSeekerEmail: String): Account? {
        return accountRepository.findByEmail(jobSeekerEmail) ?: createSilentAccForJobSeeker(jobSeekerEmail)
    }

    private fun createSilentAccForJobSeeker(email: String): Account {
        return accountRepository.save(Account().apply {
            this.email = email
            type = AccountTypeEnum.JOB_SEEKER
            status = AccountStatus.DEACTIVATED
        })
    }

    fun populateInterviewObject(createInterviewRequest: CreateInterviewRequest, jobSeekerAcc: Account?): Interview {
        val position = jobPositionRepository.getReferenceById(createInterviewRequest.jobPositionId)
        val mentorAcc = accountRepository.getReferenceById(createInterviewRequest.mentorAccId)
        val typeInterview = interviewTypeRepository.getReferenceById(createInterviewRequest.interviewTypeId)
        val mentorTimeSlot = mentorTimeSlotRepository.getReferenceById(createInterviewRequest.timeSlotId)

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
}
