package com.linchpino.core.service

import com.linchpino.core.dto.AccountSummary
import com.linchpino.core.dto.ActivateJobSeekerAccountRequest
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.MentorWithClosestTimeSlot
import com.linchpino.core.dto.RegisterMentorRequest
import com.linchpino.core.dto.RegisterMentorResult
import com.linchpino.core.dto.mapper.AccountMapper
import com.linchpino.core.dto.toAccount
import com.linchpino.core.dto.toRegisterMentorResult
import com.linchpino.core.dto.toSummary
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Role
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewTypeRepository
import lombok.extern.slf4j.Slf4j
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
    private val interviewTypeRepository: InterviewTypeRepository
) {

    fun createAccount(createAccountRequest: CreateAccountRequest): CreateAccountResult {
        val account: Account = mapper.accountDtoToAccount(createAccountRequest)
        //account.createdOn(LocalDate.now())
        // encrypt password
        account.password = passwordEncoder.encode(account.password)
        repository.save(account)
        return mapper.entityToResultDto(account)
    }


    fun findMentorsWithClosestTimeSlotsBy(date: ZonedDateTime, interviewTypeId: Long): List<MentorWithClosestTimeSlot> {
        val from = date.withZoneSameInstant(ZoneOffset.UTC)
        val to = from.plusHours(24)
        return repository.closestMentorTimeSlots(from, to, interviewTypeId)
    }

    fun activeJobSeekerAccount(request: ActivateJobSeekerAccountRequest): AccountSummary {
        val account = repository.findByExternalId(request.externalId, AccountTypeEnum.JOB_SEEKER)
            ?: throw RuntimeException("account not found")
        if (account.status == AccountStatusEnum.ACTIVATED)
            throw RuntimeException("account is already activated")
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
        if (interviewTypes.isEmpty()) throw RuntimeException("invalid interviewTypes")
        interviewTypes.forEach { account.addInterviewType(it) }
        account.password = passwordEncoder.encode(request.password)
        account.addRole(Role().apply { roleName = AccountTypeEnum.MENTOR })
        repository.save(account)
        return account.toRegisterMentorResult()
    }
}
