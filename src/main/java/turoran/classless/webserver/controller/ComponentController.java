package turoran.classless.webserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/components")
public class ComponentController {

    @GetMapping("/registration")
    public String getRegistrationComponent() {
        return "components/registration";
    }

    @GetMapping("/account-recovery")
    public String getAccountRecoveryComponent() {
        return "components/accountRecovery";
    }
}