package com.linchpino.core.dto

import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import lombok.Data

@Data
class AccountDto {
    lateinit var firstName: String
    lateinit var lastName: String
    lateinit var email: String
    lateinit var password: String
    var type: AccountTypeEnum = AccountTypeEnum.UNKNOWN
    var status: MentorTimeSlotEnum = MentorTimeSlotEnum.UNKNOWN
}
