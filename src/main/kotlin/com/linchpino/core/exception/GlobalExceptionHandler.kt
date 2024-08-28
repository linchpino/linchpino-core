package com.linchpino.core.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler(private val messageSource: MessageSource) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(Exception::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnexpectedError(exception: Exception, request: HttpServletRequest): ResponseEntity<*> {
        log.error("Unexpected error: {}", exception.localizedMessage, exception)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body<Any>(
                ErrorResponse(
                    Instant.now(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    listOf(),
                    request.servletPath
                )
            )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMissingServletRequestParameterError(
        exception: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        return ResponseEntity.status(exception.statusCode)
            .body<Any>(
                ErrorResponse(
                    Instant.now(),
                    exception.statusCode.value(),
                    "Invalid Param",
                    listOf(ErrorMessage(exception.parameterName,exception.message)),
                    request.servletPath
                )
            )
    }
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidError(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val errorMessages = exception.bindingResult.fieldErrors
            .map { fieldError: FieldError ->
                ErrorMessage(
                    fieldError.field,
                    fieldError.defaultMessage!!
                )
            }


        return ResponseEntity.status(exception.statusCode)
            .body<Any>(
                ErrorResponse(
                    Instant.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid Param",
                    errorMessages,
                    request.servletPath
                )
            )
    }


    @ExceptionHandler(LinchpinException::class)
    fun handleLinchpinException(exception: LinchpinException, request: HttpServletRequest): ResponseEntity<*> {
        val message = messageSource.getMessage(exception.errorCode.name, exception.params, request.locale)
        log.error("Linchpin exception: {} -> ", message, exception)
        return ResponseEntity.status(exception.errorCode.status).body<Any>(
            ErrorResponse(
                Instant.now(),
                exception.errorCode.status.value(),
                message,
                listOf(),
                request.servletPath
            )
        )
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleFileSizeException(exception: MaxUploadSizeExceededException, request: HttpServletRequest): ResponseEntity<*> {
        log.error("Max request size error: {}", exception.localizedMessage, exception)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body<Any>(
                ErrorResponse(
                    Instant.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    "Max upload size exceeded error",
                    listOf(),
                    request.servletPath
                )
            )
    }
}
