package com.linchpino.core.service

import com.linchpino.core.dto.AccountSummary
import com.linchpino.core.dto.ActivateJobSeekerAccountRequest
import com.linchpino.core.dto.AddProfileImageResponse
import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.MentorWithClosestSchedule
import com.linchpino.core.dto.MentorWithClosestTimeSlot
import com.linchpino.core.dto.RegisterMentorRequest
import com.linchpino.core.dto.RegisterMentorResult
import com.linchpino.core.dto.ResetAccountPasswordRequest
import com.linchpino.core.dto.ResetPasswordRequest
import com.linchpino.core.dto.SaveAccountRequest
import com.linchpino.core.dto.SearchAccountResult
import com.linchpino.core.dto.UpdateAccountRequest
import com.linchpino.core.dto.UpdateAccountRequestByAdmin
import com.linchpino.core.dto.UpdateProfileRequest
import com.linchpino.core.dto.ValidWindow
import com.linchpino.core.dto.hasOverlapWith
import com.linchpino.core.dto.toCreateAccountResult
import com.linchpino.core.dto.toIBAN
import com.linchpino.core.dto.toRegisterMentorResult
import com.linchpino.core.dto.toSummary
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.repository.RoleRepository
import com.linchpino.core.repository.findReferenceById
import com.linchpino.core.security.email
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

