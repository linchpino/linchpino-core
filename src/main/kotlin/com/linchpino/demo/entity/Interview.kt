package com.linchpino.demo.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Entity
@Table(name = "INTERVIEW")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class Interview : AbstractEntity() {
    @JoinColumn(name = "JOB_POSITION_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private val jobPositionId: Long = -1

    @JoinColumn(name = "INTERVIEW_TYPE_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private val interviewTypeId: Long = -1

    @JoinColumn(name = "TIME_SLOT_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private val timeSlotId: Long = -1

    @JoinColumn(name = "MENTOR_ACCOUNT_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private val mentorAccountId: Long = -1

    @JoinColumn(name = "JOB_SEEKER_ACCOUNT_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private val jobSeekerAccountId: Long = -1

//    @Enumerated(EnumType.STRING)
//    @Column(name = "STATUS")
//    lateinit var MentorTimeSlotEnum status
}