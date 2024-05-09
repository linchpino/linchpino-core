package com.linchpino.core.service

import com.linchpino.core.dto.AddTimeSlotsRequest
import com.linchpino.core.dto.toAvailableMentorTimeSlot
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.MentorTimeSlotRepository
import com.linchpino.core.repository.getByReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class TimeSlotService(private val accountRepository: AccountRepository, private val repository: MentorTimeSlotRepository) {


    fun addTimeSlots(request: AddTimeSlotsRequest) {
        val account = accountRepository.getByReference(request.mentorId)
        val mentorTimeSlots = request.timeSlots
            .map { it.toAvailableMentorTimeSlot(account) }
        repository.saveAll(mentorTimeSlots)
    }
}
