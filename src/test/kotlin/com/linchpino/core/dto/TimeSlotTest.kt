package com.linchpino.core.dto

import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import java.time.ZonedDateTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TimeSlotTest{

    @Test
    fun `test timeslot dto throws exception if endTime is before startTime`(){
        val exception = Assertions.assertThrows(LinchpinException::class.java){
            TimeSlot(ZonedDateTime.now().plusMinutes(10), ZonedDateTime.now()).validate()
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.INVALID_TIMESLOT)
    }
}
