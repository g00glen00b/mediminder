package codes.dimitri.mediminder.api.schedule.implementation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "completed_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class CompletedEventEntity {
    @Id
    private UUID id;
    private String userId;
    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private ScheduleEntity schedule;
    private LocalDateTime targetDate;
    private LocalDateTime completedDate;
    private BigDecimal dose;

    public CompletedEventEntity(String userId, ScheduleEntity schedule, LocalDateTime targetDate, LocalDateTime completedDate, BigDecimal dose) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.schedule = schedule;
        this.targetDate = targetDate;
        this.completedDate = completedDate;
        this.dose = dose;
        this.schedule.getCompletedEvents().add(this);
    }
}
