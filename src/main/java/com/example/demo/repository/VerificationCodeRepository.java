package com.example.demo.repository;

import com.example.demo.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByEmail(String email);

    Optional<VerificationCode> findByEmailAndCodeAndCreatedAtAfter(
            String email,
            String code,
            LocalDateTime expirationTime
    );

    @Transactional
    @Modifying
    @Query("DELETE FROM VerificationCode vc WHERE vc.email = :email")
    void deleteByEmail(@Param("email") String email);
}
