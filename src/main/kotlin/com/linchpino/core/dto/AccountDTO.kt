package com.linchpino.core.dto

import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.security.PasswordPolicy
import jakarta.validation.constraints.*
import java.time.ZonedDateTime

data class CreateAccountRequest(
    @field:NotBlank(message = "firstname is required") val firstName: String,
    @field:NotBlank(message = "lastname is required") val lastName: String,
    @field:Email(message = "email is not valid") val email: String,
    @field:PasswordPolicy val password: String,
    @field:NotNull(message = "type is required") val type: Int,
)

fun CreateAccountRequest.toAccount():Account{
    val account = Account()
    account.firstName = this.firstName
    account.lastName = this.lastName
    account.email = this.email
    account.password = this.password
    return account
}

data class CreateAccountResult(
    val id: Long,
    val firstName: String,
    val lastName: String,
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

data class ActivateJobSeekerAccountRequest(
    @field:NotBlank(message = "external id is required")val externalId: String,
    @field:NotBlank(message = "firstname is required") val firstName: String,
    @field:NotBlank(message = "lastname is required") val lastName: String,
    @field:PasswordPolicy val password: String
)


data class AccountSummary(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val type: List<AccountTypeEnum>,
    val status: AccountStatusEnum,
    val externalId:String?
)

fun Account.toSummary() = AccountSummary(id!!,firstName,lastName,email,roles().map { it.title },status,externalId)

data class RegisterMentorRequest(
    @field:NotBlank(message = "firstname is required") val firstName: String,
    @field:NotBlank(message = "lastname is required") val lastName: String,
    @field:Email(message = "email is not valid") val email: String,
    @field:PasswordPolicy val password: String,
    @field:NotEmpty(message = "interviewTypeIDs are required") val interviewTypeIDs: List<Long>,
    val detailsOfExpertise:String?,
    @field:Pattern(regexp = "^https?://(www\\.)?linkedin\\.com/in/[a-zA-Z0-9_-]+$", message = "Invalid LinkedIn URL") val linkedInUrl:String?
)

fun RegisterMentorRequest.toAccount(): Account {
    val account = Account()
    account.firstName = this.firstName
    account.lastName = this.lastName
    account.status = AccountStatusEnum.ACTIVATED
    account.email = this.email
    account.linkedInUrl = this.linkedInUrl
    account.detailsOfExpertise = this.detailsOfExpertise
    return account
}

fun Account.toRegisterMentorResult():RegisterMentorResult{
    return RegisterMentorResult(
        this.id,
        this.firstName,
        this.lastName,
        this.email,
        this.interviewTypeIDs(),
        this.detailsOfExpertise,
        this.linkedInUrl
    )
}

data class RegisterMentorResult(
    val id: Long?,
    val firstName: String,
    val lastName: String,
    val email: String,
    val interviewTypeIDs: List<Long>,
    val detailsOfExpertise:String?,
    val linkedInUrl:String?
)
