package com.example.ev_charging_system1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 📂 리소스 핸들러 설정
     * 파이썬이 C:/plate_logs 폴더에 저장한 실시간 인식 사진들을
     * 웹 브라우저(Vue)에서 URL 주소로 접근할 수 있게 매핑합니다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 브라우저에서 http://localhost:8080/images/full/파일명.jpg 로 접근 가능
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///C:/plate_logs/")
                .setCachePeriod(0); // 실시간 갱신을 위해 캐시를 끕니다.
    }

    /**
     * 🌍 CORS(Cross-Origin Resource Sharing) 설정
     * Vue(5173)와 파이썬 엔진(5001~5004)이 스프링(8080) API를
     * 자유롭게 호출할 수 있도록 보안 빗장을 풉니다.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // allowedOriginPatterns("*")는 인증정보 허용과 전체 허용을 동시에 가능케 합니다.
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // 쿠키나 인증 헤더를 허용하려면 true로 설정
                .maxAge(3600); // 프리플라이트 요청 캐싱 시간 (1시간)
    }

}