package com.linchpino.core.entity

import com.linchpino.core.enums.AccountStatus
import com.linchpino.core.enums.AccountTypeEnum
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
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
    var status: AccountStatus = AccountStatus.DEACTIVATED

    @ManyToMany(
        cascade = [CascadeType.PERSIST, CascadeType.MERGE]
    )
    @JoinTable(
        name = "account_interview_type",
        joinColumns = [JoinColumn(name = "account_id")],
        inverseJoinColumns = [JoinColumn(name = "interview_type_id")]
    )
    private val interviewTypes = mutableSetOf<InterviewType>()

    fun addInterviewType(interviewType: InterviewType) {
        interviewTypes.add(interviewType)
        interviewType.accounts.add(this)
    }

    fun removeInterviewType(interviewType: InterviewType) {
        interviewTypes.remove(interviewType)
        interviewType.accounts.remove(this)
    }
}
