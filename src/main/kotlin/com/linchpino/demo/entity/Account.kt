package com.linchpino.demo.entity

import jakarta.persistence.*
import lombok.*

@Entity
@Table(name = "ACCOUNT")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class Account : AbstractEntity() {
    @Column(name = "FIRST_NAME")
    lateinit var firstName: String

    @Column(name = "LAST_NAME")
    lateinit var lastName: String

    @Column(name = "email")
    lateinit var email: String

    @Column(name = "password")
    lateinit var password: String //encrypt password!

//    @Enumerated(EnumType.STRING)
//    @Column(name = "TYPE")
//    lateinit var AccountTypeEnum type

//    @Enumerated(EnumType.STRING)
//    @Column(name = "STATUS")
//    lateinit var MentorTimeSlotEnum status
}