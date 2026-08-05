package com.pfe.itsm.n0.ai;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ConversationPolicyGenerator {

    private final LlmClient llmClient;

    public ConversationPolicyGenerator(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public ConversationPolicy classify(String sanitizedMessage) {
        if (sanitizedMessage == null || sanitizedMessage.isBlank()) {
            return vague("Decrivez le probleme rencontre, avec le service concerne et le message d'erreur si disponible.");
        }

        if (!llmClient.isConfigured()) {
            return heuristic(sanitizedMessage);
        }

        try {
            GeneratedAnswer answer = llmClient.generateAnswer(buildPrompt(sanitizedMessage));
            return parse(answer.contenu(), sanitizedMessage);
        } catch (RuntimeException exception) {
            return heuristic(sanitizedMessage);
        }
    }

    private String buildPrompt(String sanitizedMessage) {
        return """
                Tu es le module de pilotage conversationnel d'AssistEX, assistant N0 ITSM.
                Tu ne dois PAS donner de procedure technique.
                Tu dois uniquement classifier le message utilisateur et choisir s'il faut demander une precision avant la recherche RAG.

                Regles:
                - Si le message est seulement une salutation, intent=SALUTATION.
                - Si le probleme est trop vague, intent=INCIDENT_VAGUE et pose UNE question courte.
                - Un message comme "erreur de connexion" est vague si le service n'est pas precise.
                - Ne devine jamais VPN, Outlook, SAP, Wi-Fi, imprimante ou autre service sans indice explicite.
                - Si le service/application, le symptome ou le code erreur est assez clair, intent=INCIDENT_SPECIFIQUE.
                - Si l'utilisateur dit que c'est resolu, intent=CONFIRMATION_RESOLU.
                - Si l'utilisateur demande un ticket ou une escalade, intent=DEMANDE_ESCALADE.

                Reponds STRICTEMENT dans ce format:
                intent=<SALUTATION|INCIDENT_VAGUE|INCIDENT_SPECIFIQUE|CONFIRMATION_RESOLU|DEMANDE_ESCALADE|HORS_SUJET>
                question=<question courte ou vide>
                confidence=<nombre entre 0 et 1>

                Message utilisateur sanitise:
                """ + sanitizedMessage;
    }

    private ConversationPolicy parse(String content, String originalMessage) {
        ConversationIntent intent = null;
        String question = "";
        double confidence = 0.65;

        for (String rawLine : content.split("\\R")) {
            String line = rawLine.trim();
            if (line.startsWith("intent=")) {
                intent = parseIntent(line.substring("intent=".length()));
            } else if (line.startsWith("question=")) {
                question = line.substring("question=".length()).trim();
            } else if (line.startsWith("confidence=")) {
                confidence = parseConfidence(line.substring("confidence=".length()));
            }
        }

        if (intent == null) {
            return heuristic(originalMessage);
        }
        if (intent == ConversationIntent.INCIDENT_VAGUE && question.isBlank()) {
            question = "Quel service est concerne et quel message d'erreur voyez-vous exactement ?";
        }
        return new ConversationPolicy(intent, question, confidence);
    }

    private ConversationIntent parseIntent(String value) {
        try {
            return ConversationIntent.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private double parseConfidence(String value) {
        try {
            return Math.max(0, Math.min(1, Double.parseDouble(value.trim().replace(',', '.'))));
        } catch (NumberFormatException exception) {
            return 0.65;
        }
    }

    private ConversationPolicy heuristic(String message) {
        String normalized = message.toLowerCase(Locale.ROOT).trim();
        boolean hasSupportSignal = containsAny(normalized,
                "erreur", "probleme", "connexion", "connecter", "bloque", "impossible",
                "vpn", "wifi", "sap", "outlook", "mail", "imprimante", "mot de passe");

        if (!hasSupportSignal && containsAny(normalized, "bonjour", "salut", "hello", "bonsoir")) {
            return new ConversationPolicy(ConversationIntent.SALUTATION, "", 0.85);
        }
        if (containsAny(normalized, "resolu", "c'est bon", "ca marche", "merci")) {
            return new ConversationPolicy(ConversationIntent.CONFIRMATION_RESOLU, "", 0.75);
        }
        if (containsAny(normalized, "ticket", "escalade", "technicien", "n1")) {
            return new ConversationPolicy(ConversationIntent.DEMANDE_ESCALADE, "", 0.75);
        }
        if (isVagueIssue(normalized)) {
            return vague("Quel service est concerne : Internet/Wi-Fi, VPN, Outlook, SAP ou une autre application ? Indiquez aussi le message d'erreur exact si vous l'avez.");
        }
        if (hasSupportSignal) {
            return new ConversationPolicy(ConversationIntent.INCIDENT_SPECIFIQUE, "", 0.7);
        }
        return new ConversationPolicy(
                ConversationIntent.HORS_SUJET,
                "Je peux vous aider sur un incident IT. Quel service ou equipement pose probleme ?",
                0.55
        );
    }

    private boolean isVagueIssue(String normalized) {
        if (containsAny(normalized, "vpn", "wifi", "sap", "outlook", "mail", "imprimante", "scanner")) {
            return false;
        }
        return containsAny(normalized, "erreur de connexion", "probleme de connexion", "connexion impossible")
                || normalized.equals("erreur")
                || normalized.equals("probleme")
                || normalized.equals("ca marche pas")
                || normalized.equals("ça marche pas");
    }

    private ConversationPolicy vague(String question) {
        return new ConversationPolicy(ConversationIntent.INCIDENT_VAGUE, question, 0.8);
    }

    private boolean containsAny(String value, String... fragments) {
        for (String fragment : fragments) {
            if (value.contains(fragment)) {
                return true;
            }
        }
        return false;
    }
}
