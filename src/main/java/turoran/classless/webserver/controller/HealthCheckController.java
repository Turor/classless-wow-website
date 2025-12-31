package turoran.classless.webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<Object> index() {
        log.info("Health check");
        return ResponseEntity.ok().build();
    }
}
