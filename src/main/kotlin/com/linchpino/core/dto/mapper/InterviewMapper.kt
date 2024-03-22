package com.linchpino.core.dto.mapper

import com.linchpino.core.dto.InterviewRequest
import com.linchpino.core.dto.InterviewResult
import com.linchpino.core.entity.Interview
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface InterviewMapper {
    fun interviewDtoToInterview(dto: InterviewRequest): Interview
    fun entityToResultDto(entity: Interview): InterviewResult
}
