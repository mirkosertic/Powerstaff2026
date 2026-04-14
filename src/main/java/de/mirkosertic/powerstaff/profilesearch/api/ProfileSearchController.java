package de.mirkosertic.powerstaff.profilesearch.api;

import de.mirkosertic.powerstaff.profilesearch.command.LlmService;
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchCommandService;
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchProperties;
import de.mirkosertic.powerstaff.profilesearch.query.ChatListView;
import de.mirkosertic.powerstaff.profilesearch.query.McpSearchException;
import de.mirkosertic.powerstaff.profilesearch.query.MessageView;
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchCriteria;
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchPage;
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import de.mirkosertic.powerstaff.project.command.RememberedProjectInfo;
import de.mirkosertic.powerstaff.project.command.RememberedProjectService;
import de.mirkosertic.powerstaff.shared.query.TagQueryService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Controller
@RequestMapping("/profilesearch")
public class ProfileSearchController {

    private static final int PAGE_SIZE = 20;

    private final ProfileSearchCommandService commandService;
    private final ProfileSearchQueryService queryService;
    private final LlmService llmService;
    private final RememberedProjectService rememberedProjectService;
    private final ProfileSearchProperties profileSearchProperties;
    private final TagQueryService tagQueryService;
    private final ObjectMapper objectMapper;

    public ProfileSearchController(final ProfileSearchCommandService commandService,
                                   final ProfileSearchQueryService queryService,
                                   final LlmService llmService,
                                   final RememberedProjectService rememberedProjectService,
                                   final ProfileSearchProperties profileSearchProperties,
                                   final TagQueryService tagQueryService,
                                   final ObjectMapper objectMapper) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.llmService = llmService;
        this.rememberedProjectService = rememberedProjectService;
        this.profileSearchProperties = profileSearchProperties;
        this.tagQueryService = tagQueryService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String index() {
        return "redirect:/profilesearch/chat";
    }

