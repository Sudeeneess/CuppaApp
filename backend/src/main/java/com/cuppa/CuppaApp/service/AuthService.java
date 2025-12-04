package com.cuppa.CuppaApp.service;

import com.cuppa.CuppaApp.dto.AuthRequest;
import com.cuppa.CuppaApp.dto.AuthResponse;
import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.UserRepository;
import com.cuppa.CuppaApp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис аутентификации и регистрации пользователей.
 *
 * <p>Содержит бизнес-логику для операций регистрации новых пользователей
 * и аутентификации существующих пользователей в системе мессенджера Cuppa.
 * Включает расширенный мониторинг безопасности и обнаружение аномалий.</p>
 *
 * <p><b>Основные функции:</b>
 * <ul>
 *   <li>Регистрация новых пользователей с проверкой уникальности данных</li>
 *   <li>Аутентификация пользователей по email и паролю</li>
 *   <li>Генерация JWT-токенов для успешно аутентифицированных пользователей</li>
 *   <li>Обновление онлайн-статусов пользователей</li>
 *   <li>Мониторинг безопасности и обнаружение подозрительной активности</li>
 *   <li>Защита от brute-force атак через rate limiting</li>
 * </ul>
 * </p>
 *
 * @author Walerya Pleskova
 * @version 2.1
 * @since 2025-10-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AnomalyDetectionService anomalyDetectionService;
    private final RateLimitService rateLimitService;

    // Хранилище для отслеживания неудачных попыток входа (дополнительная защита)
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_BLOCK_DURATION_MINUTES = 15;

    /**
     * Регистрирует нового пользователя в системе.
     *
     * <p><b>Процесс регистрации:</b>
     * <ol>
     *   <li>Проверка уникальности email и имени пользователя</li>
     *   <li>Хэширование пароля для безопасного хранения</li>
     *   <li>Создание и сохранение пользователя в базе данных</li>
     *   <li>Генерация JWT-токена для нового пользователя</li>
     *   <li>Логирование события регистрации</li>
     * </ol>
     * </p>
     *
     * @param request DTO с данными для регистрации (email, username, password, имена)
     * @return AuthResponse с JWT-токеном и данными пользователя
     * @throws RuntimeException если email или имя пользователя уже используются
     * @see AuthRequest
     * @see AuthResponse
     */
    public AuthResponse register(AuthRequest request) {
        log.info("Попытка регистрации нового пользователя: {}", request.getEmail());

        // ПРОВЕРКА RATE LIMITING ДЛЯ РЕГИСТРАЦИИ
        if (!rateLimitService.isAllowed("registration_" + request.getEmail())) {
            log.warn("Превышен лимит запросов на регистрацию для: {}", request.getEmail());
            throw new RuntimeException("Слишком много запросов на регистрацию. Попробуйте позже.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Попытка регистрации с уже существующим email: {}", request.getEmail());
            throw new RuntimeException("Email уже используется");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Попытка регистрации с уже существующим username: {}", request.getUsername());
            throw new RuntimeException("Имя пользователя уже занято");
        }

        // ВАЛИДАЦИЯ ПАРОЛЯ
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Пароль должен содержать минимум 6 символов");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirst_name());
        user.setLastName(request.getLast_name());
        user.setIsOnline(false);
        user.setLastSeen(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        // ЛОГИРОВАНИЕ УСПЕШНОЙ РЕГИСТРАЦИИ
        anomalyDetectionService.recordUserAction(
                savedUser.getEmail(),
                AnomalyDetectionService.ActionType.LOGIN_SUCCESS,
                "registration",
                "New user registered successfully"
        );

        log.info("Успешная регистрация пользователя: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
        return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }

    /**
     * Выполняет аутентификацию пользователя в системе.
     *
     * <p><b>Процесс аутентификации:</b>
     * <ol>
     *   <li>Проверка блокировки аккаунта из-за множественных неудачных попыток</li>
     *   <li>Поиск пользователя по email в базе данных</li>
     *   <li>Проверка соответствия введенного пароля хэшированному паролю</li>
     *   <li>Обновление онлайн-статуса и времени последней активности</li>
     *   <li>Генерация JWT-токена для аутентифицированного пользователя</li>
     *   <li>Мониторинг и логирование событий безопасности</li>
     * </ol>
     * </p>
     *
     * @param request DTO с данными для входа (email и password)
     * @return AuthResponse с JWT-токеном и данными пользователя
     * @throws RuntimeException если пользователь не найден, пароль неверный или аккаунт временно заблокирован
     * @see AuthRequest
     * @see AuthResponse
     */
    public AuthResponse login(AuthRequest request) {
        log.info("Попытка входа пользователя: {}", request.getEmail());

        // ПРОВЕРКА RATE LIMITING ДЛЯ АУТЕНТИФИКАЦИИ
        if (!rateLimitService.isAllowed("login_" + request.getEmail())) {
            log.warn("Превышен лимит запросов на вход для: {}", request.getEmail());
            throw new RuntimeException("Слишком много попыток входа. Попробуйте позже.");
        }

        // ПРОВЕРКА БЛОКИРОВКИ АККАУНТА
        checkIfAccountLocked(request.getEmail());

        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        // ЗАПИСЫВАЕМ НЕУДАЧНУЮ ПОПЫТКУ ВХОДА - ПОЛЬЗОВАТЕЛЬ НЕ НАЙДЕН
                        recordFailedLoginAttempt(request.getEmail(), "User not found");
                        anomalyDetectionService.recordUserAction(
                                request.getEmail(),
                                AnomalyDetectionService.ActionType.LOGIN_FAILED,
                                "auth",
                                "User not found"
                        );
                        return new RuntimeException("Пользователь не найден");
                    });

            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                // ЗАПИСЫВАЕМ НЕУДАЧНУЮ ПОПЫТКУ ВХОДА - НЕВЕРНЫЙ ПАРОЛЬ
                recordFailedLoginAttempt(request.getEmail(), "Invalid password");
                anomalyDetectionService.recordUserAction(
                        request.getEmail(),
                        AnomalyDetectionService.ActionType.LOGIN_FAILED,
                        "auth",
                        "Invalid password"
                );
                throw new RuntimeException("Неверный пароль");
            }

            // СБРАСЫВАЕМ СЧЕТЧИК НЕУДАЧНЫХ ПОПЫТОК ПРИ УСПЕШНОМ ВХОДЕ
            loginAttempts.remove(request.getEmail());

            // ОБНОВЛЯЕМ СТАТУС ПОЛЬЗОВАТЕЛЯ
            user.setIsOnline(true);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);

            // ГЕНЕРИРУЕМ ТОКЕН
            String token = jwtTokenProvider.generateToken(user.getEmail());

            // ЗАПИСЫВАЕМ УСПЕШНЫЙ ВХОД
            anomalyDetectionService.recordUserAction(
                    user.getEmail(),
                    AnomalyDetectionService.ActionType.LOGIN_SUCCESS,
                    "auth",
                    "Successful login"
            );

            log.info("Успешный вход пользователя: {} (ID: {})", user.getEmail(), user.getId());
            return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());

        } catch (RuntimeException e) {
            log.warn("Ошибка аутентификации для {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * Проверяет не заблокирован ли аккаунт из-за множественных неудачных попыток входа
     *
     * @param email email пользователя для проверки
     * @throws RuntimeException если аккаунт временно заблокирован
     */
    private void checkIfAccountLocked(String email) {
        LoginAttempt attempt = loginAttempts.get(email);
        if (attempt != null && attempt.getAttempts() >= MAX_LOGIN_ATTEMPTS) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime unlockTime = attempt.getLastAttemptTime().plusMinutes(LOGIN_BLOCK_DURATION_MINUTES);

            if (now.isBefore(unlockTime)) {
                long minutesLeft = java.time.Duration.between(now, unlockTime).toMinutes();

                // ЛОГИРУЕМ ПОПЫТКУ ВХОДА В ЗАБЛОКИРОВАННЫЙ АККАУНТ
                anomalyDetectionService.recordUserAction(
                        email,
                        AnomalyDetectionService.ActionType.LOGIN_FAILED,
                        "auth",
                        "Account temporarily locked - " + minutesLeft + " minutes remaining"
                );

                log.warn("Попытка входа в заблокированный аккаунт: {}", email);
                throw new RuntimeException(
                        "Аккаунт временно заблокирован из-за множественных неудачных попыток входа. " +
                                "Попробуйте через " + minutesLeft + " минут."
                );
            } else {
                // РАЗБЛОКИРОВЫВАЕМ АККАУНТ ПО ИСТЕЧЕНИИ ВРЕМЕНИ
                loginAttempts.remove(email);
                log.info("Автоматическая разблокировка аккаунта: {}", email);
            }
        }
    }

    /**
     * Записывает неудачную попытку входа и проверяет не превышен ли лимит
     *
     * @param email email пользователя
     * @param reason причина неудачи
     */
    private void recordFailedLoginAttempt(String email, String reason) {
        LoginAttempt attempt = loginAttempts.getOrDefault(email, new LoginAttempt());
        attempt.incrementAttempts();
        loginAttempts.put(email, attempt);

        log.warn("Неудачная попытка входа для {}: {} (попытка {}/{})",
                email, reason, attempt.getAttempts(), MAX_LOGIN_ATTEMPTS);

        // ПРОВЕРЯЕМ НЕ ПРЕВЫШЕН ЛИ ЛИМИТ ПОПЫТОК
        if (attempt.getAttempts() >= MAX_LOGIN_ATTEMPTS) {
            anomalyDetectionService.logSecurityEvent(
                    AnomalyDetectionService.SecurityEventType.MULTIPLE_FAILED_LOGINS,
                    email,
                    "Account locked after " + attempt.getAttempts() + " failed login attempts",
                    LocalDateTime.now()
            );
            log.warn("АККАУНТ ЗАБЛОКИРОВАН: {} - превышено максимальное количество неудачных попыток входа", email);
        }
    }

    /**
     * Выход пользователя из системы с логированием события
     *
     * @param userEmail email пользователя
     */
    public void logout(String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setIsOnline(false);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);

            // ЛОГИРУЕМ ВЫХОД ИЗ СИСТЕМЫ
            anomalyDetectionService.recordUserAction(
                    userEmail,
                    AnomalyDetectionService.ActionType.LOGIN_SUCCESS, // Можно добавить отдельный тип для logout
                    "auth",
                    "User logged out"
            );

            log.info("Успешный выход пользователя: {}", userEmail);
        } catch (Exception e) {
            log.error("Ошибка при выходе пользователя {}: {}", userEmail, e.getMessage());
        }
    }



    /**
     * Возвращает информацию о попытках входа для пользователя
     * Для использования в Security Test Controller
     */
    public LoginAttemptInfo getLoginAttemptsInfo(String email) {
        LoginAttempt attempt = loginAttempts.get(email);
        if (attempt == null) {
            return null;
        }

        LocalDateTime unlockTime = attempt.getLastAttemptTime().plusMinutes(LOGIN_BLOCK_DURATION_MINUTES);
        boolean isLocked = attempt.getAttempts() >= MAX_LOGIN_ATTEMPTS &&
                LocalDateTime.now().isBefore(unlockTime);

        return new LoginAttemptInfo(
                email,
                attempt.getAttempts(),
                MAX_LOGIN_ATTEMPTS,
                isLocked,
                unlockTime
        );
    }

    /**
     * Внутренний класс для отслеживания попыток входа
     */
    private static class LoginAttempt {
        private int attempts = 0;
        private LocalDateTime lastAttemptTime = LocalDateTime.now();

        public void incrementAttempts() {
            this.attempts++;
            this.lastAttemptTime = LocalDateTime.now();
        }

        public int getAttempts() { return attempts; }
        public LocalDateTime getLastAttemptTime() { return lastAttemptTime; }
    }

    /**
     * DTO для информации о попытках входа
     */
    public static class LoginAttemptInfo {
        private final String email;
        private final int currentAttempts;
        private final int maxAttempts;
        private final boolean isLocked;
        private final LocalDateTime unlockTime;

        public LoginAttemptInfo(String email, int currentAttempts, int maxAttempts,
                                boolean isLocked, LocalDateTime unlockTime) {
            this.email = email;
            this.currentAttempts = currentAttempts;
            this.maxAttempts = maxAttempts;
            this.isLocked = isLocked;
            this.unlockTime = unlockTime;
        }

        // Getters
        public String getEmail() { return email; }
        public int getCurrentAttempts() { return currentAttempts; }
        public int getMaxAttempts() { return maxAttempts; }
        public boolean isLocked() { return isLocked; }
        public LocalDateTime getUnlockTime() { return unlockTime; }
    }
}