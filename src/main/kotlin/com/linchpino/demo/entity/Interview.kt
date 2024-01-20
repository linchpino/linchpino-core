package com.linchpino.demo.entity

import jakarta.persistence.*
import lombok.*

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
    private val jobPositionId: Long? = null

    @JoinColumn(name = "INTERVIEW_TYPE_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private val interviewTypeId: Long? = null

    @JoinColumn(name = "TIME_SLOT_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private val timeSlotId: Long? = null

    @JoinColumn(name = "MENTOR_ACCOUNT_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private val mentorAccountId: Long? = null

    @JoinColumn(name = "JOB_SEEKER_ACCOUNT_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private val jobSeekerAccountId: Long? = null

//    @Enumerated(EnumType.STRING)
//    @Column(name = "STATUS")
//    lateinit var MentorTimeSlotEnum status
}