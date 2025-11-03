package com.cuppa.CuppaApp.config;

import com.cuppa.CuppaApp.security.JwtAuthenticationFilter;
import com.cuppa.CuppaApp.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация безопасности Spring Security для приложения Cuppa.
 *
 * <p>Настраивает JWT-аутентификацию, правила доступа к эндпоинтам,
 * управление сессиями и дополнительные меры безопасности для REST API.</p>
 *
 * <p><b>Основные функции:</b>
 * <ul>
 *   <li>Настройка JWT-аутентификации через фильтры</li>
 *   <li>Определение правил доступа к API эндпоинтам</li>
 *   <li>Конфигурация кодировщика паролей</li>
 *   <li>Настройка CORS политики</li>
 *   <li>Заголовки безопасности HTTP</li>
 *   <li>Отключение CSRF для REST API</li>
 *   <li>Настройка stateless сессий</li>
 * </ul>
 * </p>
 *
 * @author Walerya Pleskova
 * @version 2.1
 * @since 2025-10-28
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.security.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Value("${app.security.headers.content-security-policy:default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:}")
    private String contentSecurityPolicy;

    /**
     * Конструктор конфигурации безопасности.
     *
     * @param jwtTokenProvider провайдер для работы с JWT-токенами
     */
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Создает bean-компонент для шифрования паролей.
     *
     * <p>Использует алгоритм BCrypt для безопасного хранения паролей
     * в базе данных. BCrypt автоматически генерирует соль(salt — это случайные данные, которые добавляются к паролю
     * перед его хешированием.)
     * обеспечивает защиту от атак перебором.</p>
     *
     * @return кодировщик паролей BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создает bean-компонент менеджера аутентификации.
     *
     * @param authenticationConfiguration конфигурация аутентификации Spring Security
     * @return менеджер аутентификации
     * @throws Exception если возникла ошибка при создании менеджера
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Настраивает цепочку фильтров безопасности HTTP.
     *
     * <p><b>Конфигурация включает:</b>
     * <ul>
     *   <li>Настройку CORS политики</li>
     *   <li>Отключение CSRF защиты для REST API</li>
     *   <li>Настройка stateless сессий (без сохранения состояния)</li>
     *   <li>Определение правил доступа к эндпоинтам</li>
     *   <li>Добавление JWT-фильтра аутентификации</li>
     *   <li>Заголовки безопасности HTTP (CSP, HSTS)</li>
     *   <li>Разрешение Swagger без аутентификации</li>
     * </ul>
     * </p>
     *
     * @param http построитель конфигурации HTTP безопасности
     * @return сконфигурированная цепочка фильтров безопасности
     * @throws Exception если возникла ошибка при конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //НАСТРОЙКА CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ОТКЛЮЧЕНИЕ CSRF ДЛЯ REST API
                .csrf(AbstractHttpConfigurer::disable)

                // НАСТРОЙКА STATELESS СЕССИЙ
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // НАСТРОЙКА ЗАГОЛОВКОВ БЕЗОПАСНОСТИ
                .headers(headers -> headers
                        // Content Security Policy - основная защита от XSS
                        .contentSecurityPolicy(csp -> csp.policyDirectives(contentSecurityPolicy))
                        // Защита от clickjacking
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        // HSTS - HTTP Strict Transport Security
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000) // 1 year
                        )
                        // XSS Protection deprecated в новых версиях - используем CSP вместо этого
                        // MIME type sniffing protection
                        .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable)
                )

                //  НАСТРОЙКА ПРАВ ДОСТУПА
                .authorizeHttpRequests(auth -> auth
                        // Разрешить OPTIONS запросы для CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // PUBLIC ENDPOINTS
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/v3/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // AUTH ENDPOINTS
                        .requestMatchers("/api/auth/**").permitAll()

                        // PROTECTED ENDPOINTS
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/chat-rooms/**").authenticated()
                        .requestMatchers("/api/chat-participants/**").authenticated()
                        .requestMatchers("/api/messages/**").authenticated()

                        // ADMIN ENDPOINTS
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )

                // ДОБАВЛЕНИЕ JWT ФИЛЬТРА
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Настраивает политику CORS (Cross-Origin Resource Sharing).
     *
     * <p>Определяет разрешенные источники, методы и заголовки для кросс-доменных запросов.
     * Обеспечивает безопасное взаимодействие фронтенда и бэкенда.</p>
     *
     * @return конфигурация CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // РАЗРЕШЕННЫЕ ИСТОЧНИКИ
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // РАЗРЕШЕННЫЕ HTTP МЕТОДЫ
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // РАЗРЕШЕННЫЕ ЗАГОЛОВКИ
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin",
                "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));

        // ДОПОЛНИТЕЛЬНЫЕ НАСТРОЙКИ
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour cache

        // ЭКСПОНИРУЕМЫЕ ЗАГОЛОВКИ
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization", "Content-Disposition"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}