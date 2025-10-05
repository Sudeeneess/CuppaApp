package com.cuppa.CuppaApp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация CORS (Cross-Origin Resource Sharing) для приложения Cuppa
 *
 * <p>Настраивает правила доступа к API с внешних доменов.
 * В режиме разработки разрешает запросы со всех источников.
 *
 * <p><b>ВНИМАНИЕ:</b> В продакшн-среде необходимо ограничить allowedOrigins
 * конкретными доменами фронтенд-приложения.
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 05.10.2025
 */

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Настраивает CORS правила для всех эндпоинтов, начинающихся с /api/
     *
     * <p>Конфигурация включает:
     * <ul>
     *   <li>Разрешение запросов со всех источников ({@code allowedOrigins("*")})</li>
     *   <li>Разрешение основных HTTP методов: GET, POST, DELETE, OPTIONS</li>
     *   <li>Разрешение всех заголовков</li>
     *   <li>Отключение учетных данных (credentials)</li>
     * </ul>
     *
     * @param registry реестр для настройки CORS правил
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
