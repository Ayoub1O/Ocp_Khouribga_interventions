package com.pfe.itsm.n0.service;

import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.common.ResourceNotFoundException;
import com.pfe.itsm.n0.domain.KnowledgeArticle;
import com.pfe.itsm.n0.domain.KnowledgeChunk;
import com.pfe.itsm.n0.domain.KnowledgeSection;
import com.pfe.itsm.n0.domain.KnowledgeSectionType;
import com.pfe.itsm.n0.dto.KnowledgeImportResponse;
import com.pfe.itsm.n0.dto.KnowledgeArticleRequest;
import com.pfe.itsm.n0.dto.KnowledgeArticleResponse;
import com.pfe.itsm.n0.repository.KnowledgeArticleRepository;
import com.pfe.itsm.n0.repository.KnowledgeChunkRepository;
import com.pfe.itsm.n0.repository.KnowledgeSectionRepository;
import com.pfe.itsm.tickets.domain.TicketCategory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class KnowledgeBaseService {

    private static final int CHUNK_SIZE = 900;
    private static final long MAX_IMPORT_BYTES = 2 * 1024 * 1024;

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final KnowledgeSectionRepository sectionRepository;

    public KnowledgeBaseService(
            KnowledgeArticleRepository articleRepository,
            KnowledgeChunkRepository chunkRepository,
            KnowledgeSectionRepository sectionRepository
    ) {
        this.articleRepository = articleRepository;
        this.chunkRepository = chunkRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeArticleResponse> list() {
        return articleRepository.findAll()
                .stream()
                .map(KnowledgeArticleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public KnowledgeArticleResponse get(UUID id) {
        return KnowledgeArticleResponse.from(findArticle(id));
    }

    @Transactional
    public KnowledgeArticleResponse create(KnowledgeArticleRequest request) {
        KnowledgeArticle article = articleRepository.save(new KnowledgeArticle(
                request.titre().trim(),
                request.categorie(),
                request.contenu().trim(),
                request.motsCles().trim(),
                request.actif()
        ));
        regenerateChunks(article);
        return KnowledgeArticleResponse.from(article);
    }

    @Transactional
    public KnowledgeImportResponse importDocument(MultipartFile file, TicketCategory categorie, String motsCles) {
        validateImport(file, motsCles);
        String filename = safeFilename(file.getOriginalFilename());
        String content = extractText(file);
        String title = titleFrom(filename, content);
        String normalizedContent = normalizeContent(content);

        KnowledgeArticle article = articleRepository.save(new KnowledgeArticle(
                title,
                categorie,
                normalizedContent,
                motsCles.trim(),
                false,
                "IMPORT",
                filename
        ));
        int chunks = regenerateChunks(article);
        return new KnowledgeImportResponse(
                KnowledgeArticleResponse.from(article),
                chunks,
                "Document importe comme brouillon inactif. Validation administrateur requise avant utilisation par N0."
        );
    }

    @Transactional
    public KnowledgeArticleResponse update(UUID id, KnowledgeArticleRequest request) {
        KnowledgeArticle article = findArticle(id);
        article.update(
                request.titre().trim(),
                request.categorie(),
                request.contenu().trim(),
                request.motsCles().trim(),
                request.actif()
        );
        regenerateChunks(article);
        return KnowledgeArticleResponse.from(article);
    }

    private int regenerateChunks(KnowledgeArticle article) {
        chunkRepository.deleteByArticleId(article.getId());
        sectionRepository.deleteByArticleId(article.getId());

        List<ExtractedSection> sections = extractSections(article.getContenu());
        int chunkOrder = 0;
        for (int sectionIndex = 0; sectionIndex < sections.size(); sectionIndex++) {
            ExtractedSection section = sections.get(sectionIndex);
            sectionRepository.save(new KnowledgeSection(
                    article,
                    section.type(),
                    section.title(),
                    section.content(),
                    sectionIndex
            ));
            for (String chunk : splitIntoChunks(section.content())) {
                chunkRepository.save(new KnowledgeChunk(
                        article,
                        chunk,
                        article.getMotsCles(),
                        section.type(),
                        chunkOrder++,
                        article.isActif()
                ));
            }
        }
        return chunkOrder;
    }

    private KnowledgeArticle findArticle(UUID id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article de connaissance introuvable."));
    }

    private List<String> splitIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        String remaining = content.trim();
        while (remaining.length() > CHUNK_SIZE) {
            int split = remaining.lastIndexOf(". ", CHUNK_SIZE);
            if (split < CHUNK_SIZE / 2) {
                split = CHUNK_SIZE;
            } else {
                split += 1;
            }
            chunks.add(remaining.substring(0, split).trim());
            remaining = remaining.substring(split).trim();
        }
        if (!remaining.isBlank()) {
            chunks.add(remaining);
        }
        return chunks;
    }

    private List<ExtractedSection> extractSections(String content) {
        List<ExtractedSection> sections = new ArrayList<>();
        KnowledgeSectionType currentType = KnowledgeSectionType.AUTRE;
        String currentTitle = "Contenu";
        StringBuilder currentContent = new StringBuilder();

        for (String line : content.split("\\n")) {
            Heading heading = parseHeading(line);
            if (heading != null) {
                addSectionIfPresent(sections, currentType, currentTitle, currentContent);
                currentType = heading.type();
                currentTitle = heading.title();
                currentContent = new StringBuilder();
            } else if (!line.isBlank()) {
                currentContent.append(line.trim()).append("\n");
            }
        }
        addSectionIfPresent(sections, currentType, currentTitle, currentContent);

        if (sections.isEmpty()) {
            sections.add(new ExtractedSection(KnowledgeSectionType.AUTRE, "Contenu", content.trim()));
        }
        return sections;
    }

    private void addSectionIfPresent(
            List<ExtractedSection> sections,
            KnowledgeSectionType type,
            String title,
            StringBuilder content
    ) {
        String value = content.toString().trim();
        if (!value.isBlank()) {
            sections.add(new ExtractedSection(type, title, value));
        }
    }

    private Heading parseHeading(String line) {
        String trimmed = line.trim();
        if (trimmed.isBlank()) {
            return null;
        }

        String title = trimmed;
        if (title.startsWith("#")) {
            title = title.replaceFirst("^#+", "").trim();
        } else if (title.endsWith(":")) {
            title = title.substring(0, title.length() - 1).trim();
        } else {
            return null;
        }

        KnowledgeSectionType type = sectionTypeFromTitle(title);
        return type == null ? null : new Heading(type, trimTitle(title));
    }

    private KnowledgeSectionType sectionTypeFromTitle(String title) {
        String normalized = title.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "symptome", "symptom", "probleme", "problem")) {
            return KnowledgeSectionType.SYMPTOMES;
        }
        if (containsAny(normalized, "cause", "root cause", "origine")) {
            return KnowledgeSectionType.CAUSES;
        }
        if (containsAny(normalized, "prerequis", "pre-requis", "prerequisite", "condition")) {
            return KnowledgeSectionType.PREREQUIS;
        }
        if (containsAny(normalized, "procedure", "resolution", "solution", "steps", "etapes", "fix")) {
            return KnowledgeSectionType.PROCEDURE;
        }
        if (containsAny(normalized, "verification", "test", "validation", "controle")) {
            return KnowledgeSectionType.VERIFICATION;
        }
        if (containsAny(normalized, "escalade", "escalation", "support level")) {
            return KnowledgeSectionType.ESCALADE;
        }
        return null;
    }

    private boolean containsAny(String value, String... fragments) {
        for (String fragment : fragments) {
            if (value.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private void validateImport(MultipartFile file, String motsCles) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Le fichier importe est obligatoire.");
        }
        if (file.getSize() > MAX_IMPORT_BYTES) {
            throw new BusinessException("Le fichier importe ne doit pas depasser 2 Mo.");
        }
        String filename = safeFilename(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!filename.endsWith(".txt") && !filename.endsWith(".md")) {
            throw new BusinessException("Seuls les fichiers .txt et .md sont acceptes pour cet import.");
        }
        if (motsCles == null || motsCles.isBlank()) {
            throw new BusinessException("Les mots-cles sont obligatoires pour indexer le document.");
        }
    }

    private String extractText(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String normalized = normalizeContent(content);
            if (normalized.length() < 30) {
                throw new BusinessException("Le document importe est trop court pour etre exploitable.");
            }
            if (normalized.length() > 4000) {
                throw new BusinessException("Le contenu extrait depasse 4000 caracteres. Decoupez le document en plusieurs procedures.");
            }
            return normalized;
        } catch (IOException exception) {
            throw new BusinessException("Impossible de lire le fichier importe.");
        }
    }

    private String normalizeContent(String content) {
        return content
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private String titleFrom(String filename, String content) {
        String firstLine = content.lines().findFirst().orElse("").trim();
        if (firstLine.startsWith("#")) {
            return trimTitle(firstLine.replaceFirst("^#+", "").trim());
        }
        String withoutExtension = filename.replaceFirst("\\.[^.]+$", "").replace('-', ' ').replace('_', ' ');
        return trimTitle(withoutExtension);
    }

    private String trimTitle(String title) {
        String value = title == null || title.isBlank() ? "Document importe" : title.trim();
        return value.length() <= 180 ? value : value.substring(0, 180);
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "document-importe.txt";
        }
        return filename.replace("\\", "/").substring(filename.replace("\\", "/").lastIndexOf('/') + 1);
    }

    private record Heading(KnowledgeSectionType type, String title) {
    }

    private record ExtractedSection(KnowledgeSectionType type, String title, String content) {
    }
}
