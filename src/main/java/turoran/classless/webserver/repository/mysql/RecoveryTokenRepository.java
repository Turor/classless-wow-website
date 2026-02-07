package turoran.classless.webserver.repository.mysql;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import turoran.classless.webserver.repository.mysql.entities.RecoveryToken;

import java.util.Optional;

@Repository
@Profile("prod")
public interface RecoveryTokenRepository extends JpaRepository<RecoveryToken, Long> {
    Optional<RecoveryToken> findByEmail(String email);
    Optional<RecoveryToken> findByToken(String token);

    @Query("SELECT MAX(r.id) FROM RecoveryToken r")
    Optional<Long> findMaxId();
}
