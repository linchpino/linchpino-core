package com.linchpino.core.entity

import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.enums.converters.AccountStatusEnumConverter
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Table(name = "ACCOUNT")
@Entity
class Account : AbstractEntity() {

    @Column(name = "FIRST_NAME")
    var firstName: String? = null

    @Column(name = "LAST_NAME")
    var lastName: String? = null

    @Column(name = "email")
    lateinit var email: String

    @Column(name = "password")
    var password: String? = null

    @Convert(converter = AccountStatusEnumConverter::class)
    @Column(name = "STATUS")
    var status: AccountStatusEnum = AccountStatusEnum.ACTIVATED

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "account_interview_type",
        joinColumns = [JoinColumn(name = "account_id")],
        inverseJoinColumns = [JoinColumn(name = "interview_type_id")]
    )
    private val interviewTypes = mutableSetOf<InterviewType>()

    @Column(name = "external_id")
    var externalId: String? = null

    @Column(name = "expertise", columnDefinition = "TEXT")
    var detailsOfExpertise:String? = null

    @Column(name = "linkedin_url")
    var linkedInUrl:String? = null

    @Column(name = "avatar")
    var avatar: String? = null

    @OneToOne(mappedBy = "account", cascade = [CascadeType.ALL])
    var schedule: Schedule? = null

    @Column(name = "iban")
    var iban: String? = null

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
    private val roles = mutableSetOf<Role>()

    fun addRole(role: Role) {
        roles.add(role)
        role.accounts.add(this)
    }

    fun removeRole(role: Role) {
        roles.remove(role)
        role.accounts.remove(this)
    }

    fun roles() = this.roles.toSet()

    fun interviewTypes() = this.interviewTypes.toSet()

    fun interviewTypeIDs() = this.interviewTypes.map { it.id!! }.toList()
}
