package codes.dimitri.mediminder.api.cabinet.implementation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cabinet_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CabinetEntryEntity {
    @Id
    private UUID id;
    private UUID userId;
    private UUID medicationId;
    private BigDecimal remainingDoses;
    private LocalDate expiryDate;

    public CabinetEntryEntity(UUID userId, UUID medicationId, BigDecimal remainingDoses, LocalDate expiryDate) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.medicationId = medicationId;
        this.remainingDoses = remainingDoses;
        this.expiryDate = expiryDate;
    }

    public BigDecimal subtractDoses(BigDecimal dose) {
        BigDecimal resultAfterSubtraction = this.remainingDoses.subtract(dose);
        if (isNegative(resultAfterSubtraction)) {
            this.remainingDoses = BigDecimal.ZERO;
            return resultAfterSubtraction.negate();
        } else {
            this.remainingDoses = resultAfterSubtraction;
            return BigDecimal.ZERO;
        }
    }

    public void addDoses(BigDecimal doses) {
        this.remainingDoses = this.remainingDoses.add(doses);
    }

    public boolean isEmpty() {
        return remainingDoses.compareTo(BigDecimal.ZERO) <= 0;
    }

    private static boolean isNegative(BigDecimal doses) {
        return doses.compareTo(BigDecimal.ZERO) < 0;
    }
}
