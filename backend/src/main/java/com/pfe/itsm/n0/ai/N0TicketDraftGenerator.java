package com.pfe.itsm.n0.ai;

import com.pfe.itsm.tickets.domain.TicketCategory;
import com.pfe.itsm.tickets.domain.TicketPriority;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class N0TicketDraftGenerator {

    private static final int TITLE_LIMIT = 180;
    private static final int DESCRIPTION_LIMIT = 4000;

    private final LlmClient llmClient;

    public N0TicketDraftGenerator(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public N0TicketDraft generate(String sanitizedConversation, TicketCategory detectedCategory) {
        if (llmClient.isConfigured()) {
            try {
                N0TicketDraft draft = parse(llmClient.generateAnswer(buildPrompt(sanitizedConversation, detectedCategory)).contenu());
                if (hasText(draft.titre()) && hasText(draft.description())) {
                    return normalizeDraft(draft, detectedCategory, sanitizedConversation);
                }
            } catch (RuntimeException exception) {
                // Ticket creation must remain available even if the LLM drafting step is unavailable.
            }
        }
        return fallback(detectedCategory, sanitizedConversation);
    }

    private String buildPrompt(String sanitizedConversation, TicketCategory detectedCategory) {
        return """
                Tu transformes des elements d'incident AssistEX N0 en ticket ITSM professionnel.
                Ne copie pas les elements mot pour mot.
                Ne mentionne pas d'informations personnelles, mots de passe, tokens, emails, telephones ou secrets.
                Le titre doit etre specifique, court, et decrire le service + symptome si connus.
                La description doit etre courte: 2 a 4 phrases maximum.
                Elle doit resumer le probleme, le contexte connu, et pourquoi il est transmis a N1.
                Si une information manque, ecris qu'elle n'est pas encore precisee au lieu d'inventer.

                Categories autorisees:
                RESEAU, COMPTE_ACCES, EMAIL, IMPRIMANTE, SECURITE, MATERIEL, LOGICIEL, AUTRE

                Priorites autorisees:
                BASSE, NORMALE, HAUTE, CRITIQUE

                Reponds STRICTEMENT:
                titre=<titre>
                description=<description>
                categorie=<categorie>
                priorite=<priorite>

                Categorie detectee par le systeme: """ + (detectedCategory == null ? "AUTRE" : detectedCategory) + "\n\n"
                + "Elements d'incident sanitises:\n"
                + sanitizedConversation;
    }

    private N0TicketDraft parse(String content) {
        String titre = "";
        String description = "";
        TicketCategory categorie = TicketCategory.AUTRE;
        TicketPriority priorite = TicketPriority.NORMALE;

        for (String rawLine : content.split("\\R")) {
            String line = rawLine.trim();
            if (line.startsWith("titre=")) {
                titre = line.substring("titre=".length()).trim();
            } else if (line.startsWith("description=")) {
                description = line.substring("description=".length()).trim();
            } else if (line.startsWith("categorie=")) {
                categorie = parseCategory(line.substring("categorie=".length()));
            } else if (line.startsWith("priorite=")) {
                priorite = parsePriority(line.substring("priorite=".length()));
            }
        }
        return new N0TicketDraft(titre, description, categorie, priorite);
    }

    private N0TicketDraft normalizeDraft(N0TicketDraft draft, TicketCategory detectedCategory, String sanitizedConversation) {
        TicketCategory category = draft.categorie() == TicketCategory.AUTRE && detectedCategory != null
                ? detectedCategory
                : draft.categorie();
        return new N0TicketDraft(
                clamp(draft.titre(), TITLE_LIMIT),
                clamp(draft.description(), DESCRIPTION_LIMIT),
                category,
                draft.priorite() == null ? TicketPriority.NORMALE : draft.priorite()
        );
    }

    private N0TicketDraft fallback(TicketCategory detectedCategory, String sanitizedConversation) {
        TicketCategory category = detectedCategory == null ? TicketCategory.AUTRE : detectedCategory;
        String title = fallbackTitle(category, sanitizedConversation);
        String description = fallbackDescription(sanitizedConversation);
        return new N0TicketDraft(title, description, category, TicketPriority.NORMALE);
    }

    private String fallbackTitle(TicketCategory category, String context) {
        if (category == TicketCategory.AUTRE) {
            return "Incident signale via AssistEX";
        }
        return "Incident " + label(category).toLowerCase(Locale.ROOT) + " signale via AssistEX";
    }

    private String fallbackDescription(String context) {
        String problem = extractProblemStatement(context);
        if (problem.isBlank()) {
            return "Le demandeur a sollicite AssistEX pour un incident IT, mais les informations fournies restent incompletes. L'incident est transmis a la file N1 pour qualification.";
        }

        return "Le demandeur signale: " + stripTrailingPunctuation(problem) + ". "
                + "Les informations disponibles restent limitees et l'incident est transmis a la file N1 pour qualification et prise en charge.";
    }

    private String extractProblemStatement(String context) {
        StringBuilder builder = new StringBuilder();
        for (String rawLine : context.split("\\R")) {
            String line = rawLine.trim();
            if (!line.startsWith("- ")) {
                continue;
            }
            String statement = line.substring(2).trim();
            if (statement.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" ; ");
            }
            builder.append(statement);
        }
        return clamp(builder.toString(), 500);
    }

    private String stripTrailingPunctuation(String value) {
        return value.replaceAll("[\\s.。!?;:,]+$", "");
    }

    private TicketCategory parseCategory(String value) {
        try {
            return TicketCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return TicketCategory.AUTRE;
        }
    }

    private TicketPriority parsePriority(String value) {
        try {
            return TicketPriority.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return TicketPriority.NORMALE;
        }
    }

    private String label(TicketCategory category) {
        return category.name().replace('_', ' ');
    }

    private String clamp(String value, int limit) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.length() <= limit) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, limit - 3)).trim() + "...";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
