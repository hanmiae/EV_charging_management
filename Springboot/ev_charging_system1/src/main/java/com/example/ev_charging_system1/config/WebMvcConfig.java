package com.example.ev_charging_system1.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${ev.plate-logs.dir}")
    private String plateLogsDir;

    /**
     * 📂 리소스 핸들러 설정
     * Python이 plate_logs 폴더에 저장한 실시간 인식 사진을
     * Vue에서 /images/** URL로 접근할 수 있게 매핑합니다.
     *
     * 한글 경로(예: C:\tool\1차프로젝트\plate_logs)도 안전하게 처리하기 위해
     * file: URI 대신 FileSystemResource 를 직접 location 으로 등록합니다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absolute = Path.of(plateLogsDir).toAbsolutePath().normalize();
        String osPath = absolute.toString();
        if (!osPath.endsWith(java.io.File.separator)) {
            osPath = osPath + java.io.File.separator;
        }
        FileSystemResource location = new FileSystemResource(osPath);
        log.info("[ResourceHandler] /images/** -> {} (exists={})", osPath, absolute.toFile().isDirectory());

        registry.addResourceHandler("/images/**")
                .addResourceLocations(location)
                .setCachePeriod(0);
    }

    /**
     * 🌍 CORS 설정
     * Vue(5173)와 Python 엔진(5001~5004)이 Spring(8080)을 호출할 수 있도록 허용.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
