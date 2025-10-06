package turoran.classless.webserver.service;

import java.util.Optional;

public interface ClientService {

    Optional<String> registerAccount(String username, String password, String email);
    Optional<String> changePassword(String email, String newPassword);

    boolean emailExists(String email);
}
