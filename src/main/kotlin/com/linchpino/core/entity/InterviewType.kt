package com.linchpino.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "INTERVIEW_TYPE")
class InterviewType : AbstractEntity(){
    @Column(name = "NAME")
    lateinit var name: String

	@ManyToMany(mappedBy = "interviewTypes")
	val jobPositions = mutableSetOf<JobPosition>()
}