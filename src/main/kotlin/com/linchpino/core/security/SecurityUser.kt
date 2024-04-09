package com.linchpino.core.security

import com.linchpino.core.entity.Account
import com.linchpino.core.enums.AccountStatusEnum
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class SecurityUser(val account: Account) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        // Fetch roles from account and map with SimpleGrantedAuthority
        // Create Role table which has many2Many relation with account
        return mutableListOf<SimpleGrantedAuthority>()
    }

    override fun getPassword(): String {
        return account.password
    }

    override fun getUsername(): String {
        return account.email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return account.status == AccountStatusEnum.ACTIVATED
    }

}
