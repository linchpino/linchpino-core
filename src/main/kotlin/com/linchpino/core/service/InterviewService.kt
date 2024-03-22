package com.linchpino.core.service

import com.linchpino.core.dto.InterviewRequest
import com.linchpino.core.dto.InterviewResult
import com.linchpino.core.dto.mapper.InterviewMapper
import com.linchpino.core.entity.Account
import com.linchpino.core.entity.Interview
import com.linchpino.core.enums.AccountStatus
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.repository.InterviewRepository
import lombok.extern.slf4j.Slf4j
import org.apache.commons.lang3.BooleanUtils.isFalse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Slf4j
@Transactional
class InterviewService(
    private val repository: InterviewRepository,
    private val accountRepository: AccountRepository,
    private val mapper: InterviewMapper
) {

    fun newInterview(request: InterviewRequest): InterviewResult {
        val interview: Interview = mapper.interviewDtoToInterview(request)
        //interview.createdOn(LocalDate.now())
        //this email should fetch from the upcoming API
        val isUserLoggedIn: Boolean? = isFalse(accountRepository.isUserExistByEmail("email"))
        isUserLoggedIn ?: createAccountRequest("email")
        repository.save(interview)
        return mapper.entityToResultDto(interview)
    }

    private fun createAccountRequest(emailAdd: String) {
        val account = Account().apply {
            email = emailAdd
            status = AccountStatus.DEACTIVATED
        }
        accountRepository.save(account)
    }
}