package com.linchpino.core.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "MENTOR_TIME_SLOT")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class MentorTimeSlot : AbstractEntity() {
    @JoinColumn(name = "ACCOUNT_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    var account: Account? = null

    @Column(name = "DATE")
    lateinit var date: LocalDate

    @Column(name = "FROM_TIME")
    lateinit var fromTime: LocalDateTime

    @Column(name = "TO_TOME")
    lateinit var toTime: LocalDateTime

//    @Enumerated(EnumType.STRING)
//    @Column(name = "STATUS")
//    lateinit var MentorTimeSlotEnum status
}