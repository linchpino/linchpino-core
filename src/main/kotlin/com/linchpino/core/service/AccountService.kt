package com.linchpino.core.service

import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.dto.mapper.AccountMapper
import com.linchpino.core.entity.Account
import com.linchpino.core.repository.AccountRepository
import lombok.extern.slf4j.Slf4j
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Slf4j
@Transactional
class AccountService(private val repository: AccountRepository, private val mapper: AccountMapper,private val passwordEncoder: PasswordEncoder) {

	fun createAccount(createAccountRequest: CreateAccountRequest): CreateAccountResult {
		val account: Account = mapper.accountDtoToAccount(createAccountRequest)
		// encrypt password
		account.password = passwordEncoder.encode(account.password)
		repository.save(account)
		return mapper.entityToResultDto(account)
	}
}
