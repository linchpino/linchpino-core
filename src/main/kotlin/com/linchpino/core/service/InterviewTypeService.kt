package com.linchpino.core.service

import com.linchpino.core.dto.InterviewTypeCreateRequest
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.findReferenceById
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class InterviewTypeService(
    private val repository: InterviewTypeRepository,
    private val jobPositionRepository: JobPositionRepository
) {


    @Transactional(readOnly = true)
    fun searchByName(name: String?, pageable: Pageable) = repository.search(name, pageable)

    fun createInterviewType(request: InterviewTypeCreateRequest): InterviewTypeSearchResponse {
        val jobPosition = jobPositionRepository.findReferenceById(request.jobPositionId)

        val interviewType = InterviewType().apply {
            this.name = request.name
            this.jobPositions.add(jobPosition)
        }
        repository.save(interviewType)
        return InterviewTypeSearchResponse(interviewType.id,interviewType.name)
    }
}
