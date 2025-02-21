package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.notification.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationEntity {
    @Id
    private UUID id;
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private NotificationType type;
    private UUID initiatorId;
    private String title;
    private String message;
    private Instant deleteAt;
    private boolean active;

    public NotificationEntity(UUID userId, NotificationType type, UUID initiatorId, String title, String message, Instant deleteAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.type = type;
        this.initiatorId = initiatorId;
        this.title = title;
        this.message = message;
        this.deleteAt = deleteAt;
        this.active = true;
    }
}
