package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
class MedicationTool implements AssistantTool {
    private final MedicationManager manager;
    private final AssistantProperties properties;

    @Tool(description = "get all medication known to the user")
    List<MedicationDTO> findAllMedications() {
        log.debug("Called findAllMedications");
        return manager
            .findAllForCurrentUser(null, PageRequest.ofSize(properties.maxSize()))
            .getContent();
    }
}
