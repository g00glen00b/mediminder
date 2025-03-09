package codes.dimitri.mediminder.api.assistant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface AssistantManager {
    AssistantResponseDTO answer(@Valid @NotNull AssistantRequestDTO request);
}