    @GetMapping("/chat")
    public void chatIndex(final Principal principal, final HttpServletResponse response) throws IOException {
        final String userId = principal.getName();
        final var latestChatId = queryService.findLatestChatByUser(userId);
        if (latestChatId.isPresent()) {
            response.sendRedirect("/profilesearch/chat/" + latestChatId.get());
        } else {
            final Long projectId = rememberedProjectService.get(userId).orElse(null);
            final Long chatId = commandService.createChat(userId, projectId);
            response.sendRedirect("/profilesearch/chat/" + chatId);
        }
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) final String searchTerm,
                         @RequestParam(required = false) final Long salaryPerDayFrom,
                         @RequestParam(required = false) final Long salaryPerDayTo,
                         @RequestParam(required = false) final String tagIds,
                         @RequestParam(required = false) final String sortField,
                         @RequestParam(required = false) final String sortDir,
                         @RequestParam(required = false) final Boolean semanticSearch,
                         @RequestParam(defaultValue = "0.8") final float similarityThreshold,
                         @RequestParam(defaultValue = "0") final int offset,
                         final Principal principal,
                         final Model model,
                         final HttpServletResponse response) {
        final ProfileSearchCriteria criteria = new ProfileSearchCriteria(searchTerm, salaryPerDayFrom, salaryPerDayTo, tagIds, sortField, sortDir, semanticSearch, similarityThreshold);
        final boolean empty = (criteria.searchTerm() == null || criteria.searchTerm().isBlank())
                && criteria.salaryPerDayFrom() == null
                && criteria.salaryPerDayTo() == null
                && (criteria.tagIds() == null || criteria.tagIds().isBlank());

        if (empty) {
            model.addAttribute("validationError", "Bitte mindestens ein Suchkriterium angeben.");
            model.addAttribute("results", List.of());
            model.addAttribute("totalCount", 0L);
            model.addAttribute("criteria", criteria);
            model.addAttribute("sortField", criteria.sortField());
            model.addAttribute("sortDir", criteria.sortDir());
            model.addAttribute("nextUrl", null);
            model.addAttribute("allTags", tagQueryService.findAll());
            model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));
            return "profilesearch/search-page";
        }

        final String returnTo = buildSearchReturnUrl(criteria);

        if (offset > 0) {
            final ProfileSearchPage page = queryService.searchFreelancers(criteria, offset, PAGE_SIZE);
            final var results = page.results();
            final long total = page.totalHits();
            final int nextOffset = offset + PAGE_SIZE;
            if (nextOffset < total) {
                response.setHeader("X-Next-Url", buildSearchMoreUrl(criteria, nextOffset));
            }
            model.addAttribute("results", results);
            model.addAttribute("returnTo", returnTo);
            return "profilesearch/search-results :: results";
        }

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        final ProfileSearchPage page = queryService.searchFreelancers(criteria, 0, PAGE_SIZE);
        final var results = page.results();
        final long total = page.totalHits();
        final String nextUrl = PAGE_SIZE < total ? buildSearchMoreUrl(criteria, PAGE_SIZE) : null;

        model.addAttribute("results", results);
        model.addAttribute("totalCount", total);
        model.addAttribute("criteria", criteria);
        model.addAttribute("sortField", criteria.sortField());
        model.addAttribute("sortDir", criteria.sortDir());
        model.addAttribute("nextUrl", nextUrl);
        model.addAttribute("allTags", tagQueryService.findAll());
        model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));
        model.addAttribute("returnTo", returnTo);
        return "profilesearch/search-page";
    }

    private String buildSearchReturnUrl(final ProfileSearchCriteria c) {
        final var b = UriComponentsBuilder.fromPath("/profilesearch/search");
        if (c.searchTerm() != null && !c.searchTerm().isBlank()) b.queryParam("searchTerm", c.searchTerm());
        if (c.salaryPerDayFrom() != null) b.queryParam("salaryPerDayFrom", c.salaryPerDayFrom());
        if (c.salaryPerDayTo() != null) b.queryParam("salaryPerDayTo", c.salaryPerDayTo());
        if (c.tagIds() != null && !c.tagIds().isBlank()) b.queryParam("tagIds", c.tagIds());
        if (c.sortField() != null) b.queryParam("sortField", c.sortField());
        if (c.sortDir() != null) b.queryParam("sortDir", c.sortDir());
        return b.encode().build().toUriString();
    }

    private String buildSearchMoreUrl(final ProfileSearchCriteria c, final int offset) {
        final var b = UriComponentsBuilder.fromPath("/profilesearch/search").queryParam("offset", offset);
        if (c.searchTerm() != null && !c.searchTerm().isBlank()) {
            b.queryParam("searchTerm", c.searchTerm());
        }
        if (c.salaryPerDayFrom() != null) {
            b.queryParam("salaryPerDayFrom", c.salaryPerDayFrom());
        }
        if (c.salaryPerDayTo() != null) {
            b.queryParam("salaryPerDayTo", c.salaryPerDayTo());
        }
        if (c.tagIds() != null && !c.tagIds().isBlank()) {
            b.queryParam("tagIds", c.tagIds());
        }
        if (c.sortField() != null) {
            b.queryParam("sortField", c.sortField());
        }
        if (c.sortDir() != null) {
            b.queryParam("sortDir", c.sortDir());
        }
        if (Boolean.TRUE.equals(c.semanticSearch())) {
            b.queryParam("semanticSearch", "true");
            b.queryParam("similarityThreshold", c.effectiveSimilarityThreshold());
        }
        return b.encode().build().toUriString();
    }

    @GetMapping("/chat/{chatId}")
    public String chat(@PathVariable final Long chatId,
                       @RequestParam(defaultValue = "0") final int offset,
                       final Principal principal,
                       final Model model,
                       final HttpServletResponse response) {
        final String userId = principal.getName();
        final List<ChatListView> sidebar = queryService.findChatsByUser(userId, offset, PAGE_SIZE);
        final long totalChats = queryService.countChatsByUser(userId);
        final List<MessageView> messages = queryService.findMessagesByChat(chatId);

        // Set X-Next-Url header for infinite scroll if more data available
        final int nextOffset = offset + PAGE_SIZE;
        if (nextOffset < totalChats) {
            response.setHeader("X-Next-Url", "/profilesearch/chat/" + chatId + "?offset=" + nextOffset);
        }

        model.addAttribute("chatId", chatId);
        model.addAttribute("messages", messages);
        model.addAttribute("sidebar", sidebar);
        model.addAttribute("totalChats", totalChats);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));

        // Return fragment for infinite scroll requests, full page otherwise
        return offset == 0 ? "profilesearch/form" : "profilesearch/sidebar-entry";
    }

    @PostMapping("/chat/new")
    public String newChat(final Principal principal) {
        final String userId = principal.getName();
        final Long projectId = rememberedProjectService.get(userId).orElse(null);
        final Long chatId = commandService.createChat(userId, projectId);
        return "redirect:/profilesearch/chat/" + chatId;
    }

    @DeleteMapping("/chat/{chatId}")
    @ResponseBody
    public Map<String, String> deleteChat(@PathVariable final Long chatId, final Principal principal) {
        final String userId = principal.getName();
        commandService.deleteChat(chatId);

        // Navigate to most recently modified remaining chat, or create a new one
        final var nextChatId = queryService.findLatestChatByUser(userId);
        final String redirectTo;
        if (nextChatId.isPresent()) {
            redirectTo = "/profilesearch/chat/" + nextChatId.get();
        } else {
            final Long projectId = rememberedProjectService.get(userId).orElse(null);
            final Long newId = commandService.createChat(userId, projectId);
            redirectTo = "/profilesearch/chat/" + newId;
        }
        return Map.of("redirectTo", redirectTo);
    }

    record SendRequest(String message) {}

    record SendResponse(Long id, String role, String content, String jsonPayload) {}

    record SendResponseWrapper(List<SendResponse> messages, int promptTokens, int completionTokens, int maxContextTokens) {}

    @PostMapping(value = "/chat/{chatId}/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter streamMessage(@PathVariable final Long chatId,
                                    @RequestBody final SendRequest request,
                                    final Principal principal,
                                    final HttpSession session) {
        final SseEmitter emitter = new SseEmitter(180_000L);
        final AtomicReference<Thread> threadRef = new AtomicReference<>();

        final Runnable cancelStream = () -> {
            final Thread t = threadRef.get();
            if (t != null) {
                t.interrupt();
            }
        };
        emitter.onCompletion(cancelStream);
        emitter.onTimeout(cancelStream);
        emitter.onError(e -> cancelStream.run());

        final Thread thread = Thread.ofVirtual().unstarted(() -> {
            try {
                final var context = queryService.buildLlmContext(principal.getName());
                final int maxCtx = profileSearchProperties.getMaxContextTokens();

                llmService.sendMessageStreaming(principal, session.getId(),
                        chatId.toString(), context, request.message(),
                        event -> {
                            try {
                                // Enrich MessageComplete with maxContextTokens (known only in controller)
                                final LlmService.ChatStreamEvent enriched = switch (event) {
                                    case final LlmService.ChatStreamEvent.MessageComplete m ->
                                            new LlmService.ChatStreamEvent.MessageComplete(
                                                    m.id(), m.promptTokens(), m.completionTokens(), maxCtx);
                                    default -> event;
                                };
                                final String eventName = switch (enriched) {
                                    case final LlmService.ChatStreamEvent.ThinkingToken t   -> "thinking_token";
                                    case final LlmService.ChatStreamEvent.ContentToken c    -> "content_token";
                                    case final LlmService.ChatStreamEvent.ToolCall tc       -> "tool_call";
                                    case final LlmService.ChatStreamEvent.ToolResult tr     -> "tool_result";
                                    case final LlmService.ChatStreamEvent.MessageComplete m -> "message_complete";
                                    case final LlmService.ChatStreamEvent.StreamError e     -> "error";
                                };
                                emitter.send(SseEmitter.event()
                                        .name(eventName)
                                        .data(objectMapper.writeValueAsString(enriched)));
                            } catch (final IOException ex) {
                                throw new UncheckedIOException(ex);
                            }
                        });

                emitter.send(SseEmitter.event().name("done").data("{}"));
                emitter.complete();
            } catch (final UncheckedIOException ignored) {
                // Client hat Verbindung getrennt (IOException beim emitter.send())
            } catch (final RuntimeException ex) {
                // blockLast() wirft RuntimeException(InterruptedException) bei Thread-Interrupt
                if (ex.getCause() instanceof InterruptedException || Thread.currentThread().isInterrupted()) {
                    return;
                }
                try {
                    emitter.send(SseEmitter.event().name("error")
                            .data("{\"message\":\"" + ex.getMessage() + "\"}"));
                } catch (final IOException ignored) {
                    // ignore
                }
                emitter.completeWithError(ex);
            } catch (final IOException ex) {
                emitter.completeWithError(ex);
            }
        });
        threadRef.set(thread);
        thread.start();
        return emitter;
    }

    @PostMapping(value = "/chat/{chatId}/send", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SendResponseWrapper sendMessage(@PathVariable final Long chatId, @RequestBody final SendRequest request,
                                           final Principal principal,
                                           final HttpSession session) {
        final String userId = principal.getName();

        // Build LLM context and load full message history for LLM
        final var context = queryService.buildLlmContext(userId);

        // Call LLM
        final List<LlmService.Reply> replies = llmService.sendMessage(principal, session.getId(), Long.toString(chatId), context, request.message());

        final List<SendResponse> responses = replies.stream()
                .map(r -> new SendResponse(r.id(), r.role(), r.message(), r.jsonPayload()))
                .toList();

        int promptTokens = 0;
        int completionTokens = 0;
        for (final LlmService.Reply reply : replies) {
            if (reply.promptTokens() != null && reply.completionTokens() != null) {
                promptTokens = reply.promptTokens();
                completionTokens = reply.completionTokens();
            }
        }

        return new SendResponseWrapper(responses, promptTokens, completionTokens, profileSearchProperties.getMaxContextTokens());
    }

    private RememberedProjectInfo buildRememberedProjectInfo(final Principal principal) {
        if (principal == null) return null;
        return rememberedProjectService.getRememberedProjectInfo(principal.getName()).orElse(null);
    }

    /**
     * Exception-Handler für MCP-Suchfehler.
     * <p>
     * Zeigt dem Benutzer eine verständliche Fehlermeldung an, wenn die MCP-basierte
     * Profilsuche nach allen Retry-Versuchen fehlgeschlagen ist.
     */
    @ExceptionHandler(McpSearchException.class)
    public String handleMcpSearchException(final McpSearchException ex,
                                           @RequestParam(required = false) final String searchTerm,
                                           @RequestParam(required = false) final Long salaryPerDayFrom,
                                           @RequestParam(required = false) final Long salaryPerDayTo,
                                           @RequestParam(required = false) final String tagIds,
                                           @RequestParam(required = false) final String sortField,
                                           @RequestParam(required = false) final String sortDir,
                                           @RequestParam(required = false) final Boolean semanticSearch,
                                           @RequestParam(required = false) final Float similarityThreshold,
                                           final Principal principal,
                                           final Model model) {
        final ProfileSearchCriteria criteria = new ProfileSearchCriteria(
                searchTerm, salaryPerDayFrom, salaryPerDayTo, tagIds, sortField, sortDir, semanticSearch, similarityThreshold);

        model.addAttribute("error", ex.getMessage());
        model.addAttribute("results", List.of());
        model.addAttribute("totalCount", 0L);
        model.addAttribute("criteria", criteria);
        model.addAttribute("sortField", criteria.sortField());
        model.addAttribute("sortDir", criteria.sortDir());
        model.addAttribute("nextUrl", null);
        model.addAttribute("allTags", tagQueryService.findAll());
        model.addAttribute("rememberedProject", buildRememberedProjectInfo(principal));
        return "profilesearch/search-page";
    }

}
