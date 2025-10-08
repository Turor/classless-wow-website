package turoran.classless.webserver.interceptors;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
@Slf4j
public class ResponseLoggingFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(response instanceof HttpServletResponse) || !(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper((HttpServletResponse) response);

        chain.doFilter(request, wrappedResponse); // proceed with controller


        byte[] content = wrappedResponse.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, response.getCharacterEncoding());
            log.info("Response for {} → {}", ((HttpServletRequest) request).getRequestURI(), body);
        }



        // IMPORTANT: copy content back to real response
        wrappedResponse.copyBodyToResponse();
    }
}
