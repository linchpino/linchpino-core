package com.linchpino.ai.service.domain;

import java.util.List;

public class Roadmap {

    private RequestDetail requestDetail;
    private int level;
    private int targetLevel;
    private String goal;
    private List<Step> steps;

    public RequestDetail getRequestDetail() {
        return requestDetail;
    }

    public void setRequestDetail(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public static class Step {
        private double step;
        private String description;
        private List<SubStep> subSteps;

        public double getStep() {
            return step;
        }

        public void setStep(double step) {
            this.step = step;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<SubStep> getSubSteps() {
            return subSteps;
        }

        public void setSubSteps(List<SubStep> subSteps) {
            this.subSteps = subSteps;
        }
    }

    public static class SubStep {
        private double step;
        private String description;

        // Getters and setters

        public double getStep() {
            return step;
        }

        public void setStep(double step) {
            this.step = step;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
