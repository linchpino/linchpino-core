package com.linchpino.core.dto

import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum

data class CreateAccountRequest(
	val firstName: String,
	val lastName: String,
	val email: String,
	val password: String,
	val type: Int?,
)

data class CreateAccountResult(
	val id:Long,
	val firstName: String,
	val lastName: String,
	val email: String,
	val type: AccountTypeEnum = AccountTypeEnum.UNKNOWN,
	val status: MentorTimeSlotEnum = MentorTimeSlotEnum.UNKNOWN,
)
