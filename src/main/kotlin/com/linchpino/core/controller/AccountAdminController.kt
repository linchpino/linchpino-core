package com.linchpino.core.controller

import com.linchpino.core.dto.ResetAccountPasswordRequest
import com.linchpino.core.dto.SearchAccountResult
import com.linchpino.core.dto.UpdateAccountRequestByAdmin
import com.linchpino.core.service.AccountService
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/admin/accounts")
class AccountAdminController(private val accountService: AccountService) {


    @PutMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetAccountPasswordRequest) {
        accountService.resetAccountPasswordByAdmin(request)
    }

    @PutMapping("/update")
    fun updateAnyAccount(@Valid @RequestBody request: UpdateAccountRequestByAdmin) {
        accountService.updateAccountByAdmin(request)
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful"),
            ApiResponse(responseCode = "401", description = "Not authenticated"),
            ApiResponse(responseCode = "403", description = "Not authorized")
        ]
    )
    fun searchAccounts(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) role: Int?,
        page: Pageable
    ): Page<SearchAccountResult> {
        return accountService.searchAccountByNameOrRole(name, role, page)
    }
}
