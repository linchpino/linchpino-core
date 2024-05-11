package com.linchpino.core.dto.mapper

import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.entity.Account
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface AccountMapper {

    fun accountDtoToAccount(dto: CreateAccountRequest): Account
    fun entityToResultDto(entity: Account): CreateAccountResult
//
//    companion object {
//        @JvmStatic
//        @Named("mapFromInt")
//        fun mapFromInt(type: Int) =
//            AccountTypeEnum.entries.firstOrNull { it.value == type } ?: AccountTypeEnum.GUEST
//    }
}
