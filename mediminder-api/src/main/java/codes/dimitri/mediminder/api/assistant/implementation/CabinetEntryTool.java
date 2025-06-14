package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryDTO;
import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.cabinet.CreateCabinetEntryRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class CabinetEntryTool implements AssistantTool {
    private final CabinetEntryManager manager;
    private final AssistantProperties properties;

    @Tool(description = "get all medication packages for a given medication")
    List<CabinetEntryDTO> findAllEntriesByMedicationId(String medicationId) {
        log.debug("Called findAllEntriesByMedicationId with medicationId: {}", medicationId);
        return manager
            .findAllForCurrentUser(UUID.fromString(medicationId), PageRequest.ofSize(properties.maxSize()))
            .getContent();
    }

    @Tool(description = "get all medication packages")
    List<CabinetEntryDTO> findAllEntries() {
        log.debug("Called findAllEntries");
        return manager
            .findAllForCurrentUser(null, PageRequest.ofSize(properties.maxSize()))
            .getContent();
    }

    @Tool(description = "add a medication package")
    void addEntry(
        @ToolParam(description = "UUID of the medication") String medicationId,
        @ToolParam(description = "Expiry date of the package", required = false) String expirationDate,
        @ToolParam(description = "Number of doses remaining inside the package") String doses) {
        log.debug("Called addEntry with medicationId: {}, expirationDate: {}, doses: {}", medicationId, expirationDate, doses);
        manager.createForCurrentUser(new CreateCabinetEntryRequestDTO(
            UUID.fromString(medicationId),
            new BigDecimal(doses),
            LocalDate.parse(expirationDate)
        ));
    }
}
