package com.linchpino.ai.service

import java.io.File

interface RoadmapService {
    fun getRoadmap(targetLevel: String, resumeFile: File?): String?
}
