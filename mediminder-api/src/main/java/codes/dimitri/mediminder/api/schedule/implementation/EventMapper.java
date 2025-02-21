package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.schedule.EventCompletedEvent;
import codes.dimitri.mediminder.api.schedule.EventDTO;
import codes.dimitri.mediminder.api.schedule.EventUncompletedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper
interface EventMapper {
    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.schedule.id", target = "scheduleId")
    @Mapping(source = "entity.schedule.description", target = "description")
    EventDTO toDTOFromCompletedEvent(CompletedEventEntity entity, MedicationDTO medication);

    @Mapping(source = "entity.id", target = "scheduleId")
    @Mapping(expression = "java(null)", target = "id")
    @Mapping(expression = "java(null)", target = "completedDate")
    EventDTO toDTOFromUncompletedSchedule(ScheduleEntity entity, LocalDateTime targetDate, MedicationDTO medication);

    @Mapping(source = "entity.schedule.id", target = "scheduleId")
    @Mapping(source = "entity.schedule.medicationId", target = "medicationId")
    EventCompletedEvent toCompletedEvent(CompletedEventEntity entity);

    @Mapping(source = "entity.schedule.id", target = "scheduleId")
    @Mapping(source = "entity.schedule.medicationId", target = "medicationId")
    EventUncompletedEvent toUncompletedEvent(CompletedEventEntity entity);
}
