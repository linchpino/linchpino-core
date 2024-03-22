package com.linchpino.core.repository

import com.linchpino.core.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    fun isUserExistByEmail(email: String): Boolean
}