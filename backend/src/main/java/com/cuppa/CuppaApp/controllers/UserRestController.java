package com.cuppa.CuppaApp.controllers;

import com.cuppa.CuppaApp.entity.User;
import com.cuppa.CuppaApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cuppa.CuppaApp.dto.UserDto;

import java.util.stream.Collectors;

import java.util.List;
import java.util.Optional;

/**
 * REST контроллер для управления пользователями в приложении Cuppa
 *
 *
 * <p>Контроллер обеспечивает:
 * <ul>
 *   <li>Получение списка всех пользователей</li>
 *   <li>Поиск пользователей по различным критериям (ID, username)</li>
 *   <li>Создание новых пользователей с валидацией уникальности</li>
 *   <li>Обновление данных существующих пользователей</li>
 *   <li>Управление онлайн-статусом пользователей</li>
 *   <li>Получение статистической информации</li>
 * </ul>
 *
 * <p><b>ВНИМАНИЕ:</b> Все операции выполняются с реальной базой данных PostgreSQL.
 * Рекомендуется добавить аутентификацию и авторизацию для защиты endpoints в продакшн-среде.
 *
 * @author Petr Panteev
 * @author Walerya Pleskova
 * @version 1.2
 * @since 05.10.2025
 * upd 14.10.2025
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserRestController {

    /**
     * Репозиторий для работы с данными пользователей в базе данных
     *
     * <p>Обеспечивает абстракцию над операциями CRUD и кастомными запросами
     * к таблице users. Spring автоматически внедряет реализацию этого интерфейса.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Получить полный список всех зарегистрированных пользователей
     *
     * <p>Endpoint: GET /api/users
     *
     * <p>Возвращает массив JSON объектов, содержащих полную информацию
     * о каждом пользователе системы. Если пользователи отсутствуют,
     * возвращает статус 204 No Content.
     *
     * @return ResponseEntity со списком пользователей и соответствующим HTTP статусом:
     * <ul>
     *   <li>200 OK - пользователи найдены и возвращены</li>
     *   <li>204 No Content - пользователи отсутствуют в системе</li>
     *   <li>500 Internal Server Error - произошла ошибка сервера</li>
     * </ul>
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();

            if (users.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            List<UserDto> userDtos = users.stream()
                    .map(UserDto::fromEntity)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(userDtos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Получить пользователя по уникальному идентификатору (ID)
     *
     * <p>Endpoint: GET /api/users/{id}
     *
     * <p>Выполняет поиск пользователя в базе данных по первичному ключу.
     * ID пользователя передается как path variable в URL.
     *
     * @param id уникальный идентификатор пользователя (целое число)
     * @return ResponseEntity с данными пользователя и соответствующим HTTP статусом:
     * <ul>
     *   <li>200 OK - пользователь найден и возвращен</li>
     *   <li>404 Not Found - пользователь с указанным ID не существует</li>
     * </ul>
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Integer id) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            UserDto userDto = UserDto.fromEntity(userData.get());
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Получить пользователя по имени пользователя (username)
     *
     * <p>Endpoint: GET /api/users/username/{username}
     *
     * <p>Выполняет эффективный поиск пользователя по уникальному имени пользователя
     * с использованием оптимизированного запроса к базе данных.
     *
     * <p><b>Оптимизация:</b> Использует специализированный метод репозитория
     * вместо фильтрации на стороне приложения, что значительно повышает
     * производительность при большом количестве пользователей.
     *
     * @param username уникальное имя пользователя для поиска
     * @return ResponseEntity с данными пользователя и соответствующим HTTP статусом:
     * <ul>
     *   <li>200 OK - пользователь найден и возвращен</li>
     *   <li>404 Not Found - пользователь с указанным username не существует</li>
     * </ul>
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable("username") String username) {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {
            UserDto userDto = UserDto.fromEntity(user.get());
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Создать нового пользователя в системе
     *
     * <p>Endpoint: POST /api/users
     *
     * <p>Принимает JSON объект с данными нового пользователя и сохраняет его
     * в базе данных. Перед сохранением выполняет проверку уникальности
     * имени пользователя и email адреса.
     *
     * <p><b>Валидация:</b> Автоматически проверяет отсутствие дубликатов
     * username и email с использованием оптимизированных запросов к БД.
     *
     * @param user объект пользователя с данными для создания (передается в теле запроса)
     * @return ResponseEntity с созданным пользователем и соответствующим HTTP статусом:
     * <ul>
     *   <li>201 Created - пользователь успешно создан</li>
     *   <li>409 Conflict - пользователь с таким username или email уже существует</li>
     *   <li>500 Internal Server Error - произошла ошибка при создании пользователя</li>
     * </ul>
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody User user) {
        try {
            boolean usernameExists = userRepository.existsByUsername(user.getUsername());
            boolean emailExists = userRepository.existsByEmail(user.getEmail());

            if (usernameExists) {
                return new ResponseEntity<>(null, HttpStatus.CONFLICT);
            }

            if (emailExists) {
                return new ResponseEntity<>(null, HttpStatus.CONFLICT);
            }

            User newUser = userRepository.save(user);
            UserDto userDto = UserDto.fromEntity(newUser);
            return new ResponseEntity<>(userDto, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обновить данные существующего пользователя
     *
     * <p>Endpoint: PUT /api/users/{id}
     *
     * <p>Обновляет информацию о пользователе с указанным ID. Поддерживает
     * частичное обновление - изменяются только те поля, которые переданы
     * в запросе (не-null значения).
     *
     * <p><b>Особенности:</b> Метод реализует стратегию частичного обновления,
     * что позволяет клиентам отправлять только изменяемые поля без необходимости
     * передачи полного объекта.
     *
     * @param id   уникальный идентификатор пользователя для обновления
     * @param user объект с новыми данными пользователя (передается в теле запроса)
     * @return ResponseEntity с обновленным пользователем и соответствующим HTTP статусом:
     * <ul>
     *   <li>200 OK - пользователь успешно обновлен</li>
     *   <li>404 Not Found - пользователь с указанным ID не существует</li>
     * </ul>
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("id") Integer id, @RequestBody User user) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            User existingUser = userData.get();

            // Частичное обновление: изменяем только переданные поля
            if (user.getUsername() != null) {
                existingUser.setUsername(user.getUsername());
            }
            if (user.getEmail() != null) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getFirstName() != null) {
                existingUser.setFirstName(user.getFirstName());
            }
            if (user.getLastName() != null) {
                existingUser.setLastName(user.getLastName());
            }
            if (user.getAvatarUrl() != null) {
                existingUser.setAvatarUrl(user.getAvatarUrl());
            }
            if (user.getPhone() != null) {
                existingUser.setPhone(user.getPhone());
            }
            if (user.getIsOnline() != null) {
                existingUser.setIsOnline(user.getIsOnline());
            }
            if (user.getLastSeen() != null) {
                existingUser.setLastSeen(user.getLastSeen());
            }
            User updatedUser = userRepository.save(existingUser);
            UserDto userDto = UserDto.fromEntity(updatedUser);
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Удалить пользователя из системы
     *
     * <p>Endpoint: DELETE /api/users/{id}
     *
     * <p>Полностью удаляет пользователя с указанным ID из базы данных.
     * Операция необратима - все данные пользователя будут безвозвратно удалены.
     *
     * @param id уникальный идентификатор пользователя для удаления
     * @return ResponseEntity с соответствующим HTTP статусом:
     * <ul>
     *   <li>204 No Content - пользователь успешно удален</li>
     *   <li>404 Not Found - пользователь с указанным ID не существует</li>
     *   <li>500 Internal Server Error - произошла ошибка при удалении</li>
     * </ul>
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
     * Получить общее количество зарегистрированных пользователей
     *
     * <p>Endpoint: GET /api/users/count
     *
     * <p>Возвращает общее число пользователей в системе. Может использоваться
     * для административной статистики или отображения в интерфейсе.
     *
     * @return ResponseEntity с количеством пользователей и соответствующим HTTP статусом:
     * <ul>
     *   <li>200 OK - количество успешно получено</li>
     *   <li>500 Internal Server Error - произошла ошибка при подсчете</li>
     * </ul>
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