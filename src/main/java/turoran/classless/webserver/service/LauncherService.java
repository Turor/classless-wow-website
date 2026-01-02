package turoran.classless.webserver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    public final Path zipPath;

    private String cachedEtag = null;

    public LauncherService(S3Client s3Client,
                           @Value("${launcher.cacheDir:./cache}") String cacheDir,
                           @Value("${launcherservice.launcherName:ClasslessLauncher.zip}") String launcherName) {
        this.s3Client = s3Client;
        zipPath = Paths.get(cacheDir, "/",launcherName);
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

    public synchronized void synchronizeClient() throws IOException {
        HeadObjectResponse head = s3Client.headObject(b -> b
                .bucket("wow")
                .key("ClasslessLauncher.zip")
        );
        System.out.println(head);
        System.out.println(head.eTag() + " " + cachedEtag);

        String etag = head.eTag();
        boolean needsDownload = !Files.exists(zipPath) || !etag.equals(cachedEtag);

        if (needsDownload) {
            System.out.println("🔄 Updating launcher.zip from S3...");
            Files.deleteIfExists(zipPath);
            s3Client.getObject(b -> b.bucket("wow").key("ClasslessLauncher.zip"), zipPath);
            cachedEtag = etag;
            System.out.println("✅ launcher.zip updated and cached locally.");
        } else {
            System.out.println("✔️ launcher.zip is up to date.");
        }
    }

}
