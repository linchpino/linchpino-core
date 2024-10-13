package com.linchpino.core.repository

import com.linchpino.core.entity.MentorTimeSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.ZonedDateTime

@Repository
interface MentorTimeSlotRepository : JpaRepository<MentorTimeSlot, Long> {

    @Query(
        """
        SELECT COUNT(mts.id)
        FROM MentorTimeSlot mts
        WHERE mts.fromTime BETWEEN :start AND :end
        OR mts.toTime BETWEEN :start AND :end
    """
    )
    fun numberOfOverlappingSlots(
        start: ZonedDateTime,
        end: ZonedDateTime,
    ): Long


    @Query(
        """
        SELECT mts
        FROM MentorTimeSlot mts
        WHERE mts.account.id IN :accountIds
        AND FUNCTION('DATE', mts.fromTime) = :date
        AND FUNCTION('DATE', mts.toTime) = :date
    """
    )
    fun findByAccountIdsAndDate(
        @Param("accountIds") accountIds: List<Long>,
        @Param("date") date: LocalDate
    ): List<MentorTimeSlot>
}
