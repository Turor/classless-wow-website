package turoran.classless.webserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.HeadObjectPresignRequest;
import turoran.classless.webserver.config.GarageS3Config;

import java.net.URL;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class PresignedUrlService {

    private final S3Presigner presigner;
    private final GarageS3Config.GarageS3AllowListProperties allowListProperties;

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
        if (allowListProperties.buckets.contains(bucket) && allowListProperties.keys.contains(key)) {
            return true;
        }
        log.info("Bucket {} or key {} not allowed", bucket, key);
        return false;
    }
}
