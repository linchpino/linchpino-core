package com.linchpino.core.service

import com.linchpino.core.entity.InterviewLog
import com.linchpino.core.enums.InterviewLogType
import com.linchpino.core.repository.InterviewLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class InterviewLogService(private val repository: InterviewLogRepository) {

    fun save(logType: InterviewLogType, id: Long?) {
        InterviewLog().apply {
            type =  logType
            createdBy = id
        }.also {
            repository.save(it)
        }
    }
}
