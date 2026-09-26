package turoran.classless.webserver.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@Slf4j
public class PasswordGateInterceptor implements HandlerInterceptor {
    private final Set<String> unblockedList = Set.of("/index.html", "/", "/login", "/error","/components/login.html", "/stylesheet.css",
            "/files/presign/header", "/files/presign/download","/health");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Allow access to login page and static resources
        String uri = request.getRequestURI();
        log.info("Request Received for  URI: {} {}", uri, request.getSession().getAttribute("authenticated"));
        if (unblockedList.contains(uri)) {
            log.info("Allow access to login page and static resources");
            return true;
        }

        boolean authenticated = Boolean.TRUE.equals(request.getSession().getAttribute("authenticated"));
        boolean htmx = "true".equalsIgnoreCase(request.getHeader("HX-Request"));

        // HTMX partials are not full documents. A full-page navigation/refresh to a
        // /components/* URL must bounce back to the login shell at "/".
        if (!htmx && uri.startsWith("/components/")) {
            log.info("Redirecting full-page component request to login shell: {}", uri);
            response.sendRedirect("/");
            return false;
        }

        // Already logged in
        if (authenticated) {
            log.info("Allowing request because the user is logged in");
            return true;
        }

        // Unauthenticated: send the user to the password gate instead of a blank deny.
        log.info("Unauthenticated request to {}; redirecting to login", uri);
        if (htmx) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("HX-Redirect", "/");
        } else {
            response.sendRedirect("/");
        }
        return false;
    }
}
