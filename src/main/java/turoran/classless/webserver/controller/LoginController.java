package turoran.classless.webserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@Slf4j
public class LoginController {
    public final String password;

    public LoginController(@Value("${password:letmein}")String password) {
        this.password = password;
    }

    @PostMapping("/login")
    public ResponseEntity<String> handleLogin(@RequestParam String password,
                                            HttpServletRequest request) throws IOException {
        log.info("Password:{}", password);
        if (this.password.equals(password)) {
            log.info("Login successful");
            request.getSession().setAttribute("authenticated", true);
            // Keep the browser URL on "/" so refresh returns to the login shell.
            // HX-Location was changing the URL to /components/main.html (a fragment).
            String main = new ClassPathResource("static/components/main.html")
                    .getContentAsString(StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.OK)
                    .header("HX-Retarget", "#content-area")
                    .header("HX-Reswap", "outerHTML")
                    .contentType(MediaType.TEXT_HTML)
                    .body(main);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .header("HX-Retarget", "#feedback")
                .header("HX-Reswap", "outerHTML")
                .body("<span id=\"feedback\">Incorrect Password</span>");
    }
}
