package turoran.classless.webserver.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import java.net.URI;
import java.util.Set;

@Configuration
@Slf4j
public class GarageS3Config {

    private final String endpoint;

    public GarageS3Config(
            @Value("${garage.s3.endpoint.remote:http://localhost:3900}") String remoteEndpoint
    ) {
        this.endpoint = remoteEndpoint;
    }

    @Getter
    @Setter
    public static class GarageS3AllowListProperties {
        public Set<String> buckets;
        public Set<String> keys;

        @PostConstruct
        public void logProperties() {
            log.info("AllowList Properties: {}", this);
        }
        @Override
        public String toString() {
            return "GarageS3AllowListProperties{" +
                    "buckets=" + buckets +
                    ", keys=" + keys +
                    '}';
        }
    }

    @Bean
    @ConfigurationProperties(prefix = "garage.s3.allowlist")
    public GarageS3AllowListProperties allowListProperties() {
        return new GarageS3AllowListProperties();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("garage"))
                .credentialsProvider(ProfileCredentialsProvider.create("garage"))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // Garage prefers path-style URLs
                        .build())
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("garage"))
                .credentialsProvider(ProfileCredentialsProvider.create("garage"))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}

