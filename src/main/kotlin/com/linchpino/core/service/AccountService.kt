package com.linchpino.core.service

import com.linchpino.core.dto.*
import com.linchpino.core.dto.mapper.AccountMapper
import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.RoleRepository
import com.linchpino.core.repository.findReferenceById
import lombok.extern.slf4j.Slf4j
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Service
@Slf4j
@Transactional
class AccountService(
    private val repository: AccountRepository,
    private val mapper: AccountMapper,
    private val passwordEncoder: PasswordEncoder,
    private val interviewTypeRepository: InterviewTypeRepository,
    private val roleRepository: RoleRepository
) {

    fun createAccount(createAccountRequest: CreateAccountRequest): CreateAccountResult {
        val account: Account = mapper.accountDtoToAccount(createAccountRequest)
        account.password = passwordEncoder.encode(account.password)
        account.addRole(roleRepository.getReferenceById(createAccountRequest.type))
        try {
            repository.save(account)
        } catch (ex: DataIntegrityViolationException) {
            throw LinchpinException("unique email constraint violation", ex, ErrorCode.DUPLICATE_EMAIL)
        }
        return mapper.entityToResultDto(account)
    }


    fun findMentorsWithClosestTimeSlotsBy(date: ZonedDateTime, interviewTypeId: Long): List<MentorWithClosestTimeSlot> {
        val from = date.withZoneSameInstant(ZoneOffset.UTC)
        val to = from.plusHours(24)
        return repository.closestMentorTimeSlots(from, to, interviewTypeId)
    }

    fun activeJobSeekerAccount(request: ActivateJobSeekerAccountRequest): AccountSummary {
        val account = repository.findByExternalId(request.externalId, AccountTypeEnum.JOB_SEEKER)
            ?: throw LinchpinException(ErrorCode.ACCOUNT_NOT_FOUND, "no account found by externalId: ${request.externalId}")
        if (account.status == AccountStatusEnum.ACTIVATED)
            throw LinchpinException(ErrorCode.ACCOUNT_IS_ACTIVATED, "account is already activated")
        val updatedAccount = account.apply {
            firstName = request.firstName
            lastName = request.lastName
            password = passwordEncoder.encode(request.password)
            status = AccountStatusEnum.ACTIVATED
        }
        repository.save(updatedAccount)
        return updatedAccount.toSummary()
    }

    fun registerMentor(request: RegisterMentorRequest): RegisterMentorResult {
        val account = request.toAccount()
        val interviewTypes = interviewTypeRepository.findAllByIdIn(request.interviewTypeIDs)
        if (interviewTypes.isEmpty()) throw LinchpinException(ErrorCode.INTERVIEW_TYPE_NOT_FOUND,"no interview type found with id in: ${request.interviewTypeIDs}")
        interviewTypes.forEach { account.addInterviewType(it) }
        account.password = passwordEncoder.encode(request.password)
        val mentorRole = roleRepository.findReferenceById(AccountTypeEnum.MENTOR.value)
        account.addRole(mentorRole)
        try {
            repository.save(account)
        } catch (ex: DataIntegrityViolationException) {
            throw LinchpinException("unique email constraint violation", ex, ErrorCode.DUPLICATE_EMAIL)
        }
        return account.toRegisterMentorResult()
    }
}
