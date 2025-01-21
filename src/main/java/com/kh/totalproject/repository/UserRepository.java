package com.kh.totalproject.repository;

import com.kh.totalproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    Optional<User> findByUserIdAndEmail(String userId, String email);
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);
    boolean existsByNickname(String nickname);
}
