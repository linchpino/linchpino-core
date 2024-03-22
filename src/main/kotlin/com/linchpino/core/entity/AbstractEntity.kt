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
import org.hibernate.annotations.DynamicUpdate
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

@MappedSuperclass
@DynamicUpdate
@EntityListeners(AuditingEntityListener::class)
abstract class AbstractEntity(
	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	val id: Long? = null,

	@CreatedBy
	@Column(name = "CREATED_BY", nullable = true, updatable = false, columnDefinition = "char(15)", length = 255)
	var createdBy: String? = null,

	@CreatedDate
	@Column(name = "CREATED_ON", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	var createdOn: Date? = null,

	@LastModifiedBy
	@Column(name = "MODIFIED_BY", nullable = true, columnDefinition = "char(15)", length = 255)
	var modifiedBy: String? = null,

	@LastModifiedDate
	@Column(name = "MODIFIED_ON", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	var modifiedOn: Date? = null,
) {
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
