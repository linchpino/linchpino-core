package com.linchpino.core.entity

import jakarta.persistence.*

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
