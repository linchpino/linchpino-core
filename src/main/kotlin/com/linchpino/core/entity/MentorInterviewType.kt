package com.linchpino.core.entity

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
