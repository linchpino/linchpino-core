package com.linchpino.core.entity

import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.converters.AccountStatusEnumConverter
import com.linchpino.core.enums.converters.AccountTypeEnumConverter
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
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
    lateinit var password: String

    @Convert(converter = AccountTypeEnumConverter::class)
    @Column(name = "TYPE")
    var type: AccountTypeEnum = AccountTypeEnum.GUEST

    @Convert(converter = AccountStatusEnumConverter::class)
    @Column(name = "STATUS")
    var status: AccountStatusEnum = AccountStatusEnum.DEACTIVATED

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
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

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "account_role",
        joinColumns = [JoinColumn(name = "account_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles = mutableSetOf<Role>()

}
