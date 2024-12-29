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
public class SaveUserRequest {
    private String userId;
    private String email;
    private String password;
    private String nickname;

    public User toEntity(PasswordEncoder passwordEncoder){
        return User.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .email(email)
                .nickname(nickname)
                .build();
    }
}
