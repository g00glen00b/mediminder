package codes.dimitri.mediminder.api.medication.implementation;

import org.springframework.data.jpa.repository.JpaRepository;

interface MedicationTypeEntityRepository extends JpaRepository<MedicationTypeEntity, String> {
}
