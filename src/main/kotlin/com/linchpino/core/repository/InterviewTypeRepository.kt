package com.linchpino.core.repository

import com.linchpino.core.entity.InterviewType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InterviewTypeRepository : JpaRepository<InterviewType, Long>