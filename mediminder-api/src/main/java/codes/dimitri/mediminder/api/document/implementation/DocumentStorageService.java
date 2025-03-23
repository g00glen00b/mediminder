package codes.dimitri.mediminder.api.document.implementation;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

interface DocumentStorageService {
    void upload(DocumentEntity entity, MultipartFile file);
    Resource download(DocumentEntity entity);
    void delete(DocumentEntity entity);
}
