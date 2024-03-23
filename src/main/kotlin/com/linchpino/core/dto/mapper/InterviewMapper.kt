package com.linchpino.core.dto.mapper

import com.linchpino.core.dto.InterviewRequest
import com.linchpino.core.dto.InterviewResult
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.entity.InterviewType
import com.linchpino.core.entity.JobPosition
import com.linchpino.core.entity.MentorTimeSlot
import com.linchpino.core.enums.AccountStatus

class InterviewMapper {
    companion object {
        fun interviewDtoToInterview(dto: InterviewRequest): Interview {
            val jobPositionId = JobPosition()
            jobPositionId.id = dto.jobPositionId
            jobPositionId.title = ""

            val interviewTypeId = InterviewType()
            interviewTypeId.id = dto.interviewTypeId
            interviewTypeId.name = ""

            val jobSeekerAcc = Account()
            jobSeekerAcc.email = dto.jobSeekerEmail
            jobSeekerAcc.status = AccountStatus.DEACTIVATED

            val timeSlotId = MentorTimeSlot()
            timeSlotId.id = dto.timeSlotId
            timeSlotId.account = jobSeekerAcc

            return Interview().apply {
                jobPosition = jobPositionId
                interviewType = interviewTypeId
                timeSlot = timeSlotId
                jobSeekerAccount = jobSeekerAcc
            }
        }

        fun entityToResultDto(entity: Interview): InterviewResult {
            return InterviewResult(
                entity.id,
                entity.jobPosition?.id,
                entity.interviewType?.id,
                entity.timeSlot?.id,
                entity.jobSeekerAccount?.email
            )
        }
    }
}