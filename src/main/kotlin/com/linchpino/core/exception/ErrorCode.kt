package com.linchpino.core.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus) {
    SERVER_ERROR(HttpStatus.FORBIDDEN),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND),
    ACCOUNT_IS_ACTIVATED(HttpStatus.BAD_REQUEST),
    INVALID_TIMESLOT(HttpStatus.BAD_REQUEST),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND)
}
