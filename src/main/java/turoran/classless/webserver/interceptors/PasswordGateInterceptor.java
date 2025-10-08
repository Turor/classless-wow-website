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
    private final Set<String> unblockedList = Set.of("/index.html", "/", "/login", "/error","/components/login.html", "/stylesheet.css");

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

        // Already logged in
        if (Boolean.TRUE.equals(request.getSession().getAttribute("authenticated"))) {
            log.info("Allowing request because the user is logged in");
            return true;
        }

        return false;
    }
}
