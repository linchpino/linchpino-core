package com.linchpino.core.service

import com.linchpino.core.repository.InterviewTypeSearchResponse
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.JobPositionSearchResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class JobPositionService(private val jobPositionRepository: JobPositionRepository) {


	@Transactional(readOnly = true)
	fun searchByName(name: String?, pageable: Pageable): Page<JobPositionSearchResponse> =
		jobPositionRepository.search(name, pageable)

	@Transactional(readOnly = true)
	fun findInterviewTypesFor(jobPositionId: Long): List<InterviewTypeSearchResponse> {
		return jobPositionRepository.findInterviewsByJobPositionId(jobPositionId)
	}
}
