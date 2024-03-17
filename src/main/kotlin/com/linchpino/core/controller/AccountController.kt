package com.linchpino.core.controller

import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.dto.CreateAccountResult
import com.linchpino.core.service.impl.AccountService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/accounts")
class AccountController(private val accountService: AccountService) {

	@Operation(summary = "Create a new account")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiResponses(value = [
		ApiResponse(responseCode = "201", description = "Account created successfully"),
		ApiResponse(responseCode = "400", description = "Invalid request body")
	])
	@PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun newAccount(@RequestBody createAccountRequest: CreateAccountRequest): ResponseEntity<CreateAccountResult> {
        val result = accountService.newAccount(createAccountRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }
}
