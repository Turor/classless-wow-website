package turoran.classless.webserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import turoran.classless.webserver.service.LauncherService;
import turoran.classless.webserver.service.PresignedUrlService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final LauncherService launcherService;
    private final PresignedUrlService presignedUrlService;

    @GetMapping("/presign/download")
    public ResponseEntity<String> getPresignedDownloadURL(@RequestParam String bucket, @RequestParam String key) {
        URL url = presignedUrlService.generatePresignedDownloadURL(bucket, key, Duration.ofMinutes(10));
        return ResponseEntity.ok(url.toString());
    }

    @GetMapping("/presign/header")
    public ResponseEntity<String> getPresignedHeaderURL(@RequestParam String bucket, @RequestParam String key) {
        URL url = presignedUrlService.generatePresignedHeaderURL(bucket, key, Duration.ofMinutes(10));
        return ResponseEntity.ok(url.toString());
    }

    @GetMapping(value="/downloadlauncher", produces = "application/zip")
    public ResponseEntity<StreamingResponseBody> serveLauncher() throws IOException {
        try {
            launcherService.synchronizeClient();
        } catch (Exception e) {
            log.error("Failed to synchronize ClasslessLauncher.zip: {}", e.getMessage());
        }

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream in = Files.newInputStream(launcherService.localZipPath)) {
                in.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"ClasslessLauncher.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(launcherService.localZipPath))
                .body(responseBody);
    }

    @GetMapping(value="/download")
    public ResponseEntity<Void> getLauncher() {
        return ResponseEntity.ok()
                .header("HX-Redirect", "/files/downloadlauncher")
                .build();
    }
}

