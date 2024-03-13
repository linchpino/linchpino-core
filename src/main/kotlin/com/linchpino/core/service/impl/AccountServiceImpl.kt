package com.linchpino.core.service.impl

import com.linchpino.core.dto.AccountDto
import com.linchpino.core.dto.mapper.AccountMapper
import com.linchpino.core.entity.Account
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.service.AccountService
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service

@Service
@Slf4j
open class AccountServiceImpl(private val repository: AccountRepository, private val mapper: AccountMapper) :
    AccountService {

    override fun newAccount(accountDto: AccountDto) {
        val account: Account = mapper.accountDtoToAccount(accountDto)
        repository.save(account)
    }

}