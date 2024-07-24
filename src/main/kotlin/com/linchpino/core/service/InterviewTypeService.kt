package com.linchpino.core.service

import com.linchpino.core.dto.InterviewTypeCreateRequest
import com.linchpino.core.dto.InterviewTypeSearchResponse
import com.linchpino.core.dto.InterviewTypeUpdateRequest
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import com.linchpino.core.repository.InterviewTypeRepository
import com.linchpino.core.repository.JobPositionRepository
import com.linchpino.core.repository.findReferenceById
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
    fun searchByName(name: String?, pageable: Pageable) = repository.search(name, pageable)

    fun createInterviewType(request: InterviewTypeCreateRequest): InterviewTypeSearchResponse {
        val jobPosition = jobPositionRepository.findReferenceById(request.jobPositionId)

        val interviewType = InterviewType().apply {
            this.name = request.name
            this.jobPositions.add(jobPosition)
        }
        repository.save(interviewType)
        return InterviewTypeSearchResponse(interviewType.id, interviewType.name)
    }

    @Transactional(readOnly = true)
    fun getInterviewTypeById(id: Long) = repository.findByIdOrNull(id)
        ?.let {
            InterviewTypeSearchResponse(it.id, it.name)
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
        interviewType.name = request.name
    }

    fun deleteInterviewType(id: Long) {
        repository.deleteById(id)
    }
}
