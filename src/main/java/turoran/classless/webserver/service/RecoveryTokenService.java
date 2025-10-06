package turoran.classless.webserver.service;

public interface RecoveryTokenService {
    String generateToken(String email);
    String validateToken(String token);
    String consumeToken(String token);

}
