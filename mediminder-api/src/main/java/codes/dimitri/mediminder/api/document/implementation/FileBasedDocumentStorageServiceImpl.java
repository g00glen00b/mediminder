package codes.dimitri.mediminder.api.document.implementation;

import codes.dimitri.mediminder.api.document.InvalidDocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
class FileBasedDocumentStorageServiceImpl implements DocumentStorageService {
    private final DocumentProperties properties;

    @Override
    public void upload(DocumentEntity entity, MultipartFile file) {
        Path folder = getFolderLocation(entity);
        Path fileLocation = getFileLocation(folder, file);
        validateDocumentStaysWithinDirectory(folder, fileLocation);
        try (InputStream stream = file.getInputStream()) {
            Files.createDirectories(folder);
            Files.copy(stream, fileLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new InvalidDocumentException("Failed to upload document", ex);
        }
    }

    private static Path getFileLocation(Path folder, MultipartFile file) {
        return getFileLocation(folder, file.getOriginalFilename());
    }

    private static Path getFileLocation(Path folder, String filename) {
        return Paths.get(folder.toString(), filename);
    }

    private Path getFolderLocation(DocumentEntity entity) {
        return Paths.get(properties.storageLocation(), entity.getId().toString());
    }

    private static void validateDocumentStaysWithinDirectory(Path documentLocation, Path location) {
        if (!location.getParent().toAbsolutePath().equals(documentLocation.toAbsolutePath())) {
            throw new InvalidDocumentException("Invalid document location");
        }
    }

    @Override
    public Resource download(DocumentEntity entity) {
        Path folder = getFolderLocation(entity);
        Path file = getFileLocation(folder, entity.getFilename());
        return new FileSystemResource(file);
    }

    @Override
    public void delete(DocumentEntity entity) {
        Path folder = getFolderLocation(entity);
        Path file = getFileLocation(folder, entity.getFilename());
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            throw new InvalidDocumentException("Failed to delete document", ex);
        }
    }
}
