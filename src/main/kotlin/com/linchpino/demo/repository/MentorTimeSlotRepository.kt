package com.linchpino.demo.repository

import com.linchpino.demo.entity.MentorTimeSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MentorTimeSlotRepository : JpaRepository<MentorTimeSlot, Long>