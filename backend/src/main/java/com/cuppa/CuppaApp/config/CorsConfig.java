package com.cuppa.CuppaApp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация CORS (Cross-Origin Resource Sharing) для веб-приложения Cuppa.
 *
 * <p>Настраивает политику совместного использования ресурсов между разными источниками,
 * позволяя фронтенд приложениям взаимодействовать с бэкенд API с различных доменов и портов.
 *
 * <p><b>Настройки CORS:</b>
 * <ul>
 *   <li><b>Разрешенные источники:</b> localhost:3000, localhost:5173, 127.0.0.1:3000</li>
 *   <li><b>Разрешенные HTTP методы:</b> GET, POST, PUT, DELETE, OPTIONS</li>
 *   <li><b>Разрешенные заголовки:</b> Все заголовки</li>
 *   <li><b>Разрешение credentials:</b> Да (куки, аутентификация)</li>
 * </ul>
 *
 * <p><b>Область действия:</b> Конфигурация применяется ко всем endpoint (/**) приложения.
 *
 * <p><b>Важность для разработки:</b> Без данной конфигурации фронтенд приложения,
 * работающие на разных портах (React, Vue, Angular dev servers), не смогут
 * обращаться к бэкенд API из-за политики одинакового источника браузера.
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-14
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Настраивает политику CORS для всего приложения.
     *
     * <p>Определяет правила доступа к API с внешних источников. Конфигурация
     * специально настроена для удобства разработки, разрешая доступ с популярных
     * фронтенд портов разработки.
     *
     * @param registry реестр для настройки CORS mapping
     * @see CorsRegistry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://127.0.0.1:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}