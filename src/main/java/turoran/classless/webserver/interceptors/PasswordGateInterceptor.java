package turoran.classless.webserver.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class PasswordGateInterceptor implements HandlerInterceptor {
    private static final String SECRET = "letmein";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Allow access to login page and static resources
        String uri = request.getRequestURI();
        log.info("Request Received for  URI: {} {}", uri, request.getSession().getAttribute("authenticated"));
        if (uri.equals("/index.html") || uri.equals("/") || uri.equals("/login") || uri.equals("/error")) {
            return true;
        }

        // Already logged in
        if (Boolean.TRUE.equals(request.getSession().getAttribute("authenticated"))) {
            return true;
        }

        return false;
    }
}
