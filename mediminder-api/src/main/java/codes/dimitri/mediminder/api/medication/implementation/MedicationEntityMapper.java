package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDTO;
import org.mapstruct.Mapper;

@Mapper
interface MedicationEntityMapper {
    MedicationDTO toDTO(MedicationEntity entity);
}
