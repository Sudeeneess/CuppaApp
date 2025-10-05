package com.cuppa.CuppaApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности Spring Security для приложения Cuppa
 *
 * <p>На начальном этапе разработки предоставляет временную заглушку безопасности,
 * отключающую все проверки аутентификации и авторизации.
 *
 * <p><b>ПЛАН РАЗРАБОТКИ:</b>
 * <ol>
 *   <li>Текущая версия: полный доступ для всех запросов</li>
 *   <li>Следующая версия: JWT аутентификация для защищенных эндпоинтов</li>
 *   <li>Финальная версия: ролевая модель + CSRF защита для веб-интерфейса</li>
 * </ol>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 05.10.2025
 * @see <a href="https://docs.spring.io/spring-security/reference/index.html">
 * Spring Security Documentation</a>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Настраивает цепочку фильтров безопасности для HTTP запросов
     *
     * <p>Временная конфигурация обеспечивает:
     * <ul>
     *   <li>Отключение CSRF защиты (не требуется для REST API)</li>
     *   <li>Разрешение всех запросов без аутентификации</li>
     *   <li>Отключение базовой HTTP аутентификации</li>
     * </ul>
     *
     * <p><b>Примечание:</b> Временная конфигурация будет заменена на полноценную
     * систему аутентификации с JWT токенами на следующем этапе разработки.
     *
     * @param http объект для настройки web безопасности
     * @return сконфигурированная цепочка фильтров безопасности
     * @throws Exception если произошла ошибка конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // отключаем CSRF
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // разрешаем ВСЕ запросы без авторизации
                );

        return http.build();
    }
}
