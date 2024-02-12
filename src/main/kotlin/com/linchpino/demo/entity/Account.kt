package com.linchpino.demo.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

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
//    lateinit val AccountTypeEnum type
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "STATUS")
//    lateinit val MentorTimeSlotEnum status
}