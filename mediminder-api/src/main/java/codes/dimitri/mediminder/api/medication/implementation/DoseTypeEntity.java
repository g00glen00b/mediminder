package codes.dimitri.mediminder.api.medication.implementation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "dose_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class DoseTypeEntity {
    @Id
    private String id;
    private String name;
    @ManyToMany(mappedBy = "doseTypes")
    private List<MedicationTypeEntity> medicationTypes;
}
