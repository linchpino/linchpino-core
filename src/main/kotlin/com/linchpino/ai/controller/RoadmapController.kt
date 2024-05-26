package com.linchpino.ai.controller

import com.linchpino.ai.service.RoadmapService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("ai")
class RoadmapController(private val roadmapService: RoadmapService) {
    @GetMapping("/roadmap")
    fun generateHaiku(): ResponseEntity<String> {
        return ResponseEntity.ok(
            roadmapService.getRoadmap(
                "Linchpino",
                "Java Developing",
                "Junior",
                "Senior",
                "Java programming"
            )
        )
    }
}
