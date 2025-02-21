package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.schedule.ScheduleDTO;
import codes.dimitri.mediminder.api.schedule.UserScheduledMedicationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface ScheduleEntityMapper {
    @Mapping(source = "entity.id", target = "id")
    ScheduleDTO toDTO(ScheduleEntity entity, MedicationDTO medication);

    UserScheduledMedicationDTO toUserScheduledMedicationDTO(UserScheduledMedication entity);
}
