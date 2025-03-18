package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.assistant.InvalidAssistantException;
import codes.dimitri.mediminder.api.medication.Color;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.schedule.EventDTO;
import codes.dimitri.mediminder.api.schedule.ScheduleDTO;
import codes.dimitri.mediminder.api.schedule.SchedulePeriodDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Mapper
interface AssistantMapper {
    DateTimeFormatter HUMAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @Mapping(target = "medicationType", source = "medication.medicationType.name")
    @Mapping(target = "administrationType", source = "medication.administrationType.name")
    @Mapping(target = "color", source = "medication.color", qualifiedByName = "mapColorToName")
    AssistantMedicationInfo toAssistantMedicationInfo(MedicationDTO medication, List<AssistantScheduleInfo> schedules, List<AssistantEventInfo> intakesToday);

    @Mapping(target = "when", source = "schedule", qualifiedByName = "mapScheduleToWhen")
    AssistantScheduleInfo toAssistantScheduleInfo(ScheduleDTO schedule);

    @Mapping(target = "completedTime", source = "completedDate", qualifiedByName = "mapLocalDateTimeToLocalTime")
    @Mapping(target = "targetTime", source = "targetDate", qualifiedByName = "mapLocalDateTimeToLocalTime")
    AssistantEventInfo toAssistantEventInfo(EventDTO event);

    @Named("mapColorToName")
    default String mapColorToName(Color color) {
        return color.name().toLowerCase();
    }

    @Named("mapScheduleToWhen")
    default String mapScheduleToWhen(ScheduleDTO schedule) {
        return MessageFormat.format(
            "Take {0} dose every {1} at {2}, {3}",
            schedule.dose().toPlainString(),
            mapIntervalToHumanReadableString(schedule.interval()),
            schedule.time(),
            mapPeriodToHumanReadableString(schedule.period())
        );
    }

    @Named("mapLocalDateTimeToLocalTime")
    default LocalTime mapLocalDateTimeToLocalTime(LocalDateTime dateTime) {
        return dateTime.toLocalTime();
    }

    private static String mapIntervalToHumanReadableString(Period interval) {
        List<String> intervals = new ArrayList<>();
        if (interval.getYears() > 0) intervals.add(interval.getYears() + " year" + (interval.getYears() > 1 ? "s" : ""));
        if (interval.getMonths() > 0) intervals.add(interval.getMonths() + " month" + (interval.getMonths() > 1 ? "s" : ""));
        if (interval.getDays() > 0) intervals.add(interval.getDays() + " day" + (interval.getDays() > 1 ? "s" : ""));
        if (intervals.isEmpty()) throw new InvalidAssistantException("No schedule found");
        return String.join(", ", intervals);
    }

    private static String mapPeriodToHumanReadableString(SchedulePeriodDTO period) {
        if (period.endingAtInclusive() == null) return "starting at " + HUMAN_DATE_FORMATTER.format(period.startingAt());
        return "between " + HUMAN_DATE_FORMATTER.format(period.startingAt()) + " and " + HUMAN_DATE_FORMATTER.format(period.endingAtInclusive()) + " included";
    }
}
