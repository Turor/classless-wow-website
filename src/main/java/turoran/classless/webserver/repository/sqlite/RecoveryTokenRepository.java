package turoran.classless.webserver.repository.sqlite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import turoran.classless.webserver.repository.sqlite.entities.RecoveryToken;

import java.util.Optional;

@Repository
public interface RecoveryTokenRepository extends JpaRepository<RecoveryToken, Long> {
    Optional<RecoveryToken> findByEmail(String email);
    Optional<RecoveryToken> findByToken(String token);

    @Query("SELECT MAX(r.id) FROM RecoveryToken r")
    Optional<Long> findMaxId();
}
