package com.conel.market.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {
    @Value("${app.file-storage.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public String saveFile(MultipartFile file){
        if (file.isEmpty()){
            throw new RuntimeException("Cannot save empty file.");
        }
        try {
            Path rootPath = Paths.get(uploadDir);
            if (!Files.exists(rootPath)){
                Files.createDirectories(rootPath);
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new RuntimeException("Cannot save file without an original file name.");
            }
            // Strip any client-supplied path information and make the name unique to avoid collisions.
            String originalName = Paths.get(originalFilename).getFileName().toString();
            String fileName = UUID.randomUUID() + "_" + originalName;
            Path filePath = rootPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File saved successfully to: {}", filePath);
            return fileName;
        }catch (IOException e){
            log.error("Failed to store file", e);
            throw new RuntimeException("Could not store file: " + e.getMessage());
        }
    }
}
