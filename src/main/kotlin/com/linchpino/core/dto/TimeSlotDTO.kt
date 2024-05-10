package com.linchpino.core.dto

import com.linchpino.core.entity.Account
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime

data class TimeSlot(@field:NotNull(message = "start time must not be null") val startTime: ZonedDateTime,@field:NotNull(message = "end time must not be null") val endTime: ZonedDateTime){
    init {
        if (startTime.isAfter(endTime)) {
            throw LinchpinException(ErrorCode.INVALID_TIMESLOT,"invalid time slot, start is after end")
        }
    }
}
data class AddTimeSlotsRequest(@field:NotNull(message = "mentorId must not be null") val mentorId: Long,@field:NotEmpty(message = "time slots must be provided") val timeSlots: List<TimeSlot>)


fun TimeSlot.toAvailableMentorTimeSlot(account:Account):MentorTimeSlot {
    val mentorTimeSlot = MentorTimeSlot()
    mentorTimeSlot.fromTime = this.startTime
    mentorTimeSlot.toTime = this.endTime
    mentorTimeSlot.account = account
    mentorTimeSlot.status = MentorTimeSlotEnum.AVAILABLE
    return mentorTimeSlot
}
