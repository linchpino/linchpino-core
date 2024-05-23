package com.linchpino.core.repository

import com.linchpino.core.dto.InterviewListResponse
import com.linchpino.core.entity.Interview
import com.linchpino.core.enums.MentorTimeSlotEnum
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface InterviewRepository : JpaRepository<Interview, Long> {
    @Query(
        "SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END " +
            "FROM Interview i " +
            "WHERE i.timeSlot.id = :timeSlotId"
    )
    fun isTimeSlotIdExist(timeSlotId: Long): Boolean

    @Query(
        """
         select NEW com.linchpino.core.dto.InterviewListResponse(
            i.jobSeekerAccount.id,
            concat(i.jobSeekerAccount.firstName, ' ', i.jobSeekerAccount.lastName),
            i.timeSlot.fromTime,
            i.timeSlot.toTime,
            i.interviewType.name
            )
        from Interview i
        where i.mentorAccount.email = :email
        and i.timeSlot.status = :status
        and i.timeSlot.fromTime > CURRENT_TIMESTAMP
    """
    )
    fun findUpcomingInterviews(email: String,page: Pageable,status: MentorTimeSlotEnum = MentorTimeSlotEnum.ALLOCATED): Page<InterviewListResponse>

    @Query(
        """
         select NEW com.linchpino.core.dto.InterviewListResponse(
            i.jobSeekerAccount.id,
            concat(i.jobSeekerAccount.firstName, ' ', i.jobSeekerAccount.lastName),
            i.timeSlot.fromTime,
            i.timeSlot.toTime,
            i.interviewType.name
            )
        from Interview i
        where i.mentorAccount.email = :email
        and i.timeSlot.status = :status
        and i.timeSlot.fromTime <= CURRENT_TIMESTAMP
    """
    )
    fun findPastInterviews(email: String,page: Pageable,status: MentorTimeSlotEnum = MentorTimeSlotEnum.ALLOCATED): Page<InterviewListResponse>
}
