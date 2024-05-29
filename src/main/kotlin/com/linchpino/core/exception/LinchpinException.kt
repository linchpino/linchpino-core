package com.linchpino.core.exception

class LinchpinException : RuntimeException {
    val errorCode: ErrorCode
    val params: Array<out Any>

    constructor(errorCode: ErrorCode, message: String?, vararg params: Any) : super(message) {
        this.errorCode = errorCode
        this.params = params
    }

    constructor(message: String?, cause: Throwable?, errorCode: ErrorCode, vararg params: Any) : super(message, cause) {
        this.errorCode = errorCode
        this.params = params
    }
}
