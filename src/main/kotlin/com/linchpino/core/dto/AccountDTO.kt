package com.linchpino.core.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.PaymentMethodType
import com.linchpino.core.security.PasswordPolicy
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.ZonedDateTime

data class CreateAccountRequest(
    @field:NotBlank(message = "firstname is required") val firstName: String?,
    @field:NotBlank(message = "lastname is required") val lastName: String?,
    @field:Email(message = "email is not valid") val email: String,
    @field:PasswordPolicy val password: String?,
    @field:NotNull(message = "type is required") val type: Int,
    val status: AccountStatusEnum = AccountStatusEnum.ACTIVATED
)

data class SaveAccountRequest(
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val plainTextPassword: String?,
    val roles: List<Int> = listOf(),
    val status: AccountStatusEnum = AccountStatusEnum.ACTIVATED,
    val interviewTypeIDs: List<Long> = listOf(),
    val detailsOfExpertise: String? = null,
    val linkedInUrl: String? = null,
    val paymentMethodRequest: PaymentMethodRequest = PaymentMethodRequest(PaymentMethodType.FREE),
    val iban: IBAN? = null,
)

data class UpdateAccountRequest(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val plainTextPassword: String?,
    val roles: List<Int> = listOf(),
    val status: AccountStatusEnum?,
    val interviewTypeIDs: List<Long> = listOf(),
    val detailsOfExpertise: String? = null,
    val linkedInUrl: String? = null,
    val externalId: String?
)

data class CreateAccountResult(
    val id: Long,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val type: List<AccountTypeEnum>,
    val status: AccountStatusEnum = AccountStatusEnum.ACTIVATED,
)

fun Account.toCreateAccountResult(): CreateAccountResult {
    return CreateAccountResult(id!!, firstName, lastName, email, roles().map { it.title }, status)
}

data class MentorWithClosestTimeSlot(
    val mentorId: Long,
    val mentorFirstName: String,
    val mentorLastName: String,
    val timeSlotId: Long,
    val from: ZonedDateTime,
    val to: ZonedDateTime
)

data class MentorWithClosestSchedule(
    val mentorId: Long?,
    val mentorFirstName: String?,
    val mentorLastName: String?,
    val validWindow: ValidWindow?,
    val email:String,
    val avatar: String?
)

data class ActivateJobSeekerAccountRequest(
    @field:NotBlank(message = "external id is required") val externalId: String,
    @field:NotBlank(message = "firstname is required") val firstName: String,
    @field:NotBlank(message = "lastname is required") val lastName: String,
    @field:PasswordPolicy val password: String
)


data class AccountSummary(
    val id: Long?,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val type: List<AccountTypeEnum>,
    val status: AccountStatusEnum,
    val externalId: String?,
    val avatar: String? = null,
    val detailsOfExpertise: String? = null,
    val linkedInUrl: String? = null,
    val iban: String? = null,
    val schedule: ScheduleResponse? = null
)

fun Account.toSummary() = AccountSummary(
    id,
    firstName,
    lastName,
    email,
    roles().map { it.title },
    status,
    externalId,
    avatar,
    detailsOfExpertise,
    linkedInUrl,
    iban,
    schedule?.toResponse()
)


data class RegisterMentorRequest(
    @field:NotBlank(message = "firstname is required") val firstName: String,
    @field:NotBlank(message = "lastname is required") val lastName: String,
    @field:Email(message = "email is not valid") val email: String,
    @field:PasswordPolicy val password: String,
    @field:NotEmpty(message = "interviewTypeIDs are required") val interviewTypeIDs: List<Long>,
    val detailsOfExpertise: String?,
    @field:Pattern(
        regexp = "^https?://(www\\.)?linkedin\\.com/in/[a-zA-Z0-9_-]+$",
        message = "Invalid LinkedIn URL"
    ) val linkedInUrl: String?,
    @field:NotNull(message = "payment method must not be null") val paymentMethodRequest: PaymentMethodRequest,
    @field:NotBlank(message = "iban must not be null") @field:ValidIBAN val iban: String?
)

fun Account.toRegisterMentorResult(): RegisterMentorResult {
    return RegisterMentorResult(
        this.id,
        this.firstName,
        this.lastName,
        this.email,
        this.interviewTypeIDs(),
        this.detailsOfExpertise,
        this.linkedInUrl,
        this.iban
    )
}

data class RegisterMentorResult(
    val id: Long?,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val interviewTypeIDs: List<Long>,
    val detailsOfExpertise: String?,
    val linkedInUrl: String?,
    val iban: String?
)

data class SearchAccountResult(
    val firstName: String?,
    val lastName: String?,
    val roles: List<String>,
    val email: String,
    val avatar: String?
)

data class AddProfileImageResponse(val imageUrl: String)

data class LinkedInUserInfoResponse(
    val email: String,
    @JsonProperty("given_name") val firstName: String?,
    @JsonProperty("family_name") val lastName: String?
)

data class ResetPasswordRequest(@field:NotNull(message = "current password must be provided") val currentPassword: String,@field:PasswordPolicy val newPassword: String)

data class ResetAccountPasswordRequest(@field:NotNull(message = "account id must not be null") val accountId:Long,@field:PasswordPolicy val newPassword: String)
