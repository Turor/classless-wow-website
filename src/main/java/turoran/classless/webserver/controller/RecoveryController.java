package turoran.classless.webserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import turoran.classless.webserver.service.ClientService;
import turoran.classless.webserver.service.RecoveryTokenService;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RecoveryController {
    private final RecoveryTokenService tokenService;
    private final ClientService clientService;

    @PostMapping("/recovery/token/generate")
    @ResponseBody
    public String generateToken(@RequestParam String email) {
        // Check if email exists
        if (!clientService.emailExists(email)) {
            log.info("Email doesn't exist for the given email:{}", email);
            // Don't reveal if email exists or not (security)
            return "<div class='success-message'>If that email exists, a recovery link has been sent.</div>";
        }
        log.info("Email exists for the given email:{}", email);

        // Generate token
        String token = tokenService.generateToken(email);
        return "<div class='success-message'>If that email exists, a recovery link has been sent.</div>";
    }

    @PostMapping("/recovery")
    @ResponseBody
    public String requestRecovery(@RequestParam String token, @RequestParam String newPassword, @RequestParam String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            return "<div class='error-message'>Passwords do not match.</div>";
        }

        String email = tokenService.consumeToken(token);
        if (email == null) {
            return "<div class='error-message'>Invalid token.</div>";
        }

        Optional<String> result = clientService.changePassword(email, newPassword);
        if (result.isPresent()) {
            return "<div class='error-message'>"+result.get()+"</div>";
        } else {
            log.info("Password changed for email:{}", email);
            return "<div class='success-message'>Password changed.</div>";
        }
    }
}
