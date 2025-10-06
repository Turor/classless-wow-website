package turoran.classless.webserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import turoran.classless.webserver.service.ClientService;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final ClientService userService;

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String email) {

        try {
            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                return "<span style='color: red; font-weight: bold;'>❌ Passwords do not match!</span>";
            }

            // Your registration logic here
            Optional<String> result = userService.registerAccount(username, password, email);
            return result.map(s -> "<span style='color: red; font-weight: bold;'>❌ Registration failed: " + s + "</span>")
                    .orElse("<span style='color: green; font-weight: bold;'>✅ Registration successful!</span>");
        } catch (Exception e) {
            // Error response
            return "<span style='color: red; font-weight: bold;'>❌ Registration failed: " + e.getMessage() + "</span>";
        }
    }
}
