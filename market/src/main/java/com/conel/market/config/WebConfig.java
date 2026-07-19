package com.conel.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
     @Value("${app.file-storage.upload-dir:./uploads}")
     private String uploadDir;

     @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
       // Use the same configured upload directory as the storage service so links stay valid in every environment.
       Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

       registry.addResourceHandler("/uploads/**")
               .addResourceLocations("file:" + uploadPath + "/");
    }
}
