package com.linchpino.core.entity

import com.linchpino.core.enums.PaymentStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import java.math.BigDecimal
import java.time.ZonedDateTime
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.format.annotation.DateTimeFormat

@Entity
@DynamicUpdate
@EntityListeners(AuditingEntityListener::class)
class Payment {

    @Id
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.PENDING

    @Column(nullable = false, length = 255)
    var refNumber: String? = null

    @OneToOne
    @MapsId
    var interview: Interview? = null

    var amount: BigDecimal = BigDecimal.ZERO

    @CreatedBy
    @Column(name = "CREATED_BY", nullable = true, updatable = false)
    var createdBy: Long? = null

    @CreationTimestamp
    @Column(name = "CREATED_ON", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var createdOn: ZonedDateTime? = null

    @LastModifiedBy
    @Column(name = "MODIFIED_BY", nullable = true)
    var modifiedBy: Long? = null

    @UpdateTimestamp
    @Column(name = "MODIFIED_ON", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var modifiedOn: ZonedDateTime? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractEntity

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
