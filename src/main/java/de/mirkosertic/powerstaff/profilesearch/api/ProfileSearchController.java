package de.mirkosertic.powerstaff.profilesearch.api;

import de.mirkosertic.powerstaff.profilesearch.command.LlmService;
import de.mirkosertic.powerstaff.profilesearch.command.ProfileSearchCommandService;
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

    public ProfileSearchController(ProfileSearchCommandService commandService,
                                   ProfileSearchQueryService queryService,
                                   LlmService llmService,
                                   RememberedProjectService rememberedProjectService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.llmService = llmService;
        this.rememberedProjectService = rememberedProjectService;
    }

    @GetMapping
    public void index(Principal principal, HttpServletResponse response) throws IOException {
        String userId = principal.getName();
        var latestChatId = queryService.findLatestChatByUser(userId);
        if (latestChatId.isPresent()) {
            response.sendRedirect("/profilesearch/chat/" + latestChatId.get());
        } else {
            Long projectId = rememberedProjectService.get(userId).orElse(null);
            Long chatId = commandService.createChat(userId, projectId);
            response.sendRedirect("/profilesearch/chat/" + chatId);
        }
    }

    @GetMapping("/chat/{chatId}")
    public String chat(@PathVariable Long chatId,
                       @RequestParam(defaultValue = "0") int offset,
                       Principal principal,
                       Model model,
                       HttpServletResponse response) {
        String userId = principal.getName();
        List<ChatListView> sidebar = queryService.findChatsByUser(userId, offset, PAGE_SIZE);
        long totalChats = queryService.countChatsByUser(userId);
        List<MessageView> messages = queryService.findMessagesByChat(chatId);

        // Set X-Next-Url header for infinite scroll if more data available
        int nextOffset = offset + PAGE_SIZE;
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
    public String newChat(Principal principal) {
        String userId = principal.getName();
        Long projectId = rememberedProjectService.get(userId).orElse(null);
        Long chatId = commandService.createChat(userId, projectId);
        return "redirect:/profilesearch/chat/" + chatId;
    }

    @DeleteMapping("/chat/{chatId}")
    @ResponseBody
    public Map<String, String> deleteChat(@PathVariable Long chatId, Principal principal) {
        String userId = principal.getName();
        commandService.deleteChat(chatId);

        // Navigate to most recently modified remaining chat, or create a new one
        var nextChatId = queryService.findLatestChatByUser(userId);
        String redirectTo;
        if (nextChatId.isPresent()) {
            redirectTo = "/profilesearch/chat/" + nextChatId.get();
        } else {
            Long projectId = rememberedProjectService.get(userId).orElse(null);
            Long newId = commandService.createChat(userId, projectId);
            redirectTo = "/profilesearch/chat/" + newId;
        }
        return Map.of("redirectTo", redirectTo);
    }

    record SendRequest(String message) {}

    record SendResponse(Long id, String role, String content) {}

    @PostMapping(value = "/chat/{chatId}/send", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SendResponse sendMessage(@PathVariable Long chatId, @RequestBody SendRequest request,
                                    Principal principal,
                                    HttpSession session) {
        String userId = principal.getName();

        // Build LLM context and load full message history for LLM
        var context = queryService.buildLlmContext(userId);

        // Call LLM
        LlmService.Reply assistantReply = llmService.sendMessage(principal, session.getId(), Long.toString(chatId), context, request.message());

        return new SendResponse(assistantReply.id(), assistantReply.role(), assistantReply.message());
    }

    private RememberedProjectInfo buildRememberedProjectInfo(Principal principal) {
        if (principal == null) return null;
        return rememberedProjectService.getRememberedProjectInfo(principal.getName()).orElse(null);
    }

}
