package com.pfe.itsm.n0.dto;

import java.util.List;
import java.util.UUID;

public record SemanticReasoningResponse(
        UUID articleId,
        String articleTitre,
        String categorie,
        String niveauEscalade,
        List<String> symptomes,
        List<String> causes,
        List<String> solutions,
        List<String> verifications,
        List<String> reglesEscalade
) {

    public boolean hasEscalationHint() {
        return niveauEscalade != null && !niveauEscalade.isBlank();
    }
}
