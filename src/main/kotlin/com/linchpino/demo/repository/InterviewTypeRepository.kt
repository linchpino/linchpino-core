package com.linchpino.demo.repository

import com.linchpino.demo.entity.InterviewType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InterviewTypeRepository : JpaRepository<InterviewType, Long>