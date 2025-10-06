package turoran.classless.webserver.repository.mysql.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "account")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", unique = true, nullable = false, length = 32)
    private String username;

    @Column(name = "salt", nullable = false, length = 32)
    private byte[] salt;

    @Column(name = "verifier", nullable = false, length = 32)
    private byte[] verifier;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "reg_mail", length = 255)
    private String regMail;
}
