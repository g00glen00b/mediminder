package codes.dimitri.mediminder.api.medication.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

interface AdministrationTypeEntityRepository extends JpaRepository<AdministrationTypeEntity, String> {
    @Query("select distinct t from AdministrationTypeEntity t inner join t.medicationTypes m where m.id = ?1")
    Page<AdministrationTypeEntity> findAllByMedicationTypeId(String id, Pageable pageable);

    @Query("select distinct t from AdministrationTypeEntity t inner join t.medicationTypes m where t.id = ?1 and m.id = ?2")
    Optional<AdministrationTypeEntity> findByIdAndMedicationTypeId(String id, String medicationTypeId);
}
