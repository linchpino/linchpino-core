package com.linchpino.ai.controller;

import com.linchpino.ai.service.RoadmapService;
import org.apache.coyote.BadRequestException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping(path ="/me/{serviceName}", produces = "application/json")
    public String getRoadmapVia(@PathVariable(name = "serviceName") String serviceName) throws BadRequestException {
        return roadmapService.getRoadmap(serviceName);
    }
}
