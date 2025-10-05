package com.cuppa.CuppaApp.repository;

import com.cuppa.CuppaApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
}
