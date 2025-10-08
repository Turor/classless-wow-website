package turoran.classless.webserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    @PostMapping("/login")
    public String handleLogin(@RequestParam String password,
                                             HttpServletRequest request) {
        log.info("Password:{}", password);
        if ("letmein".equals(password)) {
            log.info("Login successful");
            request.getSession().setAttribute("authenticated", true);
            return "forward:/components/main.html";
        }
        return "forward:index.html";
    }
}
