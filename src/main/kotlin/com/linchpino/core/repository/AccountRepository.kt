package com.linchpino.core.repository

import com.linchpino.core.dto.MentorWithClosestTimeSlot
import com.linchpino.core.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime

@Repository
interface AccountRepository : JpaRepository<Account, Long>{

    @Query(
        """
    SELECT NEW com.linchpino.core.dto.MentorWithClosestTimeSlot(
            a.id ,
            a.firstName,
            a.lastName,
            mts.id,
            mts.fromTime,
            mts.toTime
        )
    FROM
        Account a
    JOIN
        a.interviewTypes it
    JOIN
        MentorTimeSlot mts ON mts.account.id = a.id
    WHERE
        a.type = 2
        AND cast(mts.fromTime as localdate) = cast(:dateParam as localdate )
        AND it.id = :interviewTypeId
        AND mts.fromTime = (
            SELECT
                MIN(mentorTimeSlot.fromTime)
            FROM
                MentorTimeSlot mentorTimeSlot
            WHERE
                mentorTimeSlot.account.id = a.id
                AND cast(mentorTimeSlot.fromTime as localdate) = cast(:dateParam as localdate)
                AND mentorTimeSlot.status = 1
        )
    """
    )
    fun closestMentorTimeSlots(
        dateParam: ZonedDateTime,
        interviewTypeId: Long,
    ): List<MentorWithClosestTimeSlot>

}
