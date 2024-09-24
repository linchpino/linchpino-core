package com.linchpino.core.service

import com.linchpino.core.dto.CreateInterviewRequest
import com.linchpino.core.dto.ScheduleRequest
import com.linchpino.core.dto.ScheduleResponse
import com.linchpino.core.dto.ScheduleUpdateRequest
import com.linchpino.core.dto.toResponse
import com.linchpino.core.dto.toSchedule
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.repository.ScheduleRepository
import com.linchpino.core.security.email
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val accountRepository: AccountRepository,
    private val mentorTimeSlotRepository: MentorTimeSlotRepository
) {


    fun addSchedule(request: ScheduleRequest, authentication: Authentication): ScheduleResponse {
        val account = accountRepository.findByEmailIgnoreCase(authentication.email()) ?: throw LinchpinException(
            ErrorCode.ACCOUNT_NOT_FOUND,
            "account not found"
        )
        try {
            val schedule = request.toSchedule(account)
            scheduleRepository.save(schedule)
            return schedule.toResponse()
        } catch (ex: DataIntegrityViolationException) {
            throw LinchpinException(ErrorCode.DUPLICATE_SCHEDULE, "account already has schedule")
        }
    }

    fun availableTimeSlot(account: Account, request: CreateInterviewRequest): MentorTimeSlot {
        val validWindow = account.schedule?.timeSlot(request.startTime, request.endTime)
            ?: throw LinchpinException(ErrorCode.INVALID_TIMESLOT, "mentor has no valid window for selected time")
        val timeSlots = mentorTimeSlotRepository.numberOfOverlappingSlots(validWindow.start, validWindow.end)
        if (timeSlots > 0) {
            throw LinchpinException(ErrorCode.TIMESLOT_IS_BOOKED, "there is an active time-slot in $validWindow")
        }

        return MentorTimeSlot().apply {
            this.account = account
            this.fromTime = validWindow.start
            this.toTime = validWindow.end
            this.status = MentorTimeSlotEnum.ALLOCATED
        }.let {
            mentorTimeSlotRepository.save(it)
        }

    }


    fun updateSchedule(authentication: Authentication, request: ScheduleUpdateRequest): ScheduleResponse {
        val account = accountRepository.findByEmailIgnoreCase(authentication.email())
            ?: throw LinchpinException(ErrorCode.ACCOUNT_NOT_FOUND, "account not found")

        val schedule = account.schedule?.update(request)
            ?: throw LinchpinException(ErrorCode.ENTITY_NOT_FOUND, "schedule not found for account: ${account.id}")
        return schedule.toResponse()
    }


    fun deleteSchedule(authentication: Authentication, request: ScheduleUpdateRequest) {
        val account = accountRepository.findByEmailIgnoreCase(authentication.email())
            ?: throw LinchpinException(ErrorCode.ACCOUNT_NOT_FOUND, "account not found")
        account.schedule?.let {
            scheduleRepository.delete(it)
        }
    }
}
