package com.linchpino.core.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "INTERVIEW")
class Interview : AbstractEntity() {
    @JoinColumn(name = "JOB_POSITION_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val jobPosition: JobPosition? = null

    @JoinColumn(name = "INTERVIEW_TYPE_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val interviewType: InterviewType? = null

    @JoinColumn(name = "TIME_SLOT_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val timeSlot: MentorTimeSlot? = null

    @JoinColumn(name = "MENTOR_ACCOUNT_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val mentorAccount: Account? = null

    @JoinColumn(name = "JOB_SEEKER_ACCOUNT_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val jobSeekerAccount: Account? = null

}