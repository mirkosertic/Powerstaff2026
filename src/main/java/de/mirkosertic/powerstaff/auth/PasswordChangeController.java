package de.mirkosertic.powerstaff.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/passwort-aendern")
public class PasswordChangeController {

    private final PsUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public PasswordChangeController(final PsUserRepository repository, final PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showForm(final Model model) {
        model.addAttribute("passwordChangeCommand", new PasswordChangeCommand());
        return "auth/password-change";
    }

    @PostMapping
    public String changePassword(@RequestParam("oldPassword") final String oldPassword,
                                 @RequestParam("newPassword") final String newPassword,
                                 @RequestParam("newPasswordConfirm") final String newPasswordConfirm,
                                 final Authentication authentication,
                                 final Model model) {

        if (!newPassword.equals(newPasswordConfirm)) {
            model.addAttribute("passwordChangeCommand", new PasswordChangeCommand());
            model.addAttribute("error", "Die neuen Passwörter stimmen nicht überein.");
            return "auth/password-change";
        }

        if (newPassword.length() < 8) {
            model.addAttribute("passwordChangeCommand", new PasswordChangeCommand());
            model.addAttribute("error", "Das neue Passwort muss mindestens 8 Zeichen lang sein.");
            return "auth/password-change";
        }

        final String username = authentication.getName();
        final PsUser psUser = repository.findById(username).orElse(null);

        if (psUser == null || !passwordEncoder.matches(oldPassword, psUser.getPasswordHash())) {
            model.addAttribute("passwordChangeCommand", new PasswordChangeCommand());
            model.addAttribute("error", "Das aktuelle Passwort ist falsch.");
            return "auth/password-change";
        }

        final String newHash = passwordEncoder.encode(newPassword);
        repository.updatePassword(username, newHash);

        return "redirect:/";
    }

    public static class PasswordChangeCommand {
        // Command-Objekt für das Thymeleaf-Formular
    }
}
