package com.pfe.itsm.n0.dto;

import java.util.List;
import java.util.Map;

public record SparqlQueryResponse(
        List<String> variables,
        List<Map<String, String>> rows
) {
}
