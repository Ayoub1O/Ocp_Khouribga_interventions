package com.pfe.itsm.n0.controller;

import com.pfe.itsm.n0.dto.KnowledgeArticleRequest;
import com.pfe.itsm.n0.dto.KnowledgeArticleResponse;
import com.pfe.itsm.n0.dto.KnowledgeImportResponse;
import com.pfe.itsm.n0.service.KnowledgeBaseService;
import com.pfe.itsm.tickets.domain.TicketCategory;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/knowledge")
@PreAuthorize("hasRole('ADMIN')")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @GetMapping("/articles")
    public List<KnowledgeArticleResponse> list() {
        return knowledgeBaseService.list();
    }

    @GetMapping("/articles/{id}")
    public KnowledgeArticleResponse get(@PathVariable UUID id) {
        return knowledgeBaseService.get(id);
    }

    @PostMapping("/articles")
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeArticleResponse create(@Valid @RequestBody KnowledgeArticleRequest request) {
        return knowledgeBaseService.create(request);
    }

    @PostMapping(value = "/imports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public KnowledgeImportResponse importDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("categorie") TicketCategory categorie,
            @RequestParam("motsCles") String motsCles
    ) {
        return knowledgeBaseService.importDocument(file, categorie, motsCles);
    }

    @PatchMapping("/articles/{id}")
    public KnowledgeArticleResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody KnowledgeArticleRequest request
    ) {
        return knowledgeBaseService.update(id, request);
    }
}
