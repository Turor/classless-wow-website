package turoran.classless.webserver.repository.sqlite.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "recovery_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryToken {

    @Id
    private Long id;

    private String token;
    
    private String email;

    private Instant created = Instant.now();

    private Instant expires = Instant.now().plusSeconds(60 * 60 * 24);
}
