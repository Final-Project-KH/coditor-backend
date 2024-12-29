package com.kh.totalproject.dto.request;


import com.kh.totalproject.constant.UserStatus;
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
public class SaveAdminRequest {
    private String userId;
    private String email;
    private String password;
    private String nickname;
    private UserStatus userStatus;

    public User toEntity(PasswordEncoder passwordEncoder){
        return User.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .email(email)
                .nickname(nickname)
                .userStatus(userStatus)
                .build();
    }
}
