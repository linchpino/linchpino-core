package com.linchpino.core.repository

import com.linchpino.core.entity.PaymentMethod
import org.springframework.data.repository.CrudRepository

interface PaymentMethodRepository:CrudRepository<PaymentMethod,Long> {
}
