package com.linchpino.core.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.linchpino.core.entity.Account
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.util.UUID


@Service
class StorageService(
    @Value("\${gcp.bucket}")
    private val bucketName: String,
    private val storage: Storage
) {

    private var validMimeTypes: Array<String> = arrayOf(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/bmp",
        "image/gif")

    private val tika: Tika = Tika()

    fun uploadProfileImage(account: Account, file: MultipartFile): String {
        validateMimeType(BufferedInputStream(file.inputStream))
        if(file.size < 10 * 1024){
            throw LinchpinException(ErrorCode.SMALL_FILE_SIZE,"file too small")
        }
        val filename = "${account.id}-${UUID.randomUUID()}"
        val blobId = BlobId.of(bucketName, filename)
        val blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.contentType).build()

        storage.createFrom(blobInfo, file.inputStream)
        return filename
    }


    fun downloadFile(filename: String): Pair<ByteArray, String> {
        val blob = storage.get(BlobId.of(bucketName, filename)) ?: throw LinchpinException(ErrorCode.ENTITY_NOT_FOUND,"image with name $filename not found",filename)
        val contentType = blob.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
        return blob.getContent() to contentType
    }


    private fun validateMimeType(inputStream: BufferedInputStream) {
        if (validMimeTypes.isEmpty()) return
        val metadata = Metadata()
        val detector = tika.detector
        val mimeType: String = detector.detect(inputStream, metadata).toString()
        for (validMimetype in validMimeTypes)
            if (validMimetype == mimeType) return

        throw LinchpinException(ErrorCode.MIME_TYPE_NOT_ALLOWED,"MIME type not supported: $mimeType")
    }
}
