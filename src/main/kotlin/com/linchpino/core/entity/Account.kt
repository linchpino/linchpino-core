package com.linchpino.core.entity

import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table


@Table(name = "ACCOUNT")
@Entity
class Account : AbstractEntity() {
    @Column(name = "FIRST_NAME")
    lateinit var firstName: String

    @Column(name = "LAST_NAME")
    lateinit var lastName: String

    @Column(name = "email")
    lateinit var email: String

    @Column(name = "password")
    lateinit var password: String //encrypt password!

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    var type: AccountTypeEnum = AccountTypeEnum.UNKNOWN

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    var status: MentorTimeSlotEnum = MentorTimeSlotEnum.UNKNOWN
}
