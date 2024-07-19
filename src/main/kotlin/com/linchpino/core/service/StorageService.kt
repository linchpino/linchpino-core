package com.linchpino.core.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.linchpino.core.entity.Account
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class StorageService(
    @Value("\${gcp.bucket}")
    private val bucketName: String,
    private val storage: Storage
) {


    fun uploadProfileImage(account: Account, file: MultipartFile): String {
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
}
