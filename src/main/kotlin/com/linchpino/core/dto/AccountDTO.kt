package com.linchpino.core.dto

import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime

data class CreateAccountRequest(
    @field:NotBlank(message = "firstname is required") val firstName: String,
    @field:NotBlank(message = "lastname is required") val lastName: String,
    @field:Email(message = "email is not valid") val email: String,
    @field:NotBlank(message = "password is required") val password: String,
    @field:NotNull(message = "type is required") val type: Int,
)

data class CreateAccountResult(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val type: AccountTypeEnum = AccountTypeEnum.GUEST,
    val status: AccountStatusEnum = AccountStatusEnum.DEACTIVATED,
)

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
    @field:NotBlank(message = "password is required") val password: String
)


data class AccountSummary(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val type: AccountTypeEnum,
    val status: AccountStatusEnum,
    val externalId:String?
)

fun Account.toSummary() = AccountSummary(id!!,firstName,lastName,email,type,status,externalId)

data class RegisterMentorRequest(
    @field:NotBlank(message = "firstname is required") val firstName: String,
    @field:NotBlank(message = "lastname is required") val lastName: String,
    @field:Email(message = "email is not valid") val email: String,
    @field:NotBlank(message = "password is required") val password: String,
    @field:NotNull(message = "type is required") val type: Int,
    val interviewTypeIDs: List<Long>,
    val detailsOfExpertise:String,
    val linkedInUrl:String
)

fun RegisterMentorRequest.toAccount(): Account {
    val account = Account()
    account.firstName = this.firstName
    account.lastName = this.lastName
    account.status = AccountStatusEnum.ACTIVATED
    account.email = this.email
    account.type = AccountTypeEnum.MENTOR
    account.linkedInUrl = this.linkedInUrl
    account.detailsOfExpertise = this.detailsOfExpertise
    return account
}

fun Account.toRegisterMentorResult():RegisterMentorResult{
    return RegisterMentorResult(
        this.id!!,
        this.firstName,
        this.lastName,
        this.email,
        this.interviewTypeIDs(),
        this.detailsOfExpertise,
        this.linkedInUrl
    )
}

data class RegisterMentorResult(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val interviewTypeIDs: List<Long>,
    val detailsOfExpertise:String?,
    val linkedInUrl:String?
)
