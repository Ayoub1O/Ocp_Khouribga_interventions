package com.pfe.itsm.n0.service;

import com.pfe.itsm.auth.security.CurrentUserService;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.common.ResourceNotFoundException;
import com.pfe.itsm.n0.ai.ChatbotAnswerGenerator;
import com.pfe.itsm.n0.ai.GeneratedAnswer;
import com.pfe.itsm.n0.ai.SensitiveDataSanitizer;
import com.pfe.itsm.n0.domain.ChatbotMessage;
import com.pfe.itsm.n0.domain.ChatbotMessageAuthor;
import com.pfe.itsm.n0.domain.ChatbotSession;
import com.pfe.itsm.n0.domain.ChatbotSessionStatus;
import com.pfe.itsm.n0.domain.KnowledgeChunk;
import com.pfe.itsm.n0.domain.KnowledgeSectionType;
import com.pfe.itsm.n0.dto.ChatbotAnswerResponse;
import com.pfe.itsm.n0.dto.ChatbotMessageResponse;
import com.pfe.itsm.n0.dto.ChatbotSessionResponse;
import com.pfe.itsm.n0.dto.SemanticReasoningResponse;
import com.pfe.itsm.n0.dto.SendChatbotMessageRequest;
import com.pfe.itsm.n0.dto.StartChatbotSessionRequest;
import com.pfe.itsm.n0.dto.VectorChunkMatch;
import com.pfe.itsm.n0.repository.ChatbotMessageRepository;
import com.pfe.itsm.n0.repository.ChatbotSessionRepository;
import com.pfe.itsm.n0.repository.KnowledgeChunkRepository;
import com.pfe.itsm.tickets.domain.TicketCategory;
import com.pfe.itsm.tickets.domain.TicketPriority;
import com.pfe.itsm.tickets.dto.CreateTicketRequest;
import com.pfe.itsm.tickets.dto.TicketResponse;
import com.pfe.itsm.tickets.service.TicketService;
import com.pfe.itsm.users.domain.UserAccount;
import com.pfe.itsm.users.domain.UserRole;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatbotService {

    private static final double ANSWER_THRESHOLD = 0.55;

    private final ChatbotSessionRepository sessionRepository;
    private final ChatbotMessageRepository messageRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final CurrentUserService currentUserService;
    private final TicketService ticketService;
    private final SemanticGraphService semanticGraphService;
    private final KnowledgeVectorService vectorService;
    private final SensitiveDataSanitizer sensitiveDataSanitizer;
    private final ChatbotAnswerGenerator answerGenerator;

    public ChatbotService(
            ChatbotSessionRepository sessionRepository,
            ChatbotMessageRepository messageRepository,
            KnowledgeChunkRepository chunkRepository,
            CurrentUserService currentUserService,
            TicketService ticketService,
            SemanticGraphService semanticGraphService,
            KnowledgeVectorService vectorService,
            SensitiveDataSanitizer sensitiveDataSanitizer,
            ChatbotAnswerGenerator answerGenerator
    ) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.chunkRepository = chunkRepository;
        this.currentUserService = currentUserService;
        this.ticketService = ticketService;
        this.semanticGraphService = semanticGraphService;
        this.vectorService = vectorService;
        this.sensitiveDataSanitizer = sensitiveDataSanitizer;
        this.answerGenerator = answerGenerator;
    }

    @Transactional
    public ChatbotSessionResponse startSession(StartChatbotSessionRequest request) {
        UserAccount demandeur = currentUserService.currentUser();
        if (demandeur.getRole() != UserRole.DEMANDEUR && demandeur.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Seul un demandeur peut ouvrir une session N0.");
        }

        ChatbotSession session = sessionRepository.save(new ChatbotSession(demandeur));
        saveMessage(
                session,
                ChatbotMessageAuthor.BOT,
                "Bonjour. Decrivez votre probleme informatique avec le plus de details possible.",
                null,
                null
        );

        if (request != null && hasText(request.messageInitial())) {
            handleMessage(session, request.messageInitial());
        }
        return ChatbotSessionResponse.from(session);
    }

    @Transactional
    public ChatbotAnswerResponse sendMessage(UUID sessionId, SendChatbotMessageRequest request) {
        ChatbotSession session = findSession(sessionId);
        requireOpen(session);
        return handleMessage(session, request.message());
    }

    @Transactional
    public ChatbotSessionResponse confirmResolution(UUID sessionId) {
        ChatbotSession session = findSession(sessionId);
        requireOpen(session);
        session.markResolved();
        saveMessage(
                session,
                ChatbotMessageAuthor.SYSTEME,
                "Resolution confirmee par le demandeur. Aucun ticket N1 n'a ete cree.",
                null,
                null
        );
        return ChatbotSessionResponse.from(session);
    }

    @Transactional
    public ChatbotAnswerResponse escalate(UUID sessionId) {
        ChatbotSession session = findSession(sessionId);
        requireOpen(session);

        TicketCategory category = session.getCategorieDetectee() == null
                ? TicketCategory.AUTRE
                : session.getCategorieDetectee();
        TicketResponse ticket = ticketService.create(new CreateTicketRequest(
                "Incident qualifie par N0",
                conversationSummary(session),
                category,
                TicketPriority.NORMALE
        ));

        session.markEscalated(ticket.id());
        ChatbotMessage message = saveMessage(
                session,
                ChatbotMessageAuthor.SYSTEME,
                "Session escaladee vers la file N1. Ticket cree: " + ticket.reference() + ".",
                null,
                null
        );

        return new ChatbotAnswerResponse(
                ChatbotSessionResponse.from(session),
                ChatbotMessageResponse.from(message),
                0,
                false,
                List.of(),
                ticket
        );
    }

    @Transactional(readOnly = true)
    public List<ChatbotMessageResponse> messages(UUID sessionId) {
        ChatbotSession session = findSession(sessionId);
        return messageRepository.findBySessionIdOrderByDateCreationAsc(session.getId())
                .stream()
                .map(ChatbotMessageResponse::from)
                .toList();
    }

    private ChatbotAnswerResponse handleMessage(ChatbotSession session, String userMessage) {
        UserAccount currentUser = currentUserService.currentUser();
        String sanitizedMessage = sensitiveDataSanitizer.sanitize(userMessage, currentUser);
        String normalized = normalize(sanitizedMessage);
        saveMessage(session, ChatbotMessageAuthor.UTILISATEUR, userMessage.trim(), null, null);

        TicketCategory category = detectCategory(normalized);
        if (category != null) {
            session.updateCategory(category);
        }

        List<ChunkMatch> matches = retrieveMatches(sanitizedMessage, normalized, session.getCategorieDetectee());
        ChunkMatch match = matches.stream()
                .max(Comparator.comparingDouble(ChunkMatch::score))
                .orElse(null);

        String content;
        String sources = null;
        double confidence = match == null ? 0 : match.score();
        SemanticReasoningResponse reasoning = match == null
                ? null
                : semanticGraphService.reasonForArticle(match.chunk().getArticle().getId());
        boolean escalationRecommended = confidence < ANSWER_THRESHOLD;

        if (answerGenerator.isAvailable() && (match != null || session.getCategorieDetectee() != null)) {
            GeneratedAnswer answer = generateWithFallback(sanitizedMessage, session.getCategorieDetectee(), matches, reasoning);
            content = answer.contenu();
            escalationRecommended = answer.escaladeRecommandee() || confidence < ANSWER_THRESHOLD;
            if (content.isBlank()) {
                content = fallbackAnswer(session, match, confidence, reasoning);
            }
            sources = sourceSummary(matches);
            if (confidence == 0) {
                confidence = 0.45;
            }
        } else if (match != null && confidence >= ANSWER_THRESHOLD) {
            content = "Voici une procedure documentee a essayer:\n\n"
                    + match.chunk().getContenu()
                    + semanticHint(reasoning)
                    + "\n\nLe probleme est-il resolu ?";
            sources = match.chunk().getArticle().getTitre() + " v" + match.chunk().getArticle().getVersion();
        } else if (session.getCategorieDetectee() == null) {
            content = "Je n'ai pas encore assez d'elements. Precisez l'application concernee, le message d'erreur ou le materiel impacte.";
        } else {
            content = "Je n'ai pas trouve de procedure interne suffisamment fiable. Je recommande une escalade vers N1."
                    + semanticHint(reasoning);
        }

        ChatbotMessage botMessage = saveMessage(
                session,
                ChatbotMessageAuthor.BOT,
                content,
                sources,
                confidence
        );

        return new ChatbotAnswerResponse(
                ChatbotSessionResponse.from(session),
                ChatbotMessageResponse.from(botMessage),
                confidence,
                escalationRecommended,
                sources == null || sources.isBlank() ? List.of() : List.of(sources),
                null
        );
    }

    private List<ChunkMatch> retrieveMatches(String sanitizedMessage, String normalized, TicketCategory category) {
        Map<UUID, ChunkMatch> matches = new LinkedHashMap<>();

        try {
            for (VectorChunkMatch vectorMatch : vectorService.search(sanitizedMessage)) {
                matches.put(vectorMatch.chunk().getId(), new ChunkMatch(
                        vectorMatch.chunk(),
                        Math.min(Math.max(vectorMatch.score(), 0), 0.95)
                ));
            }
        } catch (RuntimeException exception) {
            // Vector retrieval is an optimization. The controlled keyword path remains authoritative fallback.
        }

        chunkRepository.findByActifTrueAndArticleActifTrue()
                .stream()
                .map(chunk -> score(chunk, normalized, category))
                .filter(match -> match.score() > 0)
                .forEach(keywordMatch -> matches.merge(
                        keywordMatch.chunk().getId(),
                        keywordMatch,
                        (left, right) -> left.score() >= right.score() ? left : right
                ));

        return matches.values()
                .stream()
                .sorted(Comparator.comparingDouble(ChunkMatch::score).reversed())
                .limit(5)
                .toList();
    }

    private GeneratedAnswer generateWithFallback(
            String sanitizedMessage,
            TicketCategory category,
            List<ChunkMatch> matches,
            SemanticReasoningResponse reasoning
    ) {
        try {
            return answerGenerator.generate(
                    sanitizedMessage,
                    category,
                    matches.stream().map(ChunkMatch::chunk).limit(3).toList(),
                    reasoning
            );
        } catch (RuntimeException exception) {
            return new GeneratedAnswer("", true);
        }
    }

    private String fallbackAnswer(
            ChatbotSession session,
            ChunkMatch match,
            double confidence,
            SemanticReasoningResponse reasoning
    ) {
        if (match != null && confidence >= ANSWER_THRESHOLD) {
            return "Voici une procedure documentee a essayer:\n\n"
                    + match.chunk().getContenu()
                    + semanticHint(reasoning)
                    + "\n\nLe probleme est-il resolu ?";
        }
        if (session.getCategorieDetectee() == null) {
            return "Je n'ai pas encore assez d'elements. Precisez l'application concernee, le message d'erreur ou le materiel impacte.";
        }
        return "Je n'ai pas trouve de procedure interne suffisamment fiable. Je recommande une escalade vers N1."
                + semanticHint(reasoning);
    }

    private String sourceSummary(List<ChunkMatch> matches) {
        return matches.stream()
                .limit(3)
                .map(match -> match.chunk().getArticle().getTitre() + " v" + match.chunk().getArticle().getVersion())
                .distinct()
                .reduce((left, right) -> left + "; " + right)
                .orElse(null);
    }

    private ChatbotSession findSession(UUID sessionId) {
        UserAccount user = currentUserService.currentUser();
        if (user.getRole() == UserRole.ADMIN) {
            return sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Session N0 introuvable."));
        }
        return sessionRepository.findByIdAndDemandeurId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Session N0 introuvable."));
    }

    private void requireOpen(ChatbotSession session) {
        if (session.getStatut() != ChatbotSessionStatus.OUVERTE) {
            throw new BusinessException("Cette session N0 est deja terminee.");
        }
    }

    private ChatbotMessage saveMessage(
            ChatbotSession session,
            ChatbotMessageAuthor author,
            String content,
            String sources,
            Double confidence
    ) {
        return messageRepository.save(new ChatbotMessage(session, author, content, sources, confidence));
    }

    private ChunkMatch score(KnowledgeChunk chunk, String message, TicketCategory category) {
        double score = 0;
        if (category != null && chunk.getArticle().getCategorie() == category) {
            score += 0.35;
        }
        for (String keyword : chunk.getMotsCles().split(",")) {
            String normalizedKeyword = normalize(keyword);
            if (!normalizedKeyword.isBlank() && message.contains(normalizedKeyword)) {
                score += 0.20;
            }
        }
        if (message.contains(normalize(chunk.getArticle().getTitre()))) {
            score += 0.20;
        }
        if (chunk.getSectionType() == KnowledgeSectionType.PROCEDURE
                || chunk.getSectionType() == KnowledgeSectionType.VERIFICATION) {
            score += 0.10;
        }
        if (chunk.getSectionType() == KnowledgeSectionType.ESCALADE) {
            score -= 0.10;
        }
        return new ChunkMatch(chunk, Math.min(score, 0.95));
    }

    private String semanticHint(SemanticReasoningResponse reasoning) {
        if (reasoning == null || !reasoning.hasEscalationHint()) {
            return "";
        }
        return "\n\nNote de raisonnement semantique: la base de connaissances relie cet incident au niveau "
                + reasoning.niveauEscalade()
                + ". Le workflow reste sequentiel: toute escalade N0 passe d'abord par N1.";
    }

    private TicketCategory detectCategory(String message) {
        if (containsAny(message, "vpn", "wifi", "internet", "reseau", "connexion")) {
            return TicketCategory.RESEAU;
        }
        if (containsAny(message, "mot de passe", "compte", "login", "acces", "authentification")) {
            return TicketCategory.COMPTE_ACCES;
        }
        if (containsAny(message, "mail", "email", "outlook", "smtp", "boite")) {
            return TicketCategory.EMAIL;
        }
        if (containsAny(message, "imprimante", "impression", "scanner")) {
            return TicketCategory.IMPRIMANTE;
        }
        if (containsAny(message, "virus", "phishing", "securite", "malware")) {
            return TicketCategory.SECURITE;
        }
        if (containsAny(message, "ecran", "clavier", "souris", "pc", "poste", "disque")) {
            return TicketCategory.MATERIEL;
        }
        if (containsAny(message, "logiciel", "application", "installation", "mise a jour")) {
            return TicketCategory.LOGICIEL;
        }
        return null;
    }

    private String conversationSummary(ChatbotSession session) {
        StringBuilder builder = new StringBuilder("Conversation N0:\n");
        messageRepository.findBySessionIdOrderByDateCreationAsc(session.getId())
                .forEach(message -> builder
                        .append("- ")
                        .append(message.getAuteur())
                        .append(": ")
                        .append(message.getContenu())
                        .append("\n"));
        return builder.substring(0, Math.min(builder.length(), 4000));
    }

    private boolean containsAny(String value, String... fragments) {
        for (String fragment : fragments) {
            if (value.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private record ChunkMatch(KnowledgeChunk chunk, double score) {
    }
}
