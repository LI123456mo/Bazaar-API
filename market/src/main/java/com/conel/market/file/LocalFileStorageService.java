package com.conel.market.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService{
    private final String uploadDir="uploads";

    @Override
    public String saveFile(MultipartFile file){
        if (file.isEmpty()){
            throw new RuntimeException("Cannot save empty file.");
        }
        try {
            Path rootPath= Paths.get(uploadDir);
            if (!Files.exists(rootPath)){
                Files.createDirectories(rootPath);
            }
            String fileName=System.currentTimeMillis()+"_"+file.getOriginalFilename();
            Path filePath=rootPath.resolve(fileName);//creates full path

            Files.copy(file.getInputStream(),filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File saved successfully to: {}", filePath);
            return fileName;
        }catch (IOException e){
            log.error("Failed to store file", e);
            throw new RuntimeException("Could not store file: " + e.getMessage());
        }
    }
}
