package com.linchpino.ai.service.domain;

import com.linchpino.ai.controller.dto.RoadmapRequestDTO;

public class RequestDetail {
    private String targetLevel;
    private String linkedinUrl;

    public RequestDetail(String targetLevel, String linkedinUrl) {
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

    public static RequestDetail getDefaultRequestDetail() {
        return new RequestDetail("Senior Data Scientist", "www.linkedin.com/mmasoomi");
    }

    public static RequestDetail of(RoadmapRequestDTO requestDTO) {
        return new RequestDetail(requestDTO.getTargetLevel(), requestDTO.getLinkedinUrl());
    }
}
