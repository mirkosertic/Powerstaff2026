package de.mirkosertic.powerstaff.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/benutzer")
public class UserController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    public UserController(final UserQueryService userQueryService, final UserCommandService userCommandService) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
    }

    // -------------------------------------------------------------------------
    // Liste
    // -------------------------------------------------------------------------

    @GetMapping
    public String list(final Model model, @RequestParam(required = false) final String deleted) {
        model.addAttribute("users", userQueryService.findAll());
        model.addAttribute("activePage", "admin");
        if (deleted != null) {
            model.addAttribute("success", "Benutzer „" + deleted + " wurde erfolgreich gelöscht.");
        }
        return "admin/users";
    }

    // -------------------------------------------------------------------------
    // Neuanlage
    // -------------------------------------------------------------------------

    @PostMapping
    public String createUser(@RequestParam final String username,
                             @RequestParam final String password,
                             @RequestParam(defaultValue = "false") final boolean mustChangePassword,
                             @RequestParam(defaultValue = "false") final boolean enabled,
                             final RedirectAttributes redirectAttributes) {
        if (username == null || username.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Benutzername darf nicht leer sein.");
            return "redirect:/admin/benutzer";
        }
        if (password == null || password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Das Passwort muss mindestens 8 Zeichen lang sein.");
            return "redirect:/admin/benutzer";
        }
        if (userQueryService.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Ein Benutzer mit diesem Namen existiert bereits.");
            return "redirect:/admin/benutzer";
        }
        userCommandService.createUser(username.trim(), password, mustChangePassword, enabled);
        return "redirect:/admin/benutzer";
    }

    // -------------------------------------------------------------------------
    // Bearbeiten (Flags + optionales Passwort-Reset)
    // -------------------------------------------------------------------------

    @PostMapping("/{username}")
    public String updateUser(@PathVariable final String username,
                             @RequestParam(defaultValue = "false") final boolean mustChangePassword,
                             @RequestParam(defaultValue = "false") final boolean enabled,
                             @RequestParam(required = false) final String newPassword,
                             final Authentication authentication,
                             final RedirectAttributes redirectAttributes) {
        if (userCommandService.findByUsername(username).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Benutzer nicht gefunden.");
            return "redirect:/admin/benutzer";
        }
        userCommandService.updateUser(username, mustChangePassword, enabled);
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Das neue Passwort muss mindestens 8 Zeichen lang sein.");
                return "redirect:/admin/benutzer";
            }
            userCommandService.resetPassword(username, newPassword);
        }
        return "redirect:/admin/benutzer";
    }

    // -------------------------------------------------------------------------
    // System-Prompt bearbeiten
    // -------------------------------------------------------------------------

    @PostMapping("/{username}/systemprompt")
    public String updateSystemPrompt(@PathVariable final String username,
                                     @RequestParam final String profileSearchSystemPrompt,
                                     final RedirectAttributes redirectAttributes) {
        if (userCommandService.findByUsername(username).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Benutzer nicht gefunden.");
            return "redirect:/admin/benutzer";
        }
        userCommandService.updateSystemPrompt(username, profileSearchSystemPrompt);
        redirectAttributes.addFlashAttribute("success", "Systemprompt für \"" + username + "\" wurde gespeichert.");
        return "redirect:/admin/benutzer";
    }

    // -------------------------------------------------------------------------
    // Löschen
    // -------------------------------------------------------------------------

    @DeleteMapping("/{username}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable final String username,
                                                          final Authentication authentication) {
        if (authentication != null && authentication.getName().equals(username)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "error", "Sie können sich nicht selbst löschen."));
        }
        if (userCommandService.findByUsername(username).isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("ok", false, "error", "Benutzer nicht gefunden."));
        }
        userCommandService.deleteUser(username);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
