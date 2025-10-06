package turoran.classless.webserver.service;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turoran.classless.webserver.repository.mysql.entities.Account;
import turoran.classless.webserver.repository.mysql.AccountRepository;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientServiceImplementation implements ClientService {

    private static final Logger log = LogManager.getLogger(ClientServiceImplementation.class);
    private final AccountRepository accountRepository;

    // SRP6 constants for WoW
    private static final BigInteger g = BigInteger.valueOf(7);
    private static final BigInteger N = new BigInteger("894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7", 16);

    @Override
    @Transactional
    public Optional<String> registerAccount(String username, String password, String email) {
        try {
            // Check if username already exists
            if (accountRepository.existsByUsernameIgnoreCase(username)) {
                return Optional.of("Username already exists");
            }
            
            // Generate salt and verifier
            byte[] salt = generateSalt();
            byte[] verifier = calculateVerifier(username, password, salt);
            
            // Create new account
            Account account = new Account();
            account.setUsername(username.toUpperCase());
            account.setSalt(salt);
            account.setVerifier(verifier);
            account.setEmail(email != null ? email : "");
            account.setRegMail(email != null ? email : "");
            
            // Save to database
            accountRepository.save(account);
            
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Failed to register account: {}", e.getMessage());
            return Optional.of("Failed to register account: " + e.getMessage());
        }
    }
    
    @Override
    public boolean emailExists(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public Optional<String> changePassword(String email, String newPassword){
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isEmpty()) {
            log.warn("Email {} does not exist", email);
            return Optional.of("Email does not exist");
        } else {
            log.info("Email {} exists", email);
            changePassword(account.get(), newPassword);
            accountRepository.save(account.get());
            return Optional.empty();
        }
    }

    private void changePassword(Account account, String newPassword) {
        byte[] salt = generateSalt();
        byte[] verifier = calculateVerifier(account.getUsername(), newPassword, salt);
        account.setSalt(salt);
        account.setVerifier(verifier);
    }
    
    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return salt;
    }
    
    private byte[] calculateVerifier(String username, String password, byte[] salt) {
        try {
            // Convert to uppercase (WoW requirement)
            username = username.toUpperCase();
            password = password.toUpperCase();
            
            // Calculate h1 = SHA1(username:password)
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            String credentials = username + ":" + password;
            byte[] h1 = sha1.digest(credentials.getBytes());
            
            // Calculate h2 = SHA1(salt + h1)
            sha1.reset();
            sha1.update(salt);
            sha1.update(h1);
            byte[] h2 = sha1.digest();
            
            // Convert h2 to little-endian BigInteger
            byte[] h2Reversed = reverseBytes(h2);
            BigInteger h2Int = new BigInteger(1, h2Reversed);
            
            // Calculate verifier = g^h2 mod N
            BigInteger verifier = g.modPow(h2Int, N);
            
            // Convert to little-endian bytes
            byte[] verifierBytes = verifier.toByteArray();
            return reverseBytes(verifierBytes);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate verifier", e);
        }
    }
    
    private byte[] reverseBytes(byte[] array) {
        byte[] reversed = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            reversed[i] = array[array.length - 1 - i];
        }
        return reversed;
    }
}
