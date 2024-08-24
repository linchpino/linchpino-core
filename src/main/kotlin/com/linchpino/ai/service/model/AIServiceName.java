package com.linchpino.ai.service.model;

import com.linchpino.ai.service.impl.ChatGPTServiceImpl;
import com.linchpino.ai.service.impl.GeminiServiceImpl;

public enum AIServiceName {
    CHATGPT("ChatGPT", ChatGPTServiceImpl.COMPONENT_NAME, false),
    GEMINI("Gemini", GeminiServiceImpl.COMPONENT_NAME, true);

    public final String label;
    public final String componentName;
    private final boolean isDefault;

    AIServiceName(String label, String componentName, boolean isDefault) {
        this.label = label;
        this.componentName = componentName;
        this.isDefault = isDefault;
    }

    public String getLabel() {
        return label;
    }

    public String getComponentName() {
        return componentName;
    }

    public static AIServiceName getDefault() {
        for (AIServiceName value : values()) {
            if (value.isDefault) {
                return value;
            }
        }
        return null;
    }

    public static AIServiceName getComponentNameOrDefault(String componentName) {
        for (AIServiceName value : values()) {
            if (value.componentName.equalsIgnoreCase(componentName)) {
                return value;
            }
        }
        return getDefault();
    }
}
