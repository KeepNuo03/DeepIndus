package com.induscore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件静态资源映射配置。
 *
 * 用于将本地导出文件映射为 /files/** 访问。
 */
@Configuration
public class FileResourceConfig implements WebMvcConfigurer {

    private final String exportPath;

    public FileResourceConfig(@Value("${file.export.path}") String exportPath) {
        this.exportPath = exportPath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path path = Paths.get(exportPath).toAbsolutePath().normalize();
        String location = path.toUri().toString();
        registry.addResourceHandler("/files/**")
                .addResourceLocations(location);
    }
}
