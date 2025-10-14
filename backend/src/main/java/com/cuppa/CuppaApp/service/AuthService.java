package com.cuppa.CuppaApp.service;

import com.cuppa.CuppaApp.dto.AuthRequest;
import com.cuppa.CuppaApp.dto.AuthResponse;
import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.UserRepository;
import com.cuppa.CuppaApp.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Сервис аутентификации и регистрации пользователей.
 *
 * <p>Содержит бизнес-логику для операций регистрации новых пользователей
 * и аутентификации существующих пользователей в системе мессенджера Cuppa.</p>
 *
 * <p><b>Основные функции:</b>
 * <ul>
 *   <li>Регистрация новых пользователей с проверкой уникальности данных</li>
 *   <li>Аутентификация пользователей по email и паролю</li>
 *   <li>Генерация JWT-токенов для успешно аутентифицированных пользователей</li>
 *   <li>Обновление онлайн-статусов пользователей</li>
 * </ul>
 * </p>
 *
 * @author Walerya Pleskova
 * @version 1.0
 * @since 2025-10-07
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Конструктор сервиса аутентификации.
     *
     * @param userRepository репозиторий для работы с данными пользователей
     * @param passwordEncoder кодировщик паролей для безопасного хранения
     * @param jwtTokenProvider провайдер для генерации JWT-токенов
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Регистрирует нового пользователя в системе.
     *
     * <p><b>Процесс регистрации:</b>
     * <ol>
     *   <li>Проверка уникальности email и имени пользователя</li>
     *   <li>Хэширование пароля для безопасного хранения</li>
     *   <li>Создание и сохранение пользователя в базе данных</li>
     *   <li>Генерация JWT-токена для нового пользователя</li>
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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email уже используется");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Имя пользователя уже занято");
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

        return new AuthResponse(token, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }

    /**
     * Выполняет аутентификацию пользователя в системе.
     *
     * <p><b>Процесс аутентификации:</b>
     * <ol>
     *   <li>Поиск пользователя по email в базе данных</li>
     *   <li>Проверка соответствия введенного пароля хэшированному паролю</li>
     *   <li>Обновление онлайн-статуса и времени последней активности</li>
     *   <li>Генерация JWT-токена для аутентифицированного пользователя</li>
     * </ol>
     * </p>
     *
     * @param request DTO с данными для входа (email и password)
     * @return AuthResponse с JWT-токеном и данными пользователя
     * @throws RuntimeException если пользователь не найден или пароль неверный
     * @see AuthRequest
     * @see AuthResponse
     */
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Неверный пароль");
        }

        user.setIsOnline(true);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }
}