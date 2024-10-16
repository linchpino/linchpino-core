package com.linchpino.core.repository

import com.linchpino.core.dto.MentorWithClosestTimeSlot
import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
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
        type: AccountTypeEnum = AccountTypeEnum.MENTOR,
        status: MentorTimeSlotEnum = MentorTimeSlotEnum.AVAILABLE
    ): List<MentorWithClosestTimeSlot>

    @Query("""
        select a from Account a
        join
            a.roles role
        join
            a.interviewTypes it
        join
            a.schedule s
        where
            role.title = :type
            and it.id = :interviewTypeId
            and :from BETWEEN s.startTime and s.endTime
    """)
    fun closestMentorSchedule( from: ZonedDateTime,
                               interviewTypeId: Long,
                               type: AccountTypeEnum = AccountTypeEnum.MENTOR):List<Account>


    @Query(
        """
        select a from Account a join a.roles role where a.externalId = :externalId and role.title = :type
    """
    )
    fun findByExternalId(externalId: String, type: AccountTypeEnum): Account?

    @Query(
        """
        SELECT DISTINCT a
        FROM Account a
        JOIN a.roles r
        WHERE
        (:type IS NULL OR r.title = :type) AND
        (LOWER(a.firstName) LIKE CONCAT('%',LOWER(COALESCE(:name, a.firstName)),'%') OR LOWER(a.lastName) LIKE CONCAT('%',LOWER(COALESCE(:name, a.lastName)),'%'))
        """
    )
    fun searchByNameOrRole(name: String?, type: AccountTypeEnum?, page: Pageable): Page<Account>

}
