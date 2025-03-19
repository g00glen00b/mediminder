package codes.dimitri.mediminder.api.assistant.implementation;

import java.util.List;

public record AssistantMedicationInfo(
    String name,
    String medicationType,
    String administrationType,
    String color,
    List<AssistantScheduleInfo> schedules,
    List<AssistantCabinetEntryInfo> cabinetEntries,
    List<AssistantEventInfo> intakesToday
) {
}
