package com.linchpino.core.entity

import com.linchpino.core.enums.MentorTimeSlotEnum
import com.linchpino.core.enums.converters.MentorTimeSlotEnumConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
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
    var status: MentorTimeSlotEnum = MentorTimeSlotEnum.UNKNOWN
}
