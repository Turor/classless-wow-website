package turoran.classless.webserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import turoran.classless.webserver.repository.mysql.entities.RecoveryToken;
import turoran.classless.webserver.repository.mysql.RecoveryTokenRepository;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("prod")
public class SQLRecoveryTokenService implements RecoveryTokenService {

    private final RecoveryTokenRepository recoveryTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateToken(String email) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        log.info("Generated token:{}", token);

        RecoveryToken recoveryToken = new RecoveryToken();
        recoveryToken.setToken(token);
        recoveryToken.setEmail(email);

        Optional<Long> maxId = recoveryTokenRepository.findMaxId();
        maxId.ifPresentOrElse(id -> recoveryToken.setId(id + 1), () -> recoveryToken.setId(1L));


        recoveryTokenRepository.save(recoveryToken);

        return token;
    }

    @Override
    public String validateToken(String token) {
        return recoveryTokenRepository.findByToken(token)
                .map(RecoveryToken::getEmail)
                .orElse(null);
    }

    @Override
    public String consumeToken(String token) {
        return recoveryTokenRepository.findByToken(token)
                .map(rt -> {
                    String email = rt.getEmail();
                    recoveryTokenRepository.delete(rt);
                    return email;
                })
                .orElse(null);
    }
}
