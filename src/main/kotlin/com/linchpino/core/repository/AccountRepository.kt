package com.linchpino.core.repository

import com.linchpino.core.dto.MentorWithClosestTimeSlot
import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
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
        AND mts.fromTime BETWEEN :from AND :to
        AND it.id = :interviewTypeId
        AND mts.fromTime = (
            SELECT
                MIN(mentorTimeSlot.fromTime)
            FROM
                MentorTimeSlot mentorTimeSlot
            WHERE
                mentorTimeSlot.account.id = a.id
                AND mentorTimeSlot.fromTime BETWEEN :from AND :to
                AND mentorTimeSlot.status = 1
        )
    """
    )
    fun closestMentorTimeSlots(
        from: ZonedDateTime,
        to: ZonedDateTime,
        interviewTypeId: Long,
        type:AccountTypeEnum = AccountTypeEnum.MENTOR,
        status: MentorTimeSlotEnum = MentorTimeSlotEnum.AVAILABLE
    ): List<MentorWithClosestTimeSlot>

    fun findByEmailIgnoreCase(email:String):Account?
    @Query("""
        select a from Account a where a.externalId = :externalId and a.type = :type
    """)
    fun findByExternalId(externalId:String,type: AccountTypeEnum):Account?

}
