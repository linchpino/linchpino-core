package com.linchpino.ai.controller.dto;

import java.io.Serializable;

public class RoadmapRequestDTO implements Serializable {
    private String fullName;
    private String field;
    private String level;
    private String targetLevel;
    private String goal;
    private String linkedinUrl;

    public RoadmapRequestDTO() {}

    public RoadmapRequestDTO(String fullName, String field, String level, String targetLevel, String goal) {
        this.fullName = fullName;
        this.field = field;
        this.level = level;
        this.targetLevel = targetLevel;
        this.goal = goal;
    }

    public RoadmapRequestDTO(String targetLevel, String linkedinUrl) {
        this.targetLevel = targetLevel;
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
}
