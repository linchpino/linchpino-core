package com.linchpino.core.entity

import com.linchpino.core.enums.MentorTimeSlotEnum
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.ZonedDateTime

@Entity
@Table(name = "MENTOR_TIME_SLOT")
class MentorTimeSlot : AbstractEntity() {
    @JoinColumn(name = "ACCOUNT_ID", referencedColumnName = "ID", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    var account: Account? = null

    @Column(name = "FROM_TIME")
    lateinit var fromTime: ZonedDateTime

    @Column(name = "TO_TIME")
    lateinit var toTime: ZonedDateTime

    @Column(name = "STATUS", nullable = false)
    @Convert(converter = MentorTimeSlotEnumConverter::class)
    lateinit var status: MentorTimeSlotEnum = MentorTimeSlotEnum.UNKNOWN
}
