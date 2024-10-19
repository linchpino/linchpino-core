package com.linchpino.ai.controller

import com.linchpino.ai.service.RoadmapService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@RestController
@RequestMapping("/api/ai/roadmaps")
class RoadmapController(private val roadmapService: RoadmapService) {

    @Operation(summary = "Search mentors with available timeslots based on date and interviewTypeId")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200"),
            ApiResponse(responseCode = "400", description = "Invalid request body")
        ]
    )
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getRoadmap(@RequestParam("target") target: String, @RequestParam("file") file: MultipartFile): String? {
        return roadmapService.getRoadmap(target, convert(file))
    }

    private fun convert(file: MultipartFile): File {
        val convFile = File(file.originalFilename)
        try {
            if (convFile.createNewFile()) {
                val fos = FileOutputStream(convFile)
                fos.write(file.bytes)
                fos.close()
            }
        } catch (e: IOException) {
            throw IllegalStateException("Error in converting file: " + e.message, e)
        }
        return convFile
    }
}
