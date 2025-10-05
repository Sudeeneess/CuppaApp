package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST контроллер для работы с пользователями
 *
 * <p>Предоставляет REST API для операций CRUD с пользователями мессенджера.
 * Работает с существующими данными в базе данных.
 *
 * @author Petr Panteev
 * @version 1.0
 * @since 05.10.2025
 */
@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Получить всех пользователей
     *
     * @return список всех пользователей
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();

            if (users.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получить пользователя по ID
     *
     * @param id ID пользователя
     * @return пользователь или 404 если не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Integer id) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            return new ResponseEntity<>(userData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Получить пользователя по username
     *
     * @param username имя пользователя
     * @return пользователь или 404 если не найден
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable("username") String username) {
        // Если у вас есть метод в репозитории для поиска по username
        // User user = userRepository.findByUsername(username);

        // Временное решение - ищем по всем пользователям
        Optional<User> user = userRepository.findAll().stream()
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();

        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Создать нового пользователя
     *
     * @param user данные пользователя
     * @return созданный пользователь
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            // Проверяем, существует ли пользователь с таким username или email
            boolean usernameExists = userRepository.findAll().stream()
                    .anyMatch(u -> user.getUsername().equals(u.getUsername()));
            boolean emailExists = userRepository.findAll().stream()
                    .anyMatch(u -> user.getEmail().equals(u.getEmail()));

            if (usernameExists) {
                return new ResponseEntity<>(null, HttpStatus.CONFLICT); // 409 Conflict
            }

            if (emailExists) {
                return new ResponseEntity<>(null, HttpStatus.CONFLICT); // 409 Conflict
            }

            User newUser = userRepository.save(user);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обновить данные пользователя
     *
     * @param id ID пользователя
     * @param user новые данные пользователя
     * @return обновленный пользователь
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Integer id, @RequestBody User user) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            User existingUser = userData.get();

            // Обновляем только те поля, которые пришли в запросе
            if (user.getUsername() != null) {
                existingUser.setUsername(user.getUsername());
            }
            if (user.getEmail() != null) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getFirst_name() != null) {
                existingUser.setFirst_name(user.getFirst_name());
            }
            if (user.getLast_name() != null) {
                existingUser.setLast_name(user.getLast_name());
            }
            if (user.getAvatar_url() != null) {
                existingUser.setAvatar_url(user.getAvatar_url());
            }
            if (user.getPhone() != null) {
                existingUser.setPhone(user.getPhone());
            }
            if (user.getIs_online() != null) {
                existingUser.setIs_online(user.getIs_online());
            }
            if (user.getLast_seen() != null) {
                existingUser.setLast_seen(user.getLast_seen());
            }

            return new ResponseEntity<>(userRepository.save(existingUser), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Удалить пользователя
     *
     * @param id ID пользователя
     * @return статус операции
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable("id") Integer id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Установить статус онлайн/офлайн для пользователя
     *
     * @param id ID пользователя
     * @param isOnline статус онлайн
     * @return обновленный пользователь
     */
    @PatchMapping("/{id}/online")
    public ResponseEntity<User> setOnlineStatus(
            @PathVariable("id") Integer id,
            @RequestParam Boolean isOnline) {

        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            User user = userData.get();
            user.setIs_online((isOnline));
            return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Получить количество пользователей
     *
     * @return количество пользователей
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getUsersCount() {
        try {
            long count = userRepository.count();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}