package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 실제 이름 포함?
// 프로필 이미지 추가?
// Refresh Token 데이터베이스에 저장 필요?
@Entity
@Table(name="user", uniqueConstraints = {
        @UniqueConstraint(name = "unique_email", columnNames = "email"),})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_key")
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    @Size(min = 5, max = 30, message = "아이디는 5자 이상, 30자 이하 (영어 기준)")
    private String userId;

    @Column(nullable = false, unique = true, length = 30)
    @Size(min = 4, max = 50, message = "닉네임은 1자 이상, 16자 이하(한글 기준)")
    private String nickname;

    @Column(nullable = false, length = 50)
    @Size(min = 5, max = 50, message = "이메일은 5자 이상, 50자 이하(영어 기준)")
    private String email;

    @Column(nullable = true, length = 100)
    private String profileUrl;
    
    // 암호화 하기 때문에 max 값 255로 설정
    @Column(nullable = false)
    @Size(min = 8, max = 255, message = "비밀번호는 8자 이상, 50자 이하")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @PrePersist
    private void prePersist(){
        if (role == null){
            this.role = Role.USER;
        }
        //else this.role = Role.ADMIN;      // null 이 아닐때도 기본값이 ADMIN이라 주석처리
    }

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder // NoArgsConstructor 가 있어야함
    public User(String userId, String email, String nickname, String password, Role role){
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }
}
