package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.MedicationTypeDTO;
import org.mapstruct.Mapper;

@Mapper
interface MedicationTypeEntityMapper {
    MedicationTypeDTO toDTO(MedicationTypeEntity entity);
}
