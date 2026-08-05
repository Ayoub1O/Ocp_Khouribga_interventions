package com.pfe.itsm.n0.ai;

import com.pfe.itsm.n0.domain.KnowledgeChunk;
import com.pfe.itsm.n0.dto.SemanticReasoningResponse;
import com.pfe.itsm.tickets.domain.TicketCategory;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChatbotAnswerGenerator {

    private final LlmClient llmClient;

    public ChatbotAnswerGenerator(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public boolean isAvailable() {
        return llmClient.isConfigured();
    }

    public GeneratedAnswer generate(
            String sanitizedQuestion,
            TicketCategory category,
            List<KnowledgeChunk> chunks,
            SemanticReasoningResponse reasoning) {
        return llmClient.generateAnswer(buildPrompt(sanitizedQuestion, category, chunks, reasoning));
    }

    private String buildPrompt(
            String sanitizedQuestion,
            TicketCategory category,
            List<KnowledgeChunk> chunks,
            SemanticReasoningResponse reasoning) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(
                """
                        Tu es l'assistant N0 d'une plateforme ITSM interne.
                        Reponds en francais professionnel, clair et actionnable.
                        Ne demande jamais un mot de passe, un token, une cle API ou une information secrete.
                        Tu peux proposer des corrections courantes issues de tes connaissances generales IT, mais sans citer de sites externes.
                        Utilise en priorite le contexte interne fourni.
                        Ne devine jamais le service concerne: VPN, Wi-Fi, Outlook, SAP, imprimante ou autre doivent etre deduits d'indices explicites.
                        Si la demande reste vague, pose une seule question de clarification au lieu de donner une procedure.
                        Si les informations sont insuffisantes ou si une action technicien est necessaire, recommande une escalade vers N1.
                        L'escalade n'est jamais automatique: elle doit etre confirmee par l'utilisateur.
                        Termine par exactement une ligne: Escalade recommandee: oui|non

                        Question utilisateur sanitisee:
                        """);
        prompt.append(sanitizedQuestion).append("\n\n");

        prompt.append("Categorie detectee: ")
                .append(category == null ? "INCONNUE" : category)
                .append("\n\n");

        prompt.append("Contexte interne recupere:\n");
        if (chunks.isEmpty()) {
            prompt.append("- Aucun article interne suffisamment proche.\n");
        } else {
            for (int i = 0; i < chunks.size(); i++) {
                KnowledgeChunk chunk = chunks.get(i);
                prompt.append("[Source ").append(i + 1).append("] ")
                        .append(chunk.getArticle().getTitre())
                        .append(" v").append(chunk.getArticle().getVersion())
                        .append(" / ").append(chunk.getSectionType())
                        .append("\n")
                        .append(chunk.getContenu())
                        .append("\n\n");
            }
        }

        if (reasoning != null && reasoning.hasEscalationHint()) {
            prompt.append("Raisonnement semantique RDF: niveau lie ")
                    .append(reasoning.niveauEscalade())
                    .append(". Le workflow reste N0 -> N1 -> N2 -> N3, sans saut de niveau.\n\n");
        }

        prompt.append("""
                Format attendu:
                - Diagnostic probable en une phrase.
                - Etapes de verification/correction numerotees.
                - Question finale demandant si le probleme est resolu ou si l'utilisateur veut creer un ticket N1.
                """);
        return prompt.toString();
    }
}
