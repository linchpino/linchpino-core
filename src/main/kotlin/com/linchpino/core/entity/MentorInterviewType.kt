package com.linchpino.core.entity

import jakarta.persistence.*
import lombok.*

@Entity
@Table(name = "MENTOR_INTERVIEW_TYPE")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class MentorInterviewType : AbstractEntity() {
    @JoinColumn(name = "INTERVIEW_TYPE_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    var interviewType: InterviewType? = null
}
