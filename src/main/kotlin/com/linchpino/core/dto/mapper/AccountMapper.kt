package com.linchpino.core.dto.mapper

import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.service.impl.AccountService
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import java.util.function.Predicate

@Mapper(componentModel = "spring", uses = [AccountService::class])
interface AccountMapper {

	@Mapping(target = "type", source = "type", qualifiedByName = ["mapFromInt"])
	fun accountDtoToAccount(dto: CreateAccountRequest?): Account
	fun entityToResultDto(entity: Account?): CreateAccountResult

	companion object {
		@JvmStatic
		@Named("mapFromInt")
		fun mapFromInt(type: Int): AccountTypeEnum {
			return AccountTypeEnum.entries.stream()
				.filter(Predicate<AccountTypeEnum> { s: AccountTypeEnum -> s.v == type }).findFirst()
				.orElse(AccountTypeEnum.UNKNOWN)
		}
	}
}
