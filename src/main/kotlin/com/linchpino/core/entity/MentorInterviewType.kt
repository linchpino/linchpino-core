package com.linchpino.core.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "MENTOR_INTERVIEW_TYPE")
class MentorInterviewType : AbstractEntity(){
    @JoinColumn(name = "INTERVIEW_TYPE_ID", referencedColumnName = "ID", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    var interviewType: InterviewType? = null
}