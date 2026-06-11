package com.pfe.itsm.n0.service;

import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.common.ResourceNotFoundException;
import com.pfe.itsm.n0.domain.KnowledgeArticle;
import com.pfe.itsm.n0.domain.KnowledgeSection;
import com.pfe.itsm.n0.domain.KnowledgeSectionType;
import com.pfe.itsm.n0.dto.SemanticReasoningResponse;
import com.pfe.itsm.n0.dto.SparqlQueryResponse;
import com.pfe.itsm.n0.repository.KnowledgeArticleRepository;
import com.pfe.itsm.n0.repository.KnowledgeSectionRepository;
import com.pfe.itsm.n0.semantic.ItsmOntology;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SemanticGraphService {

    private static final String DEFAULT_PREFIXES = """
            prefix itsm: <https://pfe.local/itsm/ontology#>
            prefix res: <https://pfe.local/itsm/resource/>
            prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            """;

    private final KnowledgeArticleRepository articleRepository;
    private final KnowledgeSectionRepository sectionRepository;

    public SemanticGraphService(
            KnowledgeArticleRepository articleRepository,
            KnowledgeSectionRepository sectionRepository
    ) {
        this.articleRepository = articleRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional(readOnly = true)
    public String activeModelAsTurtle() {
        return writeTurtle(buildActiveModel());
    }

    @Transactional(readOnly = true)
    public String articleModelAsTurtle(UUID articleId) {
        KnowledgeArticle article = findArticle(articleId);
        return writeTurtle(buildArticleModel(article, sectionRepository.findByArticleIdOrderByOrdreAsc(articleId)));
    }

    @Transactional(readOnly = true)
    public SparqlQueryResponse query(String sparql) {
        if (!sparql.trim().toLowerCase(Locale.ROOT).startsWith("select")) {
            throw new BusinessException("Seules les requetes SPARQL SELECT sont autorisees.");
        }
        try (var execution = QueryExecutionFactory.create(QueryFactory.create(withPrefixes(sparql)), buildActiveModel())) {
            ResultSet resultSet = execution.execSelect();
            List<String> variables = resultSet.getResultVars();
            List<Map<String, String>> rows = new ArrayList<>();
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();
                Map<String, String> row = new LinkedHashMap<>();
                for (String variable : variables) {
                    if (solution.contains(variable)) {
                        row.put(variable, solution.get(variable).toString());
                    }
                }
                rows.add(row);
            }
            return new SparqlQueryResponse(variables, rows);
        } catch (RuntimeException exception) {
            throw new BusinessException("Requete SPARQL invalide.");
        }
    }

    @Transactional(readOnly = true)
    public SemanticReasoningResponse reasonForArticle(UUID articleId) {
        KnowledgeArticle article = findArticle(articleId);
        List<KnowledgeSection> sections = sectionRepository.findByArticleIdOrderByOrdreAsc(articleId);
        return reasonFromSections(article, sections);
    }

    private Model buildActiveModel() {
        Model model = baseModel();
        List<KnowledgeSection> sections = sectionRepository.findByArticleActifTrueOrderByArticleIdAscOrdreAsc();
        Map<UUID, List<KnowledgeSection>> sectionsByArticle = new LinkedHashMap<>();
        for (KnowledgeSection section : sections) {
            sectionsByArticle
                    .computeIfAbsent(section.getArticle().getId(), ignored -> new ArrayList<>())
                    .add(section);
        }
        for (Map.Entry<UUID, List<KnowledgeSection>> entry : sectionsByArticle.entrySet()) {
            addArticle(model, entry.getValue().getFirst().getArticle(), entry.getValue());
        }
        return model;
    }

    private Model buildArticleModel(KnowledgeArticle article, List<KnowledgeSection> sections) {
        Model model = baseModel();
        addArticle(model, article, sections);
        return model;
    }

    private Model baseModel() {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("itsm", ItsmOntology.NS);
        model.setNsPrefix("res", ItsmOntology.RESOURCE_NS);
        return model;
    }

    private void addArticle(Model model, KnowledgeArticle article, List<KnowledgeSection> sections) {
        Resource articleResource = articleResource(model, article);
        Resource categoryResource = resource(model, "category/" + article.getCategorie().name());

        model.add(articleResource, RDF.type, ItsmOntology.ARTICLE);
        model.add(articleResource, RDFS.label, article.getTitre());
        model.add(articleResource, ItsmOntology.belongsToCategory, categoryResource);
        model.add(categoryResource, RDF.type, ItsmOntology.CATEGORY);
        model.add(categoryResource, RDFS.label, article.getCategorie().name());

        SemanticReasoningResponse reasoning = reasonFromSections(article, sections);
        if (reasoning.niveauEscalade() != null) {
            Resource supportLevel = resource(model, "support-level/" + reasoning.niveauEscalade());
            model.add(supportLevel, RDF.type, ItsmOntology.SUPPORT_LEVEL);
            model.add(supportLevel, RDFS.label, reasoning.niveauEscalade());
            model.add(articleResource, ItsmOntology.escalatesTo, supportLevel);
        }

        for (KnowledgeSection section : sections) {
            addSection(model, articleResource, section);
        }
    }

    private void addSection(Model model, Resource articleResource, KnowledgeSection section) {
        Resource sectionResource = resource(
                model,
                "section/" + section.getId()
        );
        model.add(sectionResource, RDF.type, ItsmOntology.SECTION);
        model.add(sectionResource, RDFS.label, section.getTitre());
        model.add(sectionResource, ItsmOntology.sourceText, section.getContenu());
        model.add(articleResource, ItsmOntology.hasSection, sectionResource);

        Resource concept = conceptResource(model, section);
        model.add(concept, ItsmOntology.documentedIn, articleResource);
        model.add(concept, ItsmOntology.sourceText, section.getContenu());

        switch (section.getType()) {
            case SYMPTOMES -> {
                model.add(concept, RDF.type, ItsmOntology.SYMPTOM);
                model.add(articleResource, ItsmOntology.hasSymptom, concept);
            }
            case CAUSES -> {
                model.add(concept, RDF.type, ItsmOntology.CAUSE);
                model.add(articleResource, ItsmOntology.hasCause, concept);
            }
            case PROCEDURE -> {
                model.add(concept, RDF.type, ItsmOntology.SOLUTION);
                model.add(articleResource, ItsmOntology.hasSolution, concept);
            }
            case VERIFICATION -> {
                model.add(concept, RDF.type, ItsmOntology.VERIFICATION);
                model.add(articleResource, ItsmOntology.hasVerification, concept);
            }
            case ESCALADE -> {
                model.add(concept, RDF.type, ItsmOntology.ESCALATION_RULE);
                model.add(articleResource, ItsmOntology.hasEscalationRule, concept);
            }
            case PREREQUIS, AUTRE -> model.add(concept, RDF.type, ItsmOntology.SECTION);
        }
    }

    private SemanticReasoningResponse reasonFromSections(KnowledgeArticle article, List<KnowledgeSection> sections) {
        List<String> symptoms = new ArrayList<>();
        List<String> causes = new ArrayList<>();
        List<String> solutions = new ArrayList<>();
        List<String> verifications = new ArrayList<>();
        List<String> escalationRules = new ArrayList<>();
        String escalationLevel = null;

        for (KnowledgeSection section : sections) {
            String text = section.getContenu();
            switch (section.getType()) {
                case SYMPTOMES -> symptoms.add(text);
                case CAUSES -> causes.add(text);
                case PROCEDURE -> solutions.add(text);
                case VERIFICATION -> verifications.add(text);
                case ESCALADE -> {
                    escalationRules.add(text);
                    escalationLevel = detectSupportLevel(text);
                }
                case PREREQUIS, AUTRE -> {
                    // These sections remain searchable but do not produce direct reasoning hints.
                }
            }
        }

        return new SemanticReasoningResponse(
                article.getId(),
                article.getTitre(),
                article.getCategorie().name(),
                escalationLevel,
                symptoms,
                causes,
                solutions,
                verifications,
                escalationRules
        );
    }

    private String detectSupportLevel(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        if (normalized.contains("n3")) {
            return "N3";
        }
        if (normalized.contains("n2")) {
            return "N2";
        }
        if (normalized.contains("n1")) {
            return "N1";
        }
        return null;
    }

    private Resource conceptResource(Model model, KnowledgeSection section) {
        return resource(model, "concept/" + section.getType().name().toLowerCase(Locale.ROOT) + "/" + section.getId());
    }

    private Resource articleResource(Model model, KnowledgeArticle article) {
        return resource(model, "article/" + article.getId());
    }

    private Resource resource(Model model, String path) {
        return model.createResource(ItsmOntology.RESOURCE_NS + path);
    }

    private KnowledgeArticle findArticle(UUID articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article de connaissance introuvable."));
    }

    private String writeTurtle(Model model) {
        StringWriter writer = new StringWriter();
        model.write(writer, "TURTLE");
        return writer.toString();
    }

    private String withPrefixes(String sparql) {
        return sparql.toLowerCase(Locale.ROOT).contains("prefix ") ? sparql : DEFAULT_PREFIXES + sparql;
    }
}
