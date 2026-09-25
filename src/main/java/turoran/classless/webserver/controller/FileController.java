package turoran.classless.webserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

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

    /** Availability map for UI (windows/linux/macos → cached locally after sync). */
    @GetMapping("/launchers")
    public ResponseEntity<Map<String, Object>> listLaunchers() {
        Map<String, Object> platforms = new LinkedHashMap<>();
        for (String platform : launcherService.supportedPlatforms()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("available", launcherService.isAvailable(platform));
            info.put("fileName", launcherService.fileName(platform));
            info.put("downloadPath", "/files/download/" + platform);
            platforms.put(platform, info);
        }
        return ResponseEntity.ok(Map.of("platforms", platforms));
    }

    @GetMapping(value = "/downloadlauncher", produces = "application/zip")
    public ResponseEntity<StreamingResponseBody> serveWindowsLauncher() throws IOException {
        return serveLauncher(LauncherService.PLATFORM_WINDOWS);
    }

    @GetMapping(value = "/downloadlauncher/{platform}", produces = "application/zip")
    public ResponseEntity<StreamingResponseBody> serveLauncher(@PathVariable String platform) throws IOException {
        if (!launcherService.isKnownPlatform(platform)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            launcherService.synchronizeClient(platform);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Failed to synchronize launcher for {}: {}", platform, e.getMessage());
        }

        if (!launcherService.isAvailable(platform)) {
            // macOS (and any unpublished platform): hooks exist, binary not published yet.
            return ResponseEntity.notFound().build();
        }

        Path zipPath = launcherService.zipPath(platform);
        String fileName = launcherService.fileName(platform);
        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream in = Files.newInputStream(zipPath)) {
                in.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(zipPath))
                .body(responseBody);
    }

    @GetMapping(value = "/download")
    public ResponseEntity<Void> getWindowsLauncher() {
        return redirectToLauncher(LauncherService.PLATFORM_WINDOWS);
    }

    @GetMapping(value = "/download/{platform}")
    public ResponseEntity<Void> getLauncher(@PathVariable String platform) {
        if (!launcherService.isKnownPlatform(platform)) {
            return ResponseEntity.badRequest().build();
        }
        return redirectToLauncher(platform);
    }

    private ResponseEntity<Void> redirectToLauncher(String platform) {
        return ResponseEntity.ok()
                .header("HX-Redirect", "/files/downloadlauncher/" + platform)
                .build();
    }
}
