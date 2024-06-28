package com.linchpino.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(name = "jobseeker_feedback",uniqueConstraints = [UniqueConstraint(columnNames = ["jobseeker_id", "interview_id"])])
class JobSeekerFeedback :AbstractEntity() {

    @Column(name = "jobseeker_id", nullable = false)
    var jobSeekerId: Long? = null

    @Column(name = "INTERVIEW_ID", nullable = false)
    var interviewId: Long? = null

    @Column(name = "satisfaction_status", nullable = false)
    var satisfactionStatus: Int = 0

    @Column(name = "content", columnDefinition = "TEXT")
    var content: String? = null
}
