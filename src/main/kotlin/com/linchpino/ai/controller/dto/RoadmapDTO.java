package com.linchpino.ai.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.linchpino.ai.service.domain.Roadmap;

import java.io.Serializable;
import java.util.List;

public class RoadmapDTO implements Serializable {
    private int level;
    private int targetLevel;
    private String goal;
    private List<Step> steps;

    // Getters and setters

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

    public static class Step implements Serializable {
        @JsonProperty("step")
        private String section;
        private String description;
        private List<SubStep> subSteps;

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
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

        public static class SubStep implements Serializable {
            @JsonProperty("subStep")
            private String subSection;
            private String description;

            public String getSubSection() {
                return subSection;
            }

            public void setSubSection(String subSection) {
                this.subSection = subSection;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }
        }
    }

    public static RoadmapDTO of(Roadmap roadmap) {
        RoadmapDTO roadmapDTO = new RoadmapDTO();
        roadmapDTO.setLevel(roadmap.getLevel());
        roadmapDTO.setTargetLevel(roadmap.getTargetLevel());
        roadmapDTO.setGoal(roadmap.getGoal());
        roadmapDTO.setSteps(getSteps(roadmap.getSteps()));
        return roadmapDTO;
    }

    private static List<Step> getSteps(List<com.linchpino.ai.service.domain.Roadmap.Step> steps) {
        return steps.stream()
            .map(step -> {
                Step stepDTO = new Step();
                stepDTO.setSection(step.getSection());
                stepDTO.setDescription(step.getDescription());
                stepDTO.setSubSteps(getSubSteps(step.getSubSteps()));
                return stepDTO;
            })
            .toList();
    }

    private static List<Step.SubStep> getSubSteps(List<Roadmap.SubStep> subSteps) {
        return subSteps.stream()
            .map(subStep -> {
                Step.SubStep subStepDTO = new Step.SubStep();
                subStepDTO.setSubSection(subStep.getSubSection());
                subStepDTO.setDescription(subStep.getDescription());
                return subStepDTO;
            })
            .toList();
    }
}
