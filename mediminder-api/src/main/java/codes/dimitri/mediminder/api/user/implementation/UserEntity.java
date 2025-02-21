package codes.dimitri.mediminder.api.user.implementation;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {
    @Id
    private UUID id;
    private String email;
    private String password;
    private String name;
    private ZoneId timezone;
    private boolean enabled;
    private boolean admin;
    private String verificationCode;
    private String passwordResetCode;
    @LastModifiedDate
    private Instant lastModifiedDate;

    public UserEntity(String email, String password, String name, ZoneId timezone, String verificationCode) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.password = password;
        this.name = name;
        this.timezone = timezone;
        this.enabled = false;
        this.admin = false;
        this.verificationCode = verificationCode;
    }
}
