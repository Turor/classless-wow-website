package turoran.classless.webserver.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@Slf4j
public class ResponseLoggingInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) {

        int status = response.getStatus();
        String uri = request.getRequestURI();
        String view = (modelAndView != null) ? modelAndView.getViewName() : "N/A";

        log.info("POST-HANDLE → [{}] {} → View: {}", uri, status, view);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("PRE-HANDLE → [{}]", request.getRequestURI());
        return true;
    }
}

