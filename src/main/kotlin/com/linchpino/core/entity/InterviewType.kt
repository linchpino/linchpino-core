package com.linchpino.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(name = "INTERVIEW_TYPE",  uniqueConstraints = [
    UniqueConstraint(name = "uc_interviewtype_name", columnNames = ["NAME"])
])
class InterviewType : AbstractEntity() {
    @Column(name = "NAME")
    lateinit var name: String

    @ManyToMany(mappedBy = "interviewTypes")
    val jobPositions = mutableSetOf<JobPosition>()

    @ManyToMany(mappedBy = "interviewTypes")
    val accounts = mutableSetOf<Account>()
}
