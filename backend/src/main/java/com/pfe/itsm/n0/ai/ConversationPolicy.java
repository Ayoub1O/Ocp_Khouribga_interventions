package com.pfe.itsm.n0.ai;

public record ConversationPolicy(
        ConversationIntent intent,
        String questionClarification,
        double confidence
) {

    public boolean shouldRunRag() {
        return intent == ConversationIntent.INCIDENT_SPECIFIQUE;
    }
}
