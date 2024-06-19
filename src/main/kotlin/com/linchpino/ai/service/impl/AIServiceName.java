package com.linchpino.ai.service.impl;

import java.util.Optional;

public enum AIServiceName {
    CHATGPT("ChatGPT", ChatGPTServiceImpl.COMPONENT_NAME),
    GEMINI("Gemini", GeminiServiceImpl.COMPONENT_NAME);

    public final String label;
    public final String componentName;

    AIServiceName(String label, String componentName) {
        this.label = label;
        this.componentName = componentName;
    }

    public String getLabel() {
        return label;
    }

    public String getComponentName() {
        return componentName;
    }

    public static Optional<AIServiceName> fromComponentName(String componentName) {
        for (AIServiceName value : values()) {
            if (value.componentName.equalsIgnoreCase(componentName)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
