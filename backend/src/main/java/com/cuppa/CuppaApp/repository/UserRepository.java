package com.cuppa.CuppaApp.repository;

import com.cuppa.CuppaApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Найти пользователя по username
     * Spring Data JPA автоматически создаст реализацию этого метода
     */
    Optional<User> findByUsername(String username);

    /**
     * Найти пользователя по email
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверить существование пользователя по username
     */
    boolean existsByUsername(String username);

    /**
     * Проверить существование пользователя по email
     */
    boolean existsByEmail(String email);
}