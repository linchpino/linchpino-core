package com.linchpino.core.service

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.linchpino.core.entity.Account
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.InputStream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows

@ExtendWith(MockitoExtension::class)
class StorageServiceTest {

    @Mock
    private lateinit var storage: Storage

    private lateinit var storageService: StorageService

    private val bucketName = "test-bucket"

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        storageService = StorageService(bucketName, storage)
    }

    @Test
    fun `test upload profile image`() {
        // Arrange
        val account = Account().apply { id = 1 }
        this::class.java.getResourceAsStream("/img/test.png").use { inputStream ->
            val file = MockMultipartFile(
                "file",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                inputStream ?: "test.png".toByteArray().inputStream()
            )

            val blobInfoCaptor: ArgumentCaptor<BlobInfo> = ArgumentCaptor.forClass(BlobInfo::class.java)
            val streamCaptor: ArgumentCaptor<InputStream> = ArgumentCaptor.forClass(InputStream::class.java)

            // Act
            val result = storageService.uploadProfileImage(account, file)

            // Then
            verify(storage, times(1)).createFrom(blobInfoCaptor.capture(), streamCaptor.capture())
            val blobInfo = blobInfoCaptor.value
            assertThat(blobInfo.blobId.bucket).isEqualTo(bucketName)
            assertThat(blobInfo.contentType).isEqualTo(MediaType.IMAGE_PNG_VALUE)
            assertThat(blobInfo.blobId.name.startsWith(account.id.toString())).isTrue()
            assertThat(result).isNotBlank()
        }

    }

    @Test
    fun `test upload profile image with wrong MIME type results in error`() {
        // Arrange
        val account = Account().apply { id = 1 }
        val file = MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test".toByteArray())
        // Act
        val ex = assertThrows<LinchpinException> { storageService.uploadProfileImage(account, file) }
        assertThat(ex.errorCode).isEqualTo(ErrorCode.MIME_TYPE_NOT_ALLOWED)
    }

    @Test
    fun `test upload profile image with size less than 10kb results in error`() {
        val account = Account().apply { id = 1 }
        this::class.java.getResourceAsStream("/img/test_small.png").use { inputStream ->
            val file = MockMultipartFile(
                "file",
                "test_small.png",
                MediaType.IMAGE_PNG_VALUE,
                inputStream ?: "test.png".toByteArray().inputStream()
            )

            val ex = assertThrows<LinchpinException> { storageService.uploadProfileImage(account, file) }

           assertThat(ex.errorCode).isEqualTo(ErrorCode.SMALL_FILE_SIZE)
        }
    }

    @Test
    fun `test downloadFile success`() {
        // Given
        val filename = "test.jpg"
        val blobId = BlobId.of(bucketName, filename)
        val blob: Blob = mock(Blob::class.java)
        val fileContent = "test content".toByteArray()
        val contentType = MediaType.IMAGE_JPEG_VALUE

        `when`(storage.get(blobId)).thenReturn(blob)
        `when`(blob.contentType).thenReturn(contentType)
        `when`(blob.getContent()).thenReturn(fileContent)

        // When
        val result = storageService.downloadFile(filename)

        // Then
        assertThat(result.first).isEqualTo(fileContent)
        assertThat(result.second).isEqualTo(contentType)
    }

    @Test
    fun `test downloadFile blob not found`() {
        // Given
        val filename = "nonexistent.jpg"
        val blobId = BlobId.of(bucketName, filename)

        `when`(storage.get(blobId)).thenReturn(null)

        // When & Then
        val exception = assertThrows<LinchpinException> {
            storageService.downloadFile(filename)
        }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.ENTITY_NOT_FOUND)
    }

}

