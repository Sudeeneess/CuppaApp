package com.cuppa.CuppaApp.config;

import com.cuppa.CuppaApp.security.JwtAuthenticationFilter;
import com.cuppa.CuppaApp.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация безопасности Spring Security для приложения Cuppa.
 *
 * <p>Настраивает JWT-аутентификацию, правила доступа к эндпоинтам
 * и управление сессиями для REST API.</p>
 *
 * <p><b>Основные функции:</b>
 * <ul>
 *   <li>Настройка JWT-аутентификации через фильтры</li>
 *   <li>Определение правил доступа к API эндпоинтам</li>
 *   <li>Конфигурация кодировщика паролей</li>
 *   <li>Отключение CSRF для REST API</li>
 *   <li>Настройка stateless сессий</li>
 * </ul>
 * </p>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-07
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

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
     * в базе данных. BCrypt автоматически генерирует соль и
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
     *   <li>Отключение CSRF защиты для REST API</li>
     *   <li>Настройка stateless сессий (без сохранения состояния)</li>
     *   <li>Определение правил доступа к эндпоинтам</li>
     *   <li>Добавление JWT-фильтра аутентификации</li>
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
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/v3/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/api/chat-rooms/**").authenticated()
                        .requestMatchers("/api/chat-participants/**").authenticated()
                        .requestMatchers("/api/messages/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}