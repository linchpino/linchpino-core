package com.linchpino.core.repository

import com.linchpino.core.entity.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository:JpaRepository<Role,Int>
