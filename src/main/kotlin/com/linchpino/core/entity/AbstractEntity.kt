package com.linchpino.core.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import lombok.EqualsAndHashCode
import lombok.Getter
import lombok.Setter
import org.hibernate.annotations.DynamicUpdate
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

@MappedSuperclass
@Getter
@Setter
@DynamicUpdate
@EntityListeners(AuditingEntityListener::class)
abstract class AbstractEntity(
    @Id @Column(name = "ID") @GeneratedValue(strategy = GenerationType.IDENTITY) @EqualsAndHashCode.Include
    private var id: Long = -1,

    @CreatedBy
    @Column(name = "CREATED_BY", nullable = false, updatable = false, columnDefinition = "char(15)", length = 15)
    private val createdBy: String? = null,

    @CreatedDate
    @Column(name = "CREATED_ON", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private val createdOn: Date? = null,

    @LastModifiedBy
    @Column(name = "MODIFIED_BY", nullable = false, columnDefinition = "char(15)", length = 15)
    private val modifiedBy: String? = null,

    @LastModifiedDate
    @Column(name = "MODIFIED_ON", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private val modifiedOn: Date? = null
)