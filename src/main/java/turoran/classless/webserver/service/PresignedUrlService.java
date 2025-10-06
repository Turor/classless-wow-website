package turoran.classless.webserver.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import java.net.URL;
import java.time.Duration;

@Service
public class PresignedUrlService {

    private final S3Presigner presigner;

    public PresignedUrlService(S3Presigner presigner) {
        this.presigner = presigner;
    }

    public URL generatePresignedUrl(String bucketName, String key, Duration expiresIn) {
        // 1. Build a GetObject request (this defines what object to download)
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
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
}
