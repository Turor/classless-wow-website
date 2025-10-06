package turoran.classless.webserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import turoran.classless.webserver.service.PresignedUrlService;

import java.net.URL;
import java.time.Duration;

@RestController
@RequestMapping("/files")
public class FileController {

    private final PresignedUrlService presignedUrlService;

    public FileController(PresignedUrlService presignedUrlService) {
        this.presignedUrlService = presignedUrlService;
    }

    @GetMapping("/presign")
    public ResponseEntity<String> getPresignedUrl(@RequestParam String bucket, @RequestParam String key) {
        URL url = presignedUrlService.generatePresignedUrl(bucket, key, Duration.ofMinutes(10));
        return ResponseEntity.ok(url.toString());
    }
}

