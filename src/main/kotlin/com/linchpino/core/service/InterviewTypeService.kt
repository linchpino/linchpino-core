package com.linchpino.core.service

import com.linchpino.core.repository.InterviewTypeRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class InterviewTypeService(private val repository: InterviewTypeRepository) {

    @Transactional(readOnly = true)
    fun searchByName(name: String?, pageable: Pageable) = repository.search(name, pageable)
}
