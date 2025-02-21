package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.DoseTypeDTO;
import org.mapstruct.Mapper;

@Mapper
interface DoseTypeEntityMapper {
    DoseTypeDTO toDTO(DoseTypeEntity entity);
}
