package com.linchpino.core.service

import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.dto.JobPositionCreateRequest
import com.linchpino.core.dto.JobPositionSearchResponse
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class JobPositionService(
    private val jobPositionRepository: JobPositionRepository,
    private val interviewTypeRepository: InterviewTypeRepository
) {


    @Transactional(readOnly = true)
    fun searchByName(name: String?, pageable: Pageable): Page<JobPositionSearchResponse> =
        jobPositionRepository.search(name, pageable)

    @Transactional(readOnly = true)
    fun findInterviewTypesBy(jobPositionId: Long, pageable: Pageable): Page<InterviewTypeSearchResponse> {
        return jobPositionRepository.findInterviewsByJobPositionId(jobPositionId, pageable)
    }

    fun createJobPosition(request: JobPositionCreateRequest) {
        val jobPosition = JobPosition().apply {
            title = request.title
        }
        jobPositionRepository.save(jobPosition)
    }
}
