package com.kh.totalproject.dto.request;


import com.kh.totalproject.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveUserRequest { // 일반 User 회원가입 Request Dto
    private String userId;
    private String email;
    private String password;
    private String nickname;

    // User 저장 시 password 는 암호화하여 데이터베이스 저장
    public User toEntity(PasswordEncoder passwordEncoder){
        return User.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .email(email)
                .nickname(nickname)
                .build();
    }
}
