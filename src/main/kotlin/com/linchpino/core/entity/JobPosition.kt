package com.linchpino.core.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "JOB_POSITION")
class JobPosition : AbstractEntity() {
    @Column(name = "TITLE")
    lateinit var title: String

    @ManyToMany(
        cascade = [CascadeType.PERSIST, CascadeType.MERGE
        ]
    )
    @JoinTable(
        name = "job_position_interview_type",
        joinColumns = [JoinColumn(name = "job_position_id")],
        inverseJoinColumns = [JoinColumn(name = "interview_type_id")]
    )
    private val interviewTypes = mutableSetOf<InterviewType>()

    fun addInterviewType(interviewType: InterviewType) {
        interviewTypes.add(interviewType)
        interviewType.jobPositions.add(this)
    }

    fun removeInterviewType(interviewType: InterviewType) {
        interviewTypes.remove(interviewType)
        interviewType.jobPositions.remove(this)
    }

    fun interviewTypes() = interviewTypes.toSet()
}
