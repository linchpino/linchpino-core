package com.linchpino.ai.service.impl

import com.linchpino.ai.model.Resume
import com.linchpino.ai.repository.ResumeRepository
import com.linchpino.core.exception.ErrorCode
import com.linchpino.core.exception.LinchpinException
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException

@Service
class ResumeService(private val resumeRepository: ResumeRepository) {
    private val logger: Logger = LoggerFactory.getLogger(ResumeService::class.java)

    @Value("\${spring.ai.roadmap-attempt-limit}")
    private val roadmapAttemptLimit = 0

    fun create(file: File): Resume {
        val lines = readLines(file)
        val email = Resume.findEmail(lines)
        if (isRoadmapAttemptLimitReached(email)) {
            throw LinchpinException(ErrorCode.TOO_MANY_ATTEMPT, "Roadmap attempt limit reached for email: {}", email)
        }
        return Resume(email, lines)
    }

    fun save(resume: Resume): Resume {
        return resumeRepository.save(resume)
    }

    private fun isRoadmapAttemptLimitReached(email: String): Boolean {
        return resumeRepository.countAllByEmail(email) >= roadmapAttemptLimit
    }

    /**
     * Read the lines from the PDF file
     *
     * @param file File
     * @return List of lines
     */
    private fun readLines(file: File): List<String> {
        try {
            Loader.loadPDF(file).use { document ->
                logger.info("Reading the file: {}", file.name)
                val extractedText = PDFTextStripper().getText(document)
                val lines = extractedText.split("\n")
                    .filter { it.isNotBlank() }
                return ArrayList(lines)
            }
        } catch (e: IOException) {
            throw LinchpinException(ErrorCode.SERVER_ERROR, "Error occurred while reading the file!", e)
        }
    }
}
