package com.linchpino.core.entity

import com.linchpino.core.enums.AccountTypeEnum
import com.linchpino.core.enums.converters.AccountTypeEnumConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "ROLE")
class Role {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "TITLE")
    @Enumerated(EnumType.STRING)
    @Convert(converter = AccountTypeEnumConverter::class)
    var title: AccountTypeEnum = AccountTypeEnum.GUEST

    @ManyToMany(mappedBy = "roles")
    val accounts = mutableSetOf<Account>()
}
