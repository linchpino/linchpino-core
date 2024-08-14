package com.linchpino.core.service

import com.linchpino.core.dto.LinkedInUserInfoResponse
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class LinkedInService(private val restClient: RestClient) {

    private val userInfoUri = "https://api.linkedin.com/v2/userinfo"

    fun userInfo(token:String): LinkedInUserInfoResponse {
        return restClient.get()
            .uri(userInfoUri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .body(LinkedInUserInfoResponse::class.java) ?: throw RuntimeException("failed to fetch user info")
    }
}
