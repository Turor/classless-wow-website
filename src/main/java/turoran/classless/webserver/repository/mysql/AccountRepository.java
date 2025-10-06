package turoran.classless.webserver.repository.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import turoran.classless.webserver.repository.mysql.entities.Account;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    
    boolean existsByUsernameIgnoreCase(String username);
    
    Optional<Account> findByUsernameIgnoreCase(String username);
    
    Optional<Account> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT MAX(a.id) FROM Account a")
    Optional<Integer> findMaxId();
}
