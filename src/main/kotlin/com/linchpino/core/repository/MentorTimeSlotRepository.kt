package com.linchpino.core.repository

import com.linchpino.core.entity.MentorTimeSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MentorTimeSlotRepository : JpaRepository<MentorTimeSlot, Long>