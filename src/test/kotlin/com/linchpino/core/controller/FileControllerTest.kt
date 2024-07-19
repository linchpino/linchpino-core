package com.linchpino.core.controller

import com.linchpino.core.service.StorageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

@ExtendWith(MockitoExtension::class)
class FileControllerTest {

    @Mock
    private lateinit var storageService: StorageService

    @InjectMocks
    private lateinit var fileController: FileController

    @Test
    fun `test downloadImage returns correct ResponseEntity`() {
        val id = "testImageId"
        val fileContent = "Test Image Content".toByteArray()
        val memeType = MediaType.IMAGE_JPEG_VALUE

        `when`(storageService.downloadFile(id)).thenReturn(fileContent to memeType)

        val responseEntity: ResponseEntity<ByteArray> = fileController.downloadImage(id)


        assertThat(HttpStatus.OK).isEqualTo(responseEntity.statusCode)
        assertThat(MediaType.IMAGE_JPEG).isEqualTo(responseEntity.headers.contentType)
        assertThat(
            ContentDisposition.inline().filename(id).build()
        ).isEqualTo(responseEntity.headers.contentDisposition)
        assertThat(fileContent).isEqualTo(responseEntity.body)
    }

}
