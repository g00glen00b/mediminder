package codes.dimitri.mediminder.api.cabinet.implementation;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryDTO;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface CabinetEntryMapper {
    @Mapping(source = "entity.id", target = "id")
    CabinetEntryDTO toDTO(CabinetEntryEntity entity, MedicationDTO medication);
}
