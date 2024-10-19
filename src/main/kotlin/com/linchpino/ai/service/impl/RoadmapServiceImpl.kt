package com.linchpino.ai.service.impl

import com.linchpino.ai.model.AIServiceName
import com.linchpino.ai.model.AIServiceName.Companion.getComponentNameOrDefault
import com.linchpino.ai.model.RequestDetail
import com.linchpino.ai.service.AIService
import com.linchpino.ai.service.RoadmapService
import io.grpc.netty.shaded.io.netty.util.internal.ObjectUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.io.File

@Service
class RoadmapServiceImpl(private val applicationContext: ApplicationContext, private val resumeService: ResumeService) :
    RoadmapService {
    @Value("\${spring.ai.default-service-provider}")
    private val aiServiceName: String? = null

    fun loadAIService(serviceName: AIServiceName): AIService {
        return applicationContext.getBean(serviceName.componentName, AIService::class.java)
    }

    override fun getRoadmap(targetLevel: String, resumeFile: File?): String? {
        ObjectUtil.checkNotNull(targetLevel, "Target level cannot be null")
        ObjectUtil.checkNotNull(resumeFile, "Resume file cannot be null")
        val serviceName = getComponentNameOrDefault(aiServiceName)
        val resume = resumeService.create(resumeFile)
        val requestDetail = RequestDetail(targetLevel, resumeService.save(resume))
        return loadAIService(serviceName).talkToAI(requestDetail)
    }
}
