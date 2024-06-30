package com.linchpino.ai.controller.dto;

import java.io.Serializable;

public class RoadmapRequestDTO implements Serializable {
    private String targetLevel;
    private String linkedinUrl;

    public RoadmapRequestDTO() {
    }

    public RoadmapRequestDTO(String targetLevel, String linkedinUrl) {
        this.targetLevel = targetLevel;
        this.linkedinUrl = linkedinUrl;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(String targetLevel) {
        this.targetLevel = targetLevel;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }
}
