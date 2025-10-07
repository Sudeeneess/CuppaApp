package com.cuppa.CuppaApp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Провайдер для работы с JWT (JSON Web Token) токенами.
 *
 * <p>Отвечает за создание, валидацию и извлечение данных из JWT-токенов
 * используемых для аутентификации пользователей в системе.</p>
 *
 * <p><b>Функциональность:</b>
 * <ul>
 *   <li>Генерация JWT-токенов с указанием имени пользователя и срока действия</li>
 *   <li>Валидация токенов на подлинность и срок действия</li>
 *   <li>Извлечение имени пользователя из валидного токена</li>
 * </ul>
 * </p>
 *
 * <p>Использует алгоритм подписи HS512 для обеспечения безопасности токенов.</p>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-06
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpirationMs;

    /**
     * Создает секретный ключ для подписи и верификации JWT-токенов.
     *
     * <p>Использует HMAC-SHA алгоритм на основе секретной строки из конфигурации.</p>
     *
     * @return секретный ключ для работы с JWT
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Генерирует JWT-токен для указанного имени пользователя.
     *
     * <p>Создает токен содержащий:
     * <ul>
     *   <li>Subject (sub) - имя пользователя</li>
     *   <li>Issued At (iat) - время создания токена</li>
     *   <li>Expiration (exp) - время истечения срока действия</li>
     * </ul>
     * </p>
     *
     * @param email имя пользователя для которого генерируется токен
     * @return JWT-токен в виде строки
     * @throws IllegalArgumentException если имя пользователя пустое или null
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Извлекает имя пользователя из JWT-токена.
     *
     * <p>Парсит токен и возвращает значение поля Subject (sub).
     * Перед извлечением данных токен должен быть валидирован.</p>
     *
     * @param token JWT-токен
     * @return имя пользователя из токена
     * @throws JwtException если токен невалиден, истек или поврежден
     * @throws IllegalArgumentException если токен пустой или null
     * @see #validateToken(String)
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * Проверяет валидность JWT-токена.
     *
     * <p>Выполняет следующие проверки:
     * <ul>
     *   <li>Корректность подписи токена</li>
     *   <li>Срок действия токена (не истек)</li>
     *   <li>Корректность формата токена</li>
     * </ul>
     * </p>
     *
     * @param token JWT-токен для проверки
     * @return true если токен валиден, false если токен невалиден, истек или поврежден
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}