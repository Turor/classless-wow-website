package turoran.classless.webserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.HeadObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class PresignedUrlService {

    private final S3Presigner presigner;
    private final GarageS3AllowListProperties allowListProperties;

    public URL generatePresignedDownloadURL(String bucket, String key, Duration expiresIn) {
        if (!allowListProperties.buckets.contains(bucket) ||
            !allowListProperties.keys.contains(key)) {
            log.error("Bucket {} or key {} not allowed", bucket, key);
            return null;
        }
        // 1. Build a GetObject request (this defines what object to download)
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        // 2. Build a presign request
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiresIn) // e.g., Duration.ofMinutes(10)
                .getObjectRequest(getObjectRequest)
                .build();

        // 3. Generate presigned URL
        return presigner.presignGetObject(presignRequest).url();
    }

    public URL generatePresignedHeaderURL(String bucket, String key, Duration expiresIn) {
        if (!isAllowed(bucket, key)) {
            return null;
        }

        HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        HeadObjectPresignRequest presignRequest = HeadObjectPresignRequest.builder()
                .signatureDuration(expiresIn)
                .headObjectRequest(headRequest)
                .build();

        return presigner.presignHeadObject(presignRequest).url();
    }

    private boolean isAllowed(String bucket, String key) {
        if (allowListProperties.buckets.contains(bucket)) {
            return true;
        }
        for (String prefix : allowListProperties.prefixes ) {
            if (key.startsWith(prefix) || allowListProperties.keys.contains(key)) {
                return true;
            }
        }
        log.info("Bucket {} or key {} not allowed", bucket, key);
        return false;
    }

    @Component
    @ConfigurationProperties(prefix = "garage.s3.allowlist")
    private static class GarageS3AllowListProperties {
        private Set<String> buckets;
        private Set<String> keys;
        private Set<String> prefixes;
    }
}
