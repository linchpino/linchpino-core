package com.linchpino.core.dto

import com.linchpino.core.enums.AccountStatus
import com.linchpino.core.enums.AccountTypeEnum
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

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
    val type: AccountTypeEnum = AccountTypeEnum.UNKNOWN,
    val status: AccountStatus = AccountStatus.DEACTIVATED,
)
