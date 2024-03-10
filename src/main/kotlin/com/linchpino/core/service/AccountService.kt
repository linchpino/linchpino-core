package com.linchpino.core.service

import com.linchpino.core.dto.AccountDto
import org.jetbrains.annotations.NotNull
import org.springframework.validation.annotation.Validated

@Validated
interface AccountService {
    fun newAccount(@NotNull accountDto: AccountDto)
}