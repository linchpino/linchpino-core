package com.linchpino.core.service.impl

import com.linchpino.core.dto.AccountDto
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.mapper.AccountMapper
import com.linchpino.core.entity.Account
import com.linchpino.core.repository.AccountRepository
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Slf4j
@Transactional
class AccountService(private val repository: AccountRepository, private val mapper: AccountMapper) {

	fun newAccount(accountDto: AccountDto): CreateAccountResult {
		val account: Account = mapper.accountDtoToAccount(accountDto)
		account.createdBy = accountDto.email // todo temporary should be removed
		repository.save(account)
		return mapper.entityToResultDto(account)
	}
}
