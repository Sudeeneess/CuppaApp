package com.cuppa.CuppaApp.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * JWT-фильтр аутентификации для обработки токенов в каждом HTTP-запросе.
 *
 * <p>Наследуется от {@link OncePerRequestFilter} для гарантии однократного выполнения
 * на каждый запрос. Извлекает JWT-токен из заголовка Authorization, валидирует его
 * и устанавливает аутентификацию в контексте безопасности Spring.</p>
 *
 * <p><b>Рабочий процесс:</b>
 * <ol>
 *   <li>Извлечение JWT из заголовка Authorization</li>
 *   <li>Валидация токена с помощью {@link JwtTokenProvider}</li>
 *   <li>Извлечение имени пользователя из токена</li>
 *   <li>Создание объекта аутентификации и установка в SecurityContext</li>
 * </ol>
 * </p>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-06
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Конструктор JWT-фильтра аутентификации.
     *
     * @param jwtTokenProvider провайдер для работы с JWT-токенами (валидация, извлечение данных)
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Обрабатывает каждый HTTP-запрос для JWT-аутентификации.
     *
     * <p>Основной метод фильтра, который выполняется для каждого входящего запроса.
     * Извлекает JWT-токен, проверяет его валидность и устанавливает аутентификацию
     * в контексте безопасности Spring Security.</p>
     *
     * <p><b>Обработка ошибок:</b> В случае исключений при обработке токена
     * ошибка логируется, но запрос продолжает обработку в цепочке фильтров.</p>
     *
     * @param request     HTTP-запрос
     * @param response    HTTP-ответ
     * @param filterChain цепочка фильтров Spring Security
     * @throws ServletException если возникает ошибка сервлета
     * @throws IOException      если возникает ошибка ввода-вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Не удалось установить аутентификацию пользователя в security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает JWT-токен из заголовка Authorization HTTP-запроса.
     *
     * <p>Ожидает токен в формате: {@code Bearer {jwt-token}}</p>
     *
     * @param request HTTP-запрос
     * @return JWT-токен без префикса "Bearer " или null если токен отсутствует или невалиден
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}