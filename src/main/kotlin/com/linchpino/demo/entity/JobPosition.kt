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