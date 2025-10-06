package turoran.classless.webserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/login/feedback")
    @ResponseBody
    public String loginFeedback(@RequestParam(required = false) String error,
                               @RequestParam(required = false) String logout) {
        if (error != null) {
            return "<div class='error-message'>❌ Invalid username and password.</div>";
        }
        if (logout != null) {
            return "<div class='info-message'>✓ You have been logged out.</div>";
        }
        return "";
    }

    @PostMapping("/login")
    public ResponseEntity<String> handleLogin(@RequestParam String password,
                                             HttpServletRequest request) {
        try {
            // Create authentication token
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken("general", password);
            
            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // Set authentication in security context
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
            
            // Store security context in session
            request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                securityContext
            );
            
            return ResponseEntity.ok()
                    .header("HX-Redirect", "/home")
                    .body("<div class='success-message'>✓ Login successful!</div>");
            
        } catch (AuthenticationException e) {
            return ResponseEntity.ok()
                    .body("<div class='error-message'>❌ Invalid username or password.</div>");
        }
    }
}
