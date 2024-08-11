package com.linchpino.core.entity

import com.linchpino.core.enums.InterviewLogType
import com.linchpino.core.enums.converters.InterviewLogEnumConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import java.time.ZonedDateTime
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.format.annotation.DateTimeFormat

@Table(name = "INTERVIEW_LOG")
@Entity
class InterviewLog {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreatedBy
    @Column(name = "CREATED_BY", nullable = true, updatable = false)
    var createdBy: Long? = null

    @CreationTimestamp
    @Column(name = "CREATED_ON", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var createdOn: ZonedDateTime? = null

    @Convert(converter = InterviewLogEnumConverter::class)
    @Column(name = "TYPE", nullable = false, updatable = false)
    var type: InterviewLogType? = null
}
