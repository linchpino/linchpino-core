package com.linchpino.ai.service.model;

import java.io.File;

public class RequestDetail {
    private String targetLevel;
    private ResumePDF resumePDF;

    public RequestDetail(String targetLevel, ResumePDF resumePDF) {
        this.targetLevel = targetLevel;
        this.resumePDF = resumePDF;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(String targetLevel) {
        this.targetLevel = targetLevel;
    }

    public ResumePDF getResumePDF() {
        return resumePDF;
    }

    public void setResumePDF(ResumePDF resumePDF) {
        this.resumePDF = resumePDF;
    }

    public static RequestDetail of(String targetLevel, File resumeFile) {
        if (targetLevel == null || targetLevel.isEmpty()) {
            throw new IllegalArgumentException("Target level cannot be empty");
        }
        if (resumeFile == null) {
            throw new IllegalArgumentException("Resume file cannot be null");
        }
        return new RequestDetail(targetLevel, new ResumePDF(resumeFile));
    }
}
