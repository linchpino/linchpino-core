package com.linchpino.core.entity

import com.linchpino.core.enums.AccountTypeEnum
import jakarta.persistence.*

@Entity
@Table(name = "ROLE")
class Role {
    @Id
    @Column(name = "ID")
    var id: Int? = null

    @Column(name = "TITLE")
    @Enumerated(EnumType.STRING)
    var title: AccountTypeEnum = AccountTypeEnum.GUEST
        set(title) {
            field = title
            id = title.value
        }

    @ManyToMany(mappedBy = "roles")
    val accounts = mutableSetOf<Account>()
}
