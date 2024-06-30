package com.linchpino.ai.service.domain;

import com.linchpino.ai.controller.dto.RoadmapRequestDTO;

public class RequestDetail {
    private String fullName;
    private String field;
    private String level;
    private String targetLevel;
    private String goal;
    private String linkedinUrl;

    public RequestDetail(String fullName, String field, String level, String targetLevel, String goal, String linkedinUrl) {
        this.fullName = fullName;
        this.field = field;
        this.level = level;
        this.targetLevel = targetLevel;
        this.goal = goal;
        this.linkedinUrl = linkedinUrl;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(String targetLevel) {
        this.targetLevel = targetLevel;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public static RequestDetail getDefaultRequestDetail() {
        return new RequestDetail("John Doe", "Data Science", "Junior Data Scientist", "Senior Data Scientist", "Data Scientist", "linkedProfile");
    }

    public static RequestDetail of(RoadmapRequestDTO requestDTO) {
        return new RequestDetail(requestDTO.getFullName(), requestDTO.getField(), requestDTO.getLevel(), requestDTO.getTargetLevel(), requestDTO.getGoal(), requestDTO.getLinkedinUrl());
    }
}
