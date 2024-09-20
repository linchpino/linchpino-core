package com.linchpino.core.controller

import com.linchpino.core.dto.ResetAccountPasswordRequest
import com.linchpino.core.dto.SearchAccountResult
import com.linchpino.core.dto.UpdateAccountRequestByAdmin
import com.linchpino.core.service.AccountService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

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

    @Test
    fun `test search accounts by name and role`() {
        // Given
        val expectedResult = listOf(
            SearchAccountResult(
                100,
                "John",
                "Doe",
                listOf("MENTOR"),
                "johndoe@example.com",
                "avatar.png"
            )
        )

        val page = Pageable.ofSize(10)
        `when`(accountService.searchAccountByNameOrRole("john", 3, page)).thenReturn(
            PageImpl(expectedResult)
        )

        // When
        val result = accountAdminController.searchAccounts("john", 3, page)

        // Then
        assertThat(result).isEqualTo(PageImpl(expectedResult))
        verify(accountService, times(1)).searchAccountByNameOrRole("john", 3, page)
    }

}
