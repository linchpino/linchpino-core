package com.linchpino.core.repository

import com.linchpino.core.dto.InterviewListResponse
import com.linchpino.core.entity.Interview
import com.linchpino.core.enums.MentorTimeSlotEnum
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Repository
interface InterviewRepository : JpaRepository<Interview, Long> {

    @Query(
        """
         select NEW com.linchpino.core.dto.InterviewListResponse(
            i.id,
            i.jobSeekerAccount.id,
            concat(coalesce(i.jobSeekerAccount.firstName, ''), ' ', coalesce(i.jobSeekerAccount.lastName, '')),
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
    fun findUpcomingInterviews(
        email: String,
        page: Pageable,
        status: MentorTimeSlotEnum = MentorTimeSlotEnum.ALLOCATED
    ): Page<InterviewListResponse>

    @Query(
        """
         select NEW com.linchpino.core.dto.InterviewListResponse(
            i.id,
            i.jobSeekerAccount.id,
            concat(coalesce(i.jobSeekerAccount.firstName, ''), ' ', coalesce(i.jobSeekerAccount.lastName, '')),
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
    fun findPastInterviews(
        email: String,
        page: Pageable,
        status: MentorTimeSlotEnum = MentorTimeSlotEnum.ALLOCATED
    ): Page<InterviewListResponse>


    @Query(
        """
         select NEW com.linchpino.core.dto.InterviewListResponse(
            i.id,
            i.mentorAccount.id,
            concat(coalesce(i.mentorAccount.firstName, ''), ' ', coalesce(i.mentorAccount.lastName, '')),
            i.timeSlot.fromTime,
            i.timeSlot.toTime,
            i.interviewType.name
            )
        from Interview i
        where i.jobSeekerAccount.email = :email
        and i.timeSlot.status = :status
        and i.timeSlot.fromTime > CURRENT_TIMESTAMP
    """
    )
    fun findJobSeekerUpcomingInterviews(
        email: String,
        page: Pageable,
        status: MentorTimeSlotEnum = MentorTimeSlotEnum.ALLOCATED
    ): Page<InterviewListResponse>

    @Query(
        """
         select NEW com.linchpino.core.dto.InterviewListResponse(
            i.id,
            i.mentorAccount.id,
            concat(coalesce(i.mentorAccount.firstName, ''), ' ', coalesce(i.mentorAccount.lastName, '')),
            i.timeSlot.fromTime,
            i.timeSlot.toTime,
            i.interviewType.name
            )
        from Interview i
        where i.jobSeekerAccount.email = :email
        and i.timeSlot.status = :status
        and i.timeSlot.fromTime <= CURRENT_TIMESTAMP
    """
    )
    fun findJobSeekerPastInterviews(
        email: String,
        page: Pageable,
        status: MentorTimeSlotEnum = MentorTimeSlotEnum.ALLOCATED
    ): Page<InterviewListResponse>


    @Query(
        """
        from Interview i where i.id = :id and (i.jobSeekerAccount.email = :email or i.mentorAccount.email = :email)
    """
    )
    fun findByInterviewIdAndAccountEmail(id: Long, email: String): Interview?

    @Query("""
        SELECT i FROM Interview i WHERE i.timeSlot.fromTime BETWEEN :start AND :end
    """)
    fun findInterviewsWithin(@Param("start") start: ZonedDateTime,
                             @Param("end") end: ZonedDateTime): List<Interview>
}
