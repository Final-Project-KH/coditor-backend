package com.kh.totalproject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class  LoginRequest {
    private String userId;
    private String password;


    public UsernamePasswordAuthenticationToken toAuthentication(){
        return new UsernamePasswordAuthenticationToken(userId, password);
    }
}
