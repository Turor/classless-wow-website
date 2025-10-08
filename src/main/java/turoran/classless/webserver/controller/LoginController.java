package turoran.classless.webserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class LoginController {
    public final String password;

    public LoginController(@Value("${password:letmein}")String password) {
        this.password = password;
    }

    @PostMapping("/login")
    public ResponseEntity<String> handleLogin(@RequestParam String password,
                                            HttpServletRequest request) {
        log.info("Password:{}", password);
        if (this.password.equals(password)) {
            log.info("Login successful");
            request.getSession().setAttribute("authenticated", true);
            return ResponseEntity.status(HttpStatus.OK)
                    .header("HX-Retarget", "#content-area")
                    .header("HX-Reswap", "outerHTML")
                    .header("HX-Location", "/components/main.html")
                    .build();
        }
        return ResponseEntity.status(HttpStatus.OK)
                .header("HX-Retarget", "#feedback")
                .header("HX-Reswap", "outerHTML")
                .body("<span id=\"feedback\">Incorrect Password</span>");
    }
}
