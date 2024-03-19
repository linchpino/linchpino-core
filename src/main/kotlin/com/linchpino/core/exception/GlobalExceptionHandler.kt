package com.linchpino.core.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

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

	@ExceptionHandler(MethodArgumentNotValidException::class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	fun handleMethodArgumentNotValidError(
		exception: MethodArgumentNotValidException,
		request: HttpServletRequest
	): ResponseEntity<*> {
		if (!exception.bindingResult.hasFieldErrors()) {
			return ResponseEntity.status(exception.statusCode).body<Any>(
				ErrorResponse(
					Instant.now(),
					HttpStatus.BAD_REQUEST.value(),
					"Invalid Param",
					listOf(),
					request.servletPath
				)
			)
		}

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
}
