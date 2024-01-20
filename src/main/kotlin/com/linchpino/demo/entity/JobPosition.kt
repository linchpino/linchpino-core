package com.linchpino.demo.entity

import jakarta.persistence.*
import lombok.*

@Entity
@Table(name = "JOB_POSITION")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class JobPosition : AbstractEntity(){
    @Column(name = "TITLE")
    lateinit var title: String
}