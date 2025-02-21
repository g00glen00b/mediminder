package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.AdministrationTypeDTO;
import org.mapstruct.Mapper;

@Mapper
interface AdministrationTypeEntityMapper {
    AdministrationTypeDTO toDTO(AdministrationTypeEntity entity);
}
