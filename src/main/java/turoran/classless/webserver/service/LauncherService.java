package turoran.classless.webserver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LauncherService {
    public static final String PLATFORM_WINDOWS = "windows";
    public static final String PLATFORM_LINUX = "linux";
    /** Apple Silicon / macOS Classless launcher object (optional until published). */
    public static final String PLATFORM_MACOS = "macos";

    private final S3Client s3Client;
    private final Path cacheDir;
    private final Map<String, String> objectKeys;
    private final Map<String, String> cachedEtags = new ConcurrentHashMap<>();
    private final Set<String> missingRemoteKeys = ConcurrentHashMap.newKeySet();

    public LauncherService(S3Client s3Client,
                           @Value("${launcherservice.cacheDir:./cache}") String cacheDir,
                           @Value("${launcherservice.windowsLauncherName:ClasslessLauncher.zip}") String windowsLauncherName,
                           @Value("${launcherservice.linuxLauncherName:ClasslessLauncherLinux.zip}") String linuxLauncherName,
                           @Value("${launcherservice.macosLauncherName:ClasslessLauncherMacos.zip}") String macosLauncherName) {
        this.s3Client = s3Client;
        this.cacheDir = Paths.get(cacheDir);
        Map<String, String> keys = new LinkedHashMap<>();
        keys.put(PLATFORM_WINDOWS, windowsLauncherName);
        keys.put(PLATFORM_LINUX, linuxLauncherName);
        keys.put(PLATFORM_MACOS, macosLauncherName);
        this.objectKeys = Map.copyOf(keys);
        log.info("LauncherService initialized with cacheDir={} windows={} linux={} macos={}",
                this.cacheDir, windowsLauncherName, linuxLauncherName, macosLauncherName);
    }

    @PostConstruct
    public void preloadLauncherZip() {
        objectKeys.keySet().forEach(platform -> {
            try {
                log.info("Checking for latest {} launcher from S3...", platform);
                synchronizeClient(platform);
            } catch (Exception e) {
                log.warn("Failed to preload {} launcher: {}", platform, e.getMessage());
            }
        });
    }

    public Set<String> supportedPlatforms() {
        return objectKeys.keySet();
    }

    public boolean isKnownPlatform(String platform) {
        return objectKeys.containsKey(normalize(platform));
    }

    /** True when a local cached zip exists for the platform (after a successful sync). */
    public boolean isAvailable(String platform) {
        Path zipPath = zipPath(platform);
        return Files.isRegularFile(zipPath) && Files.isReadable(zipPath);
    }

    public Path zipPath(String platform) {
        String key = requireKey(platform);
        return cacheDir.resolve(key);
    }

    public String fileName(String platform) {
        return requireKey(platform);
    }

    public synchronized void synchronizeClient() throws IOException {
        synchronizeClient(PLATFORM_WINDOWS);
    }

    public synchronized void synchronizeClient(String platform) throws IOException {
        String key = requireKey(platform);
        String normalized = normalize(platform);
        Path zipPath = zipPath(platform);
        Files.createDirectories(cacheDir);

        try {
            HeadObjectResponse head = s3Client.headObject(b -> b.bucket("wow").key(key));
            String etag = head.eTag();
            boolean needsDownload = !Files.exists(zipPath) || !etag.equals(cachedEtags.get(normalized));

            if (needsDownload) {
                log.info("Updating {} from S3...", key);
                Files.deleteIfExists(zipPath);
                s3Client.getObject(b -> b.bucket("wow").key(key), zipPath);
                cachedEtags.put(normalized, etag);
                missingRemoteKeys.remove(normalized);
                log.info("{} updated and cached locally.", key);
            } else {
                log.info("{} is up to date.", key);
                missingRemoteKeys.remove(normalized);
            }
        } catch (NoSuchKeyException e) {
            missingRemoteKeys.add(normalized);
            log.warn("Launcher object not found in S3 for platform {} (key={}). Placeholder until published.",
                    platform, key);
            throw new IOException("Launcher not published for platform: " + platform, e);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                missingRemoteKeys.add(normalized);
                log.warn("Launcher object not found in S3 for platform {} (key={}, status=404). Placeholder until published.",
                        platform, key);
                throw new IOException("Launcher not published for platform: " + platform, e);
            }
            throw e;
        }
    }

    public boolean isMissingRemotely(String platform) {
        return missingRemoteKeys.contains(normalize(platform));
    }

    private String requireKey(String platform) {
        String normalized = normalize(platform);
        String key = objectKeys.get(normalized);
        if (key == null) {
            throw new IllegalArgumentException("Unknown launcher platform: " + platform);
        }
        return key;
    }

    private static String normalize(String platform) {
        return platform == null ? "" : platform.toLowerCase(Locale.ROOT).trim();
    }
}
