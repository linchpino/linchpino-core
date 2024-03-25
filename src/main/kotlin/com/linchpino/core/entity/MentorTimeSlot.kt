package com.linchpino.core.entity

import jakarta.persistence.*
import lombok.*
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
