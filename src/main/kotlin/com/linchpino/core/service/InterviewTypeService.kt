package com.linchpino.core.service

import com.linchpino.core.dto.InterviewTypeCreateRequest
import com.linchpino.core.dto.InterviewTypeResponse
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.dto.InterviewTypeUpdateRequest
import com.linchpino.core.dto.JobPositionSearchResponse
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.findReferenceById
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class InterviewTypeService(
    private val repository: InterviewTypeRepository,
    private val jobPositionRepository: JobPositionRepository
) {

    @Transactional(readOnly = true)
    fun searchByName(name: String?, pageable: Pageable): Page<InterviewTypeResponse> {
        val result = repository.search(name, pageable)
        val newContent = result.content.map {
            InterviewTypeResponse(
                it.id,
                it.name,
                JobPositionSearchResponse(it.jobPositions.first().id, it.jobPositions.first().title)
            )
        }
        return PageImpl(newContent, pageable, result.totalElements)
    }

    fun createInterviewType(request: InterviewTypeCreateRequest): InterviewTypeSearchResponse {
        val jobPosition = jobPositionRepository.findReferenceById(request.jobPositionId)

        val interviewType = InterviewType().apply {
            this.name = request.name
        }
        jobPosition.addInterviewType(interviewType)
        repository.save(interviewType)
        return InterviewTypeSearchResponse(interviewType.id, interviewType.name)
    }

    @Transactional(readOnly = true)
    fun getInterviewTypeById(id: Long) = repository.findByIdOrNull(id)
        ?.let {
            InterviewTypeResponse(
                it.id,
                it.name,
                JobPositionSearchResponse(it.jobPositions.first().id, it.jobPositions.first().title)
            )
        } ?: throw LinchpinException(
        ErrorCode.ENTITY_NOT_FOUND,
        "interviewType with id: $id does not exists",
        InterviewType::class.java.simpleName
    )

    fun updateInterviewType(id: Long, request: InterviewTypeUpdateRequest) {
        val interviewType = repository.findByIdOrNull(id) ?: throw LinchpinException(
            ErrorCode.ENTITY_NOT_FOUND,
            "interviewType with id: $id does not exists",
            InterviewType::class.java.simpleName
        )
        request.name?.let {
            interviewType.name = it
        }

        request.jobPositionId?.let {
            jobPositionRepository.findByIdOrNull(it)?.let { jobPosition ->
                interviewType.jobPositions.forEach { jp -> jp.removeInterviewType(interviewType) }
                jobPosition.addInterviewType(interviewType)
            }
        }
    }

    fun deleteInterviewType(id: Long) {
        repository.deleteById(id)
    }
}
