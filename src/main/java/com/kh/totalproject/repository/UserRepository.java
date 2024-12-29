package com.kh.totalproject.repository;

import com.kh.totalproject.dto.request.LoginRequest;
import com.kh.totalproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndPassword(String email, String password);
    boolean existsByUserIdAndPassword(String userId, String password);
    Optional<Long> findIdByEmailAndPassword(String email, String password);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId);
    Optional<User> findByEmailAndPassword(String email, String password);
}
