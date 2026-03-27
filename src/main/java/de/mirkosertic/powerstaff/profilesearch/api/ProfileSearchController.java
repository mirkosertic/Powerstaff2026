package de.mirkosertic.powerstaff.profilesearch.api;

import de.mirkosertic.powerstaff.profilesearch.command.LlmService;
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchCommandService;
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchProperties;
import de.mirkosertic.powerstaff.profilesearch.query.ChatListView;
import de.mirkosertic.powerstaff.profilesearch.query.MessageView;
import de.mirkosertic.powerstaff.profilesearch.query.ProfileSearchQueryService;
import de.mirkosertic.powerstaff.project.command.RememberedProjectInfo;
import de.mirkosertic.powerstaff.project.command.RememberedProjectService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/profilesearch")
public class ProfileSearchController {

    private static final int PAGE_SIZE = 20;

    private final ProfileSearchCommandService commandService;
    private final ProfileSearchQueryService queryService;
    private final LlmService llmService;
    private final RememberedProjectService rememberedProjectService;
    private final ProfileSearchProperties profileSearchProperties;

    public ProfileSearchController(final ProfileSearchCommandService commandService,
                                   final ProfileSearchQueryService queryService,
                                   final LlmService llmService,
                                   final RememberedProjectService rememberedProjectService,
                                   final ProfileSearchProperties profileSearchProperties) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.llmService = llmService;
        this.rememberedProjectService = rememberedProjectService;
        this.profileSearchProperties = profileSearchProperties;
    }

    @GetMapping
    public void index(final Principal principal, final HttpServletResponse response) throws IOException {
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

}
