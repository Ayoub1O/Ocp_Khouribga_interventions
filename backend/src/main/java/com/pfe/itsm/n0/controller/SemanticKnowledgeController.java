package com.pfe.itsm.n0.controller;

import com.pfe.itsm.n0.dto.SemanticReasoningResponse;
import com.pfe.itsm.n0.dto.SparqlQueryRequest;
import com.pfe.itsm.n0.dto.SparqlQueryResponse;
import com.pfe.itsm.n0.service.SemanticGraphService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge/semantic")
@PreAuthorize("hasRole('ADMIN')")
public class SemanticKnowledgeController {

    private static final String TEXT_TURTLE = "text/turtle;charset=UTF-8";

    private final SemanticGraphService semanticGraphService;

    public SemanticKnowledgeController(SemanticGraphService semanticGraphService) {
        this.semanticGraphService = semanticGraphService;
    }

    @GetMapping(value = "/model", produces = TEXT_TURTLE)
    public String activeModel() {
        return semanticGraphService.activeModelAsTurtle();
    }

    @GetMapping(value = "/articles/{id}/triples", produces = TEXT_TURTLE)
    public String articleTriples(@PathVariable UUID id) {
        return semanticGraphService.articleModelAsTurtle(id);
    }

    @GetMapping("/articles/{id}/reasoning")
    public SemanticReasoningResponse articleReasoning(@PathVariable UUID id) {
        return semanticGraphService.reasonForArticle(id);
    }

    @PostMapping(
            value = "/sparql",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SparqlQueryResponse sparql(@Valid @RequestBody SparqlQueryRequest request) {
        return semanticGraphService.query(request.query());
    }
}
