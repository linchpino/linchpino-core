package com.linchpino.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "meet")
data class MeetCredential(val privateKeyId:String,val privateKey:String,val clientEmail:String,val clientId:String)
