package com.example.demo.repository;

import com.example.demo.entity.Login;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login, Long> {
    Optional<Login> findByToken(String token);
    Optional<Login> findByUser(User user);
}
