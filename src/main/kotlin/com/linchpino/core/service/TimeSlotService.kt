package com.linchpino.core.service

import com.linchpino.core.dto.AddTimeSlotsRequest
import com.linchpino.core.dto.toAvailableMentorTimeSlot
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.repository.findReferenceById
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class TimeSlotService(
    private val accountRepository: AccountRepository,
    private val repository: MentorTimeSlotRepository,
    private val mentorTimeSlotRepository: MentorTimeSlotRepository
) {


    fun addTimeSlots(request: AddTimeSlotsRequest) {
        val account = accountRepository.findReferenceById(request.mentorId)
        account.roles()
            .firstOrNull { it.title == AccountTypeEnum.MENTOR }
            ?: throw LinchpinException(ErrorCode.INVALID_ACCOUNT_ROLE, "account does not have mentor role")

        val mentorTimeSlots = request.timeSlots
            .onEach { it.validate() }
            .map { it.toAvailableMentorTimeSlot(account) }
        repository.saveAll(mentorTimeSlots)
    }

    fun updateTimeSlotStatus(timeSlot: MentorTimeSlot, mentorTimeSlotEnum: MentorTimeSlotEnum) {

        timeSlot.status = mentorTimeSlotEnum
        mentorTimeSlotRepository.save(timeSlot)
    }
}
