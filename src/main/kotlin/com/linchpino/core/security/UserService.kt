package com.linchpino.core.security

import com.linchpino.core.repository.AccountRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(private val accountRepository: AccountRepository) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails =
        accountRepository.findByEmailIgnoreCase(username)
            ?.let { SecurityUser(it.roles(), it.email, it.password, it.status) }
            ?: throw UsernameNotFoundException("user not found")

}

