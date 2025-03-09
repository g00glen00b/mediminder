package codes.dimitri.mediminder.api.assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantRequestDTO(
    @NotBlank(message = "Question is required")
    @Size(max = 256, message = "Question must be less than {max} characters")
    String question) {
}
