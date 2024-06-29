package com.linchpino.core.service

import com.linchpino.core.dto.*
import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.RoleRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

@Service
@Transactional
class AccountService(
    private val repository: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val interviewTypeRepository: InterviewTypeRepository,
    private val roleRepository: RoleRepository,
    private val emailService: EmailService
) {

    fun createAccount(createAccountRequest: CreateAccountRequest): CreateAccountResult {
        val request = SaveAccountRequest(
            createAccountRequest.firstName,
            createAccountRequest.lastName,
            createAccountRequest.email,
            createAccountRequest.password,
            listOf(createAccountRequest.type),
            createAccountRequest.status
        )
        return saveAccount(request).toCreateAccountResult()
    }

    fun findMentorsWithClosestTimeSlotsBy(date: ZonedDateTime, interviewTypeId: Long): List<MentorWithClosestTimeSlot> {
        val from = date.withZoneSameInstant(ZoneOffset.UTC)
        val to = from.plusHours(24)
        return repository.closestMentorTimeSlots(from, to, interviewTypeId)
    }

    fun activeJobSeekerAccount(request: ActivateJobSeekerAccountRequest): AccountSummary {
        val account = repository.findByExternalId(request.externalId, AccountTypeEnum.JOB_SEEKER)
            ?: throw LinchpinException(
                ErrorCode.ACCOUNT_NOT_FOUND,
                "no account found by externalId: ${request.externalId}"
            )
        if (account.status == AccountStatusEnum.ACTIVATED)
            throw LinchpinException(ErrorCode.ACCOUNT_IS_ACTIVATED, "account is already activated")

        val updateAccountRequest = UpdateAccountRequest(
            firstName = request.firstName,
            lastName = request.lastName,
            email = account.email,
            plainTextPassword = request.password,
            roles = listOf(AccountTypeEnum.JOB_SEEKER.value),
            status = AccountStatusEnum.ACTIVATED,
            externalId = request.externalId
        )
        updateAccount(updateAccountRequest, account)
        return account.toSummary()
    }

    fun registerMentor(request: RegisterMentorRequest): RegisterMentorResult {
        val saveAccountRequest = SaveAccountRequest(
            request.firstName,
            request.lastName,
            request.email,
            request.password,
            listOf(AccountTypeEnum.MENTOR.value),
            AccountStatusEnum.ACTIVATED,
            request.interviewTypeIDs,
            request.detailsOfExpertise,
            request.linkedInUrl
        )
        val registeredMentor = saveAccount(saveAccountRequest).toRegisterMentorResult()
        registeredMentor.firstName?.let { firstName ->
            registeredMentor.lastName?.let { lastName ->
                emailService.sendingWelcomeEmailToMentor(
                    firstName,
                    lastName,
                    registeredMentor.email
                )
            }
        }

        return registeredMentor
    }

    private fun saveAccount(request: SaveAccountRequest): Account {
        val interviewTypes = interviewTypeRepository.findAllByIdIn(request.interviewTypeIDs)
        if (request.interviewTypeIDs.size != interviewTypes.size)
            throw LinchpinException(
                ErrorCode.INTERVIEW_TYPE_NOT_FOUND,
                "no interview type found with id in: ${request.interviewTypeIDs}"
            )
        val roles = roleRepository.findAll()
            .filter { request.roles.contains(it.id) }
        val account = Account().apply {
            firstName = request.firstName
            lastName = request.lastName
            password = passwordEncoder.encode(request.plainTextPassword ?: "")
            email = request.email
            status = request.status
            roles.forEach { addRole(it) }
            interviewTypes.forEach { addInterviewType(it) }
            detailsOfExpertise = request.detailsOfExpertise
            linkedInUrl = request.linkedInUrl
            externalId = UUID.randomUUID().toString()
        }
        try {
            repository.save(account)
        } catch (ex: DataIntegrityViolationException) {
            throw LinchpinException("unique email constraint violation", ex, ErrorCode.DUPLICATE_EMAIL)
        }
        return account
    }

    private fun updateAccount(request: UpdateAccountRequest, account: Account) {
        val interviewTypes = interviewTypeRepository.findAllByIdIn(request.interviewTypeIDs)
        if (request.interviewTypeIDs.size != interviewTypes.size)
            throw LinchpinException(
                ErrorCode.INTERVIEW_TYPE_NOT_FOUND,
                "no interview type found with id in: ${request.interviewTypeIDs}"
            )
        val roles = roleRepository.findAll()
            .filter { request.roles.contains(it.id) }
        account.apply {
            firstName = request.firstName ?: this.firstName
            lastName = request.lastName ?: this.lastName
            if (request.plainTextPassword != null) {
                password = passwordEncoder.encode(request.plainTextPassword)
            }
            status = request.status ?: this.status
            if (roles.isNotEmpty()) {
                this.roles().forEach { removeRole(it) }
                roles.forEach { addRole(it) }
            }
            if (interviewTypes.isNotEmpty()) {
                interviewTypes().forEach { removeInterviewType(it) }
                interviewTypes.forEach { addInterviewType(it) }
            }
            detailsOfExpertise = request.detailsOfExpertise ?: this.detailsOfExpertise
            linkedInUrl = request.linkedInUrl ?: this.linkedInUrl
            externalId = request.externalId ?: this.externalId
        }
        repository.save(account)
    }

    fun searchAccountByNameOrRoleOrBoth(name: String, roleTitle: String): List<SearchAccountResult> {
        if (name.isNotEmpty() && roleTitle.isNotEmpty())
            return mapSearchResultForNameAndRoles(repository.findByNameAndRoles(name, roleTitle))
        if (name.isEmpty() && roleTitle.isNotEmpty())
            return mapSearchResultByRoleOrName(repository.findByRole(roleTitle))
        return mapSearchResultByRoleOrName(repository.findByName(name))
    }

    private fun mapSearchResultByRoleOrName(account: Account): List<SearchAccountResult> {
        if (account == null)
            throw LinchpinException(
                ErrorCode.ACCOUNT_NOT_FOUND,
                "No accounts found for the given criteria"
            )

        return account.roles().map { role ->
            SearchAccountResult(
                firstName = account.firstName,
                lastName = account.lastName,
                roles = account.roles().toList(),
            )
        }
    }

    private fun mapSearchResultForNameAndRoles(accountList: List<Account>): List<SearchAccountResult> {
        if (accountList.isEmpty())
            throw LinchpinException(
                ErrorCode.ACCOUNT_NOT_FOUND,
                "No accounts found for the given criteria"
            )

        return accountList.map { account ->
            SearchAccountResult(
                firstName = account.firstName,
                lastName = account.lastName,
                roles = account.roles().map { it }
            )
        }
    }
}
