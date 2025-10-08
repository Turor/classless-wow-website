package turoran.classless.webserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/components")
public class ComponentController {

    @GetMapping("/registration")
    public String getRegistrationComponent() {
        return "forward:/components/registration.html";
    }

    @GetMapping("/account-recovery")
    public String getAccountRecoveryComponent() {
        return "forward:/components/accountRecovery.html";
    }

    @GetMapping("/main")
    public String getMainComponent() {
        return "forward:/components/main.html";
    }
}