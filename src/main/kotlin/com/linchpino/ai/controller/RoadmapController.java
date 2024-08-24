package com.linchpino.ai.controller;

import com.linchpino.ai.service.FileService;
import com.linchpino.ai.service.RoadmapService;
import com.linchpino.ai.service.model.RequestDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final FileService fileService;

    public RoadmapController(RoadmapService roadmapService, FileService fileService) {
        this.roadmapService = roadmapService;
        this.fileService = fileService;
    }

    @PostMapping(produces = "application/json")
    public String getRoadmap(@RequestParam("target") String target, @RequestParam("file") MultipartFile file) {
        return roadmapService.getRoadmap(RequestDetail.of(target, fileService.saveFile(file)));
    }
}
