package com.linchpino.ai.controller;

import com.linchpino.ai.controller.dto.RoadmapRequestDTO;
import com.linchpino.ai.service.RoadmapService;
import com.linchpino.ai.service.domain.RequestDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @PostMapping(produces = "application/json")
    public String getRoadmap(@RequestParam(name = "serviceName", required = false) String serviceName,
                             @RequestParam(name = "interactionType", required = false) String interactionType,
                             @RequestBody RoadmapRequestDTO requestDTO) {
        return roadmapService.getRoadmap(serviceName, interactionType, RequestDetail.of(requestDTO));
    }
}
