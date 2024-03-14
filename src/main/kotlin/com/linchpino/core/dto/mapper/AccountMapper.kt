package com.linchpino.core.dto.mapper

import com.linchpino.core.dto.AccountDto
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.entity.Account
import com.linchpino.core.service.impl.AccountService
import org.mapstruct.Mapper

@Mapper(componentModel = "spring", uses = [AccountService::class])
interface AccountMapper {
    fun accountDtoToAccount(dto: AccountDto): Account
    fun accountToAccountDto(entity: Account): AccountDto
	fun entityToResultDto(entity: Account):CreateAccountResult
}
