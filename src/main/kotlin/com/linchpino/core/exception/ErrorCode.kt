package com.linchpino.core.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus) {
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND),
    ACCOUNT_IS_ACTIVATED(HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST),
    TIMESLOT_IS_BOOKED(HttpStatus.BAD_REQUEST),
    INTERVIEW_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_TIMESLOT(HttpStatus.BAD_REQUEST),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_ACCOUNT_ROLE(HttpStatus.BAD_REQUEST),
    MIME_TYPE_NOT_ALLOWED(HttpStatus.BAD_REQUEST),
    SMALL_FILE_SIZE(HttpStatus.BAD_REQUEST),
}
