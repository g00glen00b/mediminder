package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.Color;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "medication")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class MedicationEntity {
    @Id
    private UUID id;
    private String userId;
    private String name;
    @ManyToOne
    @JoinColumn(name = "medication_type_id")
    private MedicationTypeEntity medicationType;
    @ManyToOne
    @JoinColumn(name = "administration_type_id")
    private AdministrationTypeEntity administrationType;
    @ManyToOne
    @JoinColumn(name = "dose_type_id")
    private DoseTypeEntity doseType;
    private BigDecimal dosesPerPackage;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Color color;

    public MedicationEntity(String userId, String name, MedicationTypeEntity medicationType, AdministrationTypeEntity administrationType, DoseTypeEntity doseType, BigDecimal dosesPerPackage, Color color) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.medicationType = medicationType;
        this.administrationType = administrationType;
        this.doseType = doseType;
        this.dosesPerPackage = dosesPerPackage;
        this.color = color;
    }
}
