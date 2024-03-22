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
@Table(name = "INTERVIEW")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class Interview : AbstractEntity() {
	@JoinColumn(name = "JOB_POSITION_ID", referencedColumnName = "ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private val jobPosition: JobPosition? = null

	@JoinColumn(name = "INTERVIEW_TYPE_ID", referencedColumnName = "ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private val interviewType: InterviewType? = null

	@JoinColumn(name = "TIME_SLOT_ID", referencedColumnName = "ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private val timeSlot: MentorTimeSlot? = null

	@JoinColumn(name = "MENTOR_ACCOUNT_ID", referencedColumnName = "ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private val mentorAccount: Account? = null

	@JoinColumn(name = "JOB_SEEKER_ACCOUNT_ID", referencedColumnName = "ID", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private val jobSeekerAccount: Account? = null

}
