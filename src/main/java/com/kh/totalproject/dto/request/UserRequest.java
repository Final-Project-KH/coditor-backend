/* 회원가입, 아이디, 비밀번호 찾기시 필요한 데이터 전송 Dto
*  otp 추가 되었음 */
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
public class UserRequest {
    private Long id;
    private String userId;
    private String email;
    private String password;
    private String nickname;
    private String profileUrl;
    private Integer otp;

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
