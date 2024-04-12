package com.linchpino.core.entity

import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.converters.AccountTypeEnumConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "ROLE")
class Role : AbstractEntity() {
    @Convert(converter = AccountTypeEnumConverter::class)
    @Column(name = "ROLE")
    var roleName: AccountTypeEnum = AccountTypeEnum.GUEST

    @ManyToMany(mappedBy = "roles")
    val accounts = mutableSetOf<Account>()
}
