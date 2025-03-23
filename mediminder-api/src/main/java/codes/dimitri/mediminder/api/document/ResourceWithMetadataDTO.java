package codes.dimitri.mediminder.api.document;

import org.springframework.core.io.Resource;

public record ResourceWithMetadataDTO(Resource resource, String filename, String contentType) {
}
