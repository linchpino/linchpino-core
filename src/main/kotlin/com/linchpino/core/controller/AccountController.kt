package com.linchpino.core.controller

import com.linchpino.core.dto.AccountDto
import com.linchpino.core.exception.message.Message
import com.linchpino.core.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/accounts")
class AccountController(private val accountService: AccountService) {

    @PostMapping("/new")
    @ResponseStatus(HttpStatus.CREATED)
    fun newAccount(@RequestBody accountDto: AccountDto): ResponseEntity<String> {
        accountService.newAccount(accountDto)
        return ResponseEntity.ok(Message.createAccountMsg)
    }
}