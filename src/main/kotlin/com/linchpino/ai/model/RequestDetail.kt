package com.linchpino.ai.model;

public class RequestDetail {
    private String targetLevel;
    private Resume resume;

    public RequestDetail(String targetLevel, Resume resume) {
        this.targetLevel = targetLevel;
        this.resume = resume;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(String targetLevel) {
        this.targetLevel = targetLevel;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }
}
