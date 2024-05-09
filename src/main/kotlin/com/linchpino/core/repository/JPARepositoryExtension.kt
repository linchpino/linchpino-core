package com.linchpino.core.repository

import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import java.io.Serializable

inline fun <reified T, ID : Serializable> JpaRepository<T, ID>.getByReference(id: ID): T {
    return try {
        this.getReferenceById(id)
    } catch (ex: JpaObjectRetrievalFailureException) {
        throw LinchpinException("${T::class.java.simpleName} entity with id: $id not found ", ex, ErrorCode.ENTITY_NOT_FOUND, T::class.java.simpleName)
    }
}
