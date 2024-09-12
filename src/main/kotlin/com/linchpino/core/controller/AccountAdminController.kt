package com.linchpino.core.controller

import com.linchpino.core.dto.ResetAccountPasswordRequest
import com.linchpino.core.service.AccountService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/admin/accounts")
class AccountAdminController(private val accountService: AccountService) {


    @PutMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetAccountPasswordRequest) {
        accountService.resetAccountPasswordByAdmin(request)
    }
}
