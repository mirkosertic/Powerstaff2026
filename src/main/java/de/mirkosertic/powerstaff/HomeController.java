package de.mirkosertic.powerstaff;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/freelancer";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
