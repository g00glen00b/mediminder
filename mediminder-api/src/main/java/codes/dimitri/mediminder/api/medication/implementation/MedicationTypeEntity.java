package codes.dimitri.mediminder.api.medication.implementation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "medication_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class MedicationTypeEntity {
    @Id
    private String id;
    private String name;
    @ManyToMany
    @JoinTable(
        name = "medication_type_administration_type",
        joinColumns = @JoinColumn(name = "medication_type_id"),
        inverseJoinColumns = @JoinColumn(name = "administration_type_id")
    )
    private List<AdministrationTypeEntity> administrationTypes;
    @ManyToMany
    @JoinTable(
        name = "medication_type_dose_type",
        joinColumns = @JoinColumn(name = "medication_type_id"),
        inverseJoinColumns = @JoinColumn(name = "dose_type_id")
    )
    private List<DoseTypeEntity> doseTypes;
}
