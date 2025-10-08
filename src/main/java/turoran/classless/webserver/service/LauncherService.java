package turoran.classless.webserver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class LauncherService {
    private final S3Client s3Client;
    public final Path localZipPath = Paths.get("src/main/resources/static/cache/ClasslessLauncher.zip");

    private String cachedEtag = null;

    public LauncherService(S3Client s3Client) {
        this.s3Client = s3Client;
        try {
            log.info("Creating cache directory at {}", localZipPath.getParent());
            Files.createDirectories(localZipPath.getParent());
        } catch (IOException e) {
            log.error("Failed to create cache directory", e);
            throw new RuntimeException("Failed to create cache directory", e);
        }
    }

    @PostConstruct
    public void preloadLauncherZip() {
        try {
            System.out.println("🟡 Checking for latest launcher.zip from S3...");
            synchronizeClient();
        } catch (Exception e) {
            System.err.println("⚠️ Failed to preload launcher.zip: " + e.getMessage());
        }
    }

    public synchronized void synchronizeClient() {
        HeadObjectResponse head = s3Client.headObject(b -> b
                .bucket("wow")
                .key("ClasslessLauncher.zip")
        );

        String etag = head.eTag();
        boolean needsDownload = !Files.exists(localZipPath) || !etag.equals(cachedEtag);

        if (needsDownload) {
            System.out.println("🔄 Updating launcher.zip from S3...");
            s3Client.getObject(b -> b.bucket("wow").key("ClasslessLauncher.zip"), localZipPath);
            cachedEtag = etag;
            System.out.println("✅ launcher.zip updated and cached locally.");
        } else {
            System.out.println("✔️ launcher.zip is up to date.");
        }
    }

}
