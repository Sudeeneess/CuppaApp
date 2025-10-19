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
                        "http://localhost:3000",    // Create React App
                        "http://localhost:5173",    // Vite
                        "http://localhost:8080",    // Стандартный dev server
                        "http://localhost:8081",    // Альтернативный порт

                        // Expo порты (ВСЕ возможные)
                        "http://localhost:19000",   // Expo Dev Tools
                        "http://localhost:19001",   // Metro Bundler
                        "http://localhost:19002",   // Expo Tunnel
                        "http://localhost:19006",   // Expo Dev Client

                        // Дополнительные порты
                        "http://localhost:5000",    // Svelte/Solid
                        "http://localhost:4200",    // Angular
                        "http://localhost:1234",    // Parcel

                        // IP адреса для тестирования на телефоне
                        "http://127.0.0.1:3000",
                        "http://127.0.0.1:5173",
                        "http://127.0.0.1:19000",
                        "http://192.168.0.100:3000",
                        "http://192.168.1.100:3000",
                        "http://192.168.0.101:3000",
                        "http://192.168.1.101:3000",
                        "http://10.0.2.2:3000",     // Android Emulator

                        // Render.com для продакшена
                        "https://cuppaapp.onrender.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}