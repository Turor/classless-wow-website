package turoran.classless.webserver.service;

public interface S3Service {
    String generatePresignedUrl(String key);
}
