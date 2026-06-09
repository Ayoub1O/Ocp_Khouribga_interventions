package com.pfe.itsm.dashboard.dto;

public record CountByLabelResponse(
        String libelle,
        long total
) {
}
