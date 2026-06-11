package com.pfe.itsm.n0.service;

import com.pfe.itsm.auth.security.CurrentUserService;
import com.pfe.itsm.common.BusinessException;
import com.pfe.itsm.common.ResourceNotFoundException;
import com.pfe.itsm.n0.domain.ChatbotMessage;
import com.pfe.itsm.n0.domain.ChatbotMessageAuthor;
import com.pfe.itsm.n0.domain.ChatbotSession;
import com.pfe.itsm.n0.domain.ChatbotSessionStatus;
import com.pfe.itsm.n0.domain.KnowledgeChunk;
import com.pfe.itsm.n0.domain.KnowledgeSectionType;
import com.pfe.itsm.n0.dto.ChatbotAnswerResponse;
import com.pfe.itsm.n0.dto.ChatbotMessageResponse;
import com.pfe.itsm.n0.dto.ChatbotSessionResponse;
import com.pfe.itsm.n0.dto.SendChatbotMessageRequest;
import com.pfe.itsm.n0.dto.StartChatbotSessionRequest;
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
import java.util.List;
import java.util.Locale;
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

    public ChatbotService(
            ChatbotSessionRepository sessionRepository,
            ChatbotMessageRepository messageRepository,
            KnowledgeChunkRepository chunkRepository,
            CurrentUserService currentUserService,
            TicketService ticketService
    ) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.chunkRepository = chunkRepository;
        this.currentUserService = currentUserService;
        this.ticketService = ticketService;
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
        String normalized = normalize(userMessage);
        saveMessage(session, ChatbotMessageAuthor.UTILISATEUR, userMessage.trim(), null, null);

        TicketCategory category = detectCategory(normalized);
        if (category != null) {
            session.updateCategory(category);
        }

        ChunkMatch match = chunkRepository.findByActifTrueAndArticleActifTrue()
                .stream()
                .map(chunk -> score(chunk, normalized, session.getCategorieDetectee()))
                .max(Comparator.comparingDouble(ChunkMatch::score))
                .orElse(null);

        String content;
        String sources = null;
        double confidence = match == null ? 0 : match.score();
        boolean escalationRecommended = confidence < ANSWER_THRESHOLD;

        if (match != null && confidence >= ANSWER_THRESHOLD) {
            content = "Voici une procedure documentee a essayer:\n\n"
                    + match.chunk().getContenu()
                    + "\n\nLe probleme est-il resolu ?";
            sources = match.chunk().getArticle().getTitre() + " v" + match.chunk().getArticle().getVersion();
        } else if (session.getCategorieDetectee() == null) {
            content = "Je n'ai pas encore assez d'elements. Precisez l'application concernee, le message d'erreur ou le materiel impacte.";
        } else {
            content = "Je n'ai pas trouve de procedure suffisamment fiable. Je recommande une escalade vers N1.";
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
                sources == null ? List.of() : List.of(sources),
                null
        );
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
