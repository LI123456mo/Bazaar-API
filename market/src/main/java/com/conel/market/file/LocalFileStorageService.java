package com.conel.market.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    @Value("${app.file-storage.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.file-storage.max-file-size-mb:5}")
    private long maxFileSizeMb;

    @Value("#{'${app.file-storage.allowed-content-types:image/jpeg,image/png,image/webp}'.split(',')}")
    private List<String> allowedContentTypes;

    private Path rootPath;

    // Resolve and validate the storage root ONCE at startup, not on every request.
    @PostConstruct
    public void init() {
        try {
            this.rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(rootPath);
            log.info("File storage initialized at: {}", rootPath);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage directory: " + uploadDir, e);
        }
    }

    @Override
    public String saveFile(MultipartFile file) {
        validateFile(file);

        String originalName = StringUtils.cleanPath(
                requireNonBlank(file.getOriginalFilename(), "Original filename is missing")
        );

        // Reject path traversal attempts explicitly, rather than silently stripping them.
        if (originalName.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalName);
        }

        String extension = getExtension(originalName);
        String fileName = UUID.randomUUID() + extension;
        Path targetPath = rootPath.resolve(fileName).normalize();

        // Defense in depth: confirm the resolved path is still inside rootPath.
        if (!targetPath.getParent().equals(rootPath)) {
            throw new FileStorageException("Resolved path escapes storage directory: " + fileName);
        }

        try {
            // Write to a temp file first, then atomically move it into place.
            // This avoids leaving a half-written file if the process crashes mid-copy.
            Path tempPath = Files.createTempFile(rootPath, "upload_", ".tmp");
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file '{}' as '{}' ({} bytes)", originalName, fileName, file.getSize());
            return fileName;

        } catch (IOException e) {
            log.error("Failed to store file '{}'", originalName, e);
            throw new FileStorageException("Could not store file. Please try again.", e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path target = rootPath.resolve(fileName).normalize();
            if (!target.getParent().equals(rootPath)) {
                throw new FileStorageException("Invalid file reference: " + fileName);
            }
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.error("Failed to delete file '{}'", fileName, e);
            throw new FileStorageException("Could not delete file: " + fileName, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot save an empty file.");
        }

        long maxBytes = maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new FileStorageException("File exceeds maximum allowed size of " + maxFileSizeMb + "MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            throw new FileStorageException(
                    "Unsupported file type: " + contentType + ". Allowed types: " + allowedContentTypes
            );
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex) : "";
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new FileStorageException(message);
        }
        return value;
    }
}