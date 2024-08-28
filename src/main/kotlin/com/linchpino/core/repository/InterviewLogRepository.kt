package com.linchpino.core.repository

import com.linchpino.core.entity.InterviewLog
import org.springframework.data.jpa.repository.JpaRepository

interface InterviewLogRepository: JpaRepository<InterviewLog, Long> {
}
