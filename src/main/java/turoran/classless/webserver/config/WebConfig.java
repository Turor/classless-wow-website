package turoran.classless.webserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import turoran.classless.webserver.interceptors.PasswordGateInterceptor;
import turoran.classless.webserver.interceptors.ResponseLoggingInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final PasswordGateInterceptor gate;
    private final ResponseLoggingInterceptor logging;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gate);
        registry.addInterceptor(logging);
    }
}
