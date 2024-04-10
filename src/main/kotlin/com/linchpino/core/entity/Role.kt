package com.linchpino.core.entity

import com.linchpino.core.enums.RolesEnum
import com.linchpino.core.enums.converters.RolesEnumConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "ROLE")
class Role : AbstractEntity() {
    @Convert(converter = RolesEnumConverter::class)
    @Column(name = "ROLE")
    var rolesEnum: RolesEnum = RolesEnum.GUEST

    @ManyToMany(mappedBy = "accountRoles")
    val accounts = mutableSetOf<Account>()
}
