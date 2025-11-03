package com.cuppa.CuppaApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация CORS (Cross-Origin Resource Sharing) для веб-приложения Cuppa.
 *
 * <p>Настраивает политику совместного использования ресурсов между разными источниками,
 * позволяя фронтенд приложениям взаимодействовать с бэкенд API с различных доменов и портов.
 * Использует централизованную конфигурацию из application.properties.
 *
 * <p><b>Настройки CORS:</b>
 * <ul>
 *   <li><b>Разрешенные источники:</b> Настраиваются через app.security.cors.allowed-origins</li>
 *   <li><b>Разрешенные HTTP методы:</b> GET, POST, PUT, DELETE, OPTIONS, PATCH</li>
 *   <li><b>Разрешенные заголовки:</b> Authorization, Content-Type, X-Requested-With</li>
 *   <li><b>Разрешение credentials:</b> Да (куки, аутентификация)</li>
 * </ul>
 *
 * <p><b>Область действия:</b> Конфигурация применяется ко всем endpoint (/**) приложения.
 *
 * @author Walerya Pleskova
 * @version 2.1
 * @since 2025-10-28
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.security.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    /**
     * Настраивает политику CORS для всего приложения.
     *
     * <p>Определяет правила доступа к API с внешних источников. Конфигурация
     * использует централизованные настройки для согласованности между REST API и WebSocket.
     *
     * @param registry реестр для настройки CORS mapping
     * @see CorsRegistry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        String[] allowedOriginArray = origins.toArray(new String[0]);

        registry.addMapping("/**")
                .allowedOrigins(allowedOriginArray)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
                .exposedHeaders("Authorization", "Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }
}