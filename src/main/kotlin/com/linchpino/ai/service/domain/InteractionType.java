package com.linchpino.ai.service.domain;

import java.util.Arrays;

public enum InteractionType {
    PROMPT("Prompt", true), // Default by hardcode if there were no option isDefault: true
    FUNCTION_CALL("Function call", false);

    private final String label;
    private final boolean isDefault;

    InteractionType(String label, boolean isDefault) {
        this.label = label;
        this.isDefault = isDefault;
    }

    public String getLabel() {
        return label;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public static InteractionType getDefault() {
        return Arrays.stream(InteractionType.values()).filter(InteractionType::isDefault).findFirst().orElse(PROMPT); // Prompt is hardcoded if no option have isDefault true
    }

    public static InteractionType getInteractionTypeOrDefault(String name) {
        return Arrays.stream(InteractionType.values()).filter(i -> i.getLabel().equalsIgnoreCase(name)).findFirst().orElse(getDefault());
    }

    public boolean isFunctionCall() {
        return this.equals(InteractionType.FUNCTION_CALL);
    }

    public boolean isPrompt() {
        return this.equals(InteractionType.PROMPT);
    }
}