@Service
@Transactional
class AccountService(
    private val repository: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val interviewTypeRepository: InterviewTypeRepository,
    private val roleRepository: RoleRepository,
    private val emailService: EmailService,
    private val storageService: StorageService,
    private val linkedInService: LinkedInService,
    private val paymentService: PaymentService,
    private val mentorTimeSlotRepository: MentorTimeSlotRepository
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


    fun findMentorsWithClosestScheduleBy(date: ZonedDateTime, interviewTypeId: Long): List<MentorWithClosestSchedule> {
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        val currentDate = now.toLocalDate()
        val selectedDate = date.toLocalDate()

        val selectedTime = when {
            currentDate.isEqual(selectedDate) -> now
            else -> date.withZoneSameInstant(ZoneOffset.UTC).with(LocalTime.MIDNIGHT)
        }

        val accountsWithValidWindow = repository.closestMentorSchedule(selectedTime, interviewTypeId)
            .map { it to it.schedule?.doesMatchesSelectedDay(selectedTime) }
            .filter { it.second != null }

        val accountIds = accountsWithValidWindow.map { it.first.id!! }
        val bookedTimeSlots =
            mentorTimeSlotRepository.findByAccountIdsAndDate(accountIds, selectedDate).groupBy { it.account?.id }

        val mentors = accountsWithValidWindow
            .filter { (account, validWindow) ->
                accountsWithBookedTimeSlot(bookedTimeSlots[account.id] ?: emptyList(), validWindow!!)
            }.map {
                MentorWithClosestSchedule(
                    it.first.id,
                    it.first.firstName,
                    it.first.lastName,
                    it.second,
                    it.first.email,
                    it.first.avatar
                )
            }
        return mentors
    }

    private fun accountsWithBookedTimeSlot(
        bookedTimeSlots: List<MentorTimeSlot>,
        validWindow: ValidWindow
    ): Boolean {
        if (bookedTimeSlots.isEmpty())
            return true
        return bookedTimeSlots.any { timeSlot ->
            !ValidWindow(timeSlot.fromTime, timeSlot.toTime).hasOverlapWith(validWindow)
        }
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
            request.linkedInUrl,
            request.paymentMethodRequest,
            request.iban?.toIBAN()
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
            iban = request.iban?.number()
        }
        try {
            paymentService.savePaymentMethod(request.paymentMethodRequest, account)
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

    @Transactional(readOnly = true)
    fun searchAccountByNameOrRole(name: String?, role: Int?, page: Pageable): Page<SearchAccountResult> {
        val accountType = AccountTypeEnum.entries.firstOrNull { it.value == role }
        return repository.searchByNameOrRole(name, accountType, page)
            .map {
                SearchAccountResult(
                    it.id,
                    it.firstName,
                    it.lastName,
                    it.roles().map { r -> r.title.name },
                    it.email,
                    it.avatar,
                    it.status
                )
            }
    }


    fun uploadProfileImage(file: MultipartFile, authentication: Authentication): AddProfileImageResponse {
        val account = repository.findByEmailIgnoreCase(authentication.email())
            ?: throw LinchpinException(ErrorCode.ACCOUNT_NOT_FOUND, "account not found")
        val fileName = storageService.uploadProfileImage(account, file)
        account.avatar = fileName
        return AddProfileImageResponse(fileName)
    }

    fun profile(authentication: Authentication): AccountSummary {
        val account = repository.findByEmailIgnoreCase(authentication.email())
        if (authentication is BearerTokenAuthentication && account == null) {
            val userInfo = linkedInService.userInfo((authentication.credentials as OAuth2AccessToken).tokenValue)
            return saveAccount(
                SaveAccountRequest(
                    userInfo.firstName,
                    userInfo.lastName,
                    userInfo.email,
                    null,
                    listOf(AccountTypeEnum.JOB_SEEKER.value)
                )
            ).toSummary()
        } else if (account != null) {
            return account.toSummary()
        }
        throw LinchpinException(ErrorCode.ACCOUNT_NOT_FOUND, "account not found")
    }


    fun changePassword(authentication: Authentication, resetPassword: ResetPasswordRequest) {
        val account = repository.findByEmailIgnoreCase(authentication.email())
            ?: throw LinchpinException(ErrorCode.ACCOUNT_NOT_FOUND, "account not found")
        if (!passwordEncoder.matches(resetPassword.currentPassword, account.password)) {
            throw LinchpinException(ErrorCode.INVALID_PASSWORD, "currentPassword does not match")
        }
        account.password = passwordEncoder.encode(resetPassword.newPassword)
        repository.save(account)
    }

    fun resetAccountPasswordByAdmin(request: ResetAccountPasswordRequest) {
        val account = repository.findReferenceById(request.accountId)
        account.password = passwordEncoder.encode(request.newPassword)
        repository.save(account)
    }

    fun updateAccountByAdmin(request: UpdateAccountRequestByAdmin) {
        val account = repository.findReferenceById(request.accountId)
        account.roles().forEach { account.removeRole(it) }
        roleRepository.findAll()
            .filter { request.roles.contains(it.id) }
            .forEach { account.addRole(it) }

        request.status?.let {
            account.status = AccountStatusEnum.entries.first { status -> status.value == it }
        }
        repository.save(account)
    }

    fun updateProfile(authentication: Authentication, request: UpdateProfileRequest): AccountSummary {
        val account = repository.findByEmailIgnoreCase(authentication.email())
            ?: throw LinchpinException(ErrorCode.ACCOUNT_NOT_FOUND, "account not found")
        var paymentMethod = paymentService.findByIdOrNull(account.id!!)
        with(request) {
            firstName?.let { account.firstName = it }
            lastName?.let { account.lastName = it }
            detailsOfExpertise?.let { account.detailsOfExpertise = it }
            iban?.let { account.iban = it }
            linkedInUrl?.let { account.linkedInUrl = it }
            paymentMethodRequest?.let { paymentRequest ->
                if (paymentMethod == null) {
                    paymentMethod = paymentService.savePaymentMethod(paymentRequest, account)
                } else {
                    with(paymentMethod!!) {
                        type = paymentRequest.type!!
                        maxPayment = paymentRequest.maxPayment
                        minPayment = paymentRequest.minPayment
                        fixRate = paymentRequest.fixRate
                        paymentService.update(this)
                    }
                }
            }
        }
        repository.save(account)
        return account.toSummary(paymentMethod)
    }
}
