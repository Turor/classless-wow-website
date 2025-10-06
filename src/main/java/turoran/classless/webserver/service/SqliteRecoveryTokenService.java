package turoran.classless.webserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import turoran.classless.webserver.repository.sqlite.RecoveryTokenRepository;
import turoran.classless.webserver.repository.sqlite.entities.RecoveryToken;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqliteRecoveryTokenService implements RecoveryTokenService {

    private final RecoveryTokenRepository tokenRepository;
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

        Optional<Long> maxId = tokenRepository.findMaxId();
        maxId.ifPresentOrElse(id -> recoveryToken.setId(id + 1), () -> recoveryToken.setId(1L));


        tokenRepository.save(recoveryToken);

        return token;
    }

    @Override
    public String validateToken(String token) {
        return tokenRepository.findByToken(token)
                .map(RecoveryToken::getEmail)
                .orElse(null);
    }

    @Override
    public String consumeToken(String token) {
        return tokenRepository.findByToken(token)
                .map(rt -> {
                    String email = rt.getEmail();
                    tokenRepository.delete(rt);
                    return email;
                })
                .orElse(null);
    }
}
