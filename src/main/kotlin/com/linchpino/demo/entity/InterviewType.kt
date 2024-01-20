package com.linchpino.demo.entity

import jakarta.persistence.*
import lombok.*

@Entity
@Table(name = "INTERVIEW_TYPE")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class InterviewType : AbstractEntity(){
    @Column(name = "NAME")
    lateinit var name: String
}