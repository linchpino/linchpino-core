package com.linchpino.ai.controller;

import com.linchpino.ai.service.RoadmapService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/ai/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @PostMapping(produces = "application/json")
    public String getRoadmap(@RequestParam("target") String target, @RequestParam("file") MultipartFile file) {
        return roadmapService.getRoadmap(target, convert(file));
    }

    private File convert(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return null;
        }
        File convFile = new File(file.getOriginalFilename());
        try {
            if (convFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(convFile);
                fos.write(file.getBytes());
                fos.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error in converting file: " + e.getMessage(), e);
        }
        return convFile;
    }
}
