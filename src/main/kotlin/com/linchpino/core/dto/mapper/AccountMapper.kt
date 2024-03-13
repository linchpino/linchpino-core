package com.linchpino.core.dto.mapper

import com.linchpino.core.dto.AccountDto
import com.linchpino.core.entity.Account
import com.linchpino.core.service.impl.AccountServiceImpl
import org.mapstruct.Mapper

@Mapper(componentModel = "spring", uses = [AccountServiceImpl::class])
interface AccountMapper {
    fun accountDtoToAccount(dto: AccountDto): Account
    fun accountToAccountDto(car: Account): AccountDto
}
