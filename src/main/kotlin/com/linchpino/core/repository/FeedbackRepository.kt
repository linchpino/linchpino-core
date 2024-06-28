package com.linchpino.core.repository

import com.linchpino.core.entity.JobSeekerFeedback
import org.springframework.data.repository.CrudRepository

interface FeedbackRepository : CrudRepository<JobSeekerFeedback, Long> {

    fun findFirstByInterviewIdAndJobSeekerId(interviewId: Long, jobSeekerId: Long): JobSeekerFeedback?
}
