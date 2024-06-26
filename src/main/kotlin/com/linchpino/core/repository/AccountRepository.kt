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

    fun findByEmailIgnoreCase(email: String): Account?

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
    JOIN a.roles role
    JOIN
        a.interviewTypes it
    JOIN
        MentorTimeSlot mts ON mts.account.id = a.id
    WHERE
        role.title = :type
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
                AND mentorTimeSlot.status = :status
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

    @Query("""
        select a from Account a join a.roles role where a.externalId = :externalId and role.title = :type
    """)
    fun findByExternalId(externalId:String,type: AccountTypeEnum):Account?

}

