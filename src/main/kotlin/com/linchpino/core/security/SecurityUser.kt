package com.linchpino.core.security

import com.linchpino.core.entity.Role
import com.linchpino.core.enums.AccountStatusEnum
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class SecurityUser(
    private val roles: Set<Role>,
    private val email: String,
    private val password: String?,
    private val status: AccountStatusEnum
) : UserDetails {
    override fun getAuthorities(): List<GrantedAuthority> {
        return roles.map {
            SimpleGrantedAuthority(it.title.name)
        }
    }

    override fun getPassword(): String? {
        return this.password
    }

    override fun getUsername(): String {
        return this.email
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
        return this.status == AccountStatusEnum.ACTIVATED
    }

}
