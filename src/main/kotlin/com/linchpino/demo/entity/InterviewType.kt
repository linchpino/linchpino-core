package com.linchpino.demo.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

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