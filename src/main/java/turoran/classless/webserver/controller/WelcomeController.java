package turoran.classless.webserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

    @GetMapping("/home")
    public String index(Model model) {
        return "index"; // "index.html" template in the "templates" folder
    }
}
