package com.linchpino.core.controller

import com.linchpino.core.dto.ResetAccountPasswordRequest
import com.linchpino.core.dto.UpdateAccountRequestByAdmin
import com.linchpino.core.service.AccountService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class AccountAdminControllerTest {

    @Mock
    lateinit var accountService: AccountService

    @InjectMocks
    lateinit var accountAdminController: AccountAdminController

    @Test
    fun `test reset any account password by admin`() {
        // Given
        val request = ResetAccountPasswordRequest(1, "newPassword")

        // When
        accountAdminController.resetPassword(request)

        // Then
        verify(accountService, times(1)).resetAccountPasswordByAdmin(request)
    }

    @Test
    fun `test admin can update roles and status of any account`(){
        // Given
        val request = UpdateAccountRequestByAdmin(1, listOf(1,2),1)

        // When
        accountAdminController.updateAnyAccount(request)

        // Then
        verify(accountService, times(1)).updateAccountByAdmin(request)
    }

}
