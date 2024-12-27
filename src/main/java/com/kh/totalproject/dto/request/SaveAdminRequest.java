package com.kh.totalproject.dto.request;


import com.kh.totalproject.constant.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveAdminRequest {
    private String email;
    private String password;
    private String nickname;
    private UserStatus userStatus;
}
