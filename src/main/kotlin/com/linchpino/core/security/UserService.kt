package com.linchpino.core.security

import com.linchpino.core.dto.CreateAccountRequest
import com.linchpino.core.enums.AccountStatusEnum
import com.linchpino.core.repository.AccountRepository
import com.linchpino.core.service.AccountService
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(private val accountRepository: AccountRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        accountRepository.findByEmailIgnoreCase(username)?.let { SecurityUser(it) }
            ?: throw UsernameNotFoundException("user not found")

    @Bean
    fun runner(accountService: AccountService) = ApplicationRunner {
        val account = accountService.createAccount(
            CreateAccountRequest(
                firstName = "John",
                lastName = "Doe",
                email = "johndoe@gmail.com",
                password = "secret",
                type = 2,
            )
        )
        accountRepository.findByEmailIgnoreCase(account.email)?.let {
            it.status = AccountStatusEnum.ACTIVATED
            accountRepository.save(it)
        }
    }
}

