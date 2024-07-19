package com.linchpino.core.controller

import com.linchpino.core.service.StorageService
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/files")
class FileController(private val storageService: StorageService) {


    @GetMapping("/image/{id}")
    fun downloadImage(@PathVariable id: String): ResponseEntity<ByteArray> {

        val (fileContent, memeType) = storageService.downloadFile(id)

        val headers = HttpHeaders().apply {
            contentType = MediaType.valueOf(memeType)
            contentDisposition = ContentDisposition
                .inline()
                .filename(id)
                .build()
        }

        return ResponseEntity(fileContent, headers, HttpStatus.OK)
    }
}
