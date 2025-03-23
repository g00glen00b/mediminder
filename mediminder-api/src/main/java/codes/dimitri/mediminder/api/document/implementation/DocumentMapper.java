package codes.dimitri.mediminder.api.document.implementation;

import codes.dimitri.mediminder.api.document.DocumentDTO;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface DocumentMapper {
    @Mapping(target = "id", source = "documentEntity.id")
    DocumentDTO toDTO(DocumentEntity documentEntity, MedicationDTO relatedMedication);
}
