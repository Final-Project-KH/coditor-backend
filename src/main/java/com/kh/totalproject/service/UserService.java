package com.kh.totalproject.service;


import com.kh.totalproject.dto.request.SaveAdminRequest;
import com.kh.totalproject.dto.request.SaveUserRequest;
import com.kh.totalproject.dto.response.UserInfoResponse;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    // 회원정보 수정 (비밀번호 변경 시에 데이터베이스에 암호화되어서 들어가는지 확인 필요)
    public UserInfoResponse update(Long id, SaveUserRequest requestDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 회원을 찾을 수 없습니다. 회원 식별자 id 값 : " + id));
        if (requestDto.getPassword() != null) { // 이 부분 기존 비밀번호와 일치하지 않으면으로 변경? (암호화 방식 비교)
            user.setPassword(requestDto.getPassword());
        }

        user.setEmail(requestDto.getEmail());
        user.setNickname(requestDto.getNickname());

        // Flush 작업 수행 : 영속성 컨텍스트에 쌓인 변경 내용을 데이터베이스에 반영(시간 정보가 업데이트 되도록 SQL 즉시 실행, COMMIT은 수행되지 않음)
        // COMMIT은 메서드 종료 시 수행됨
        userRepository.saveAndFlush(user);
        return convertToUserInfoResponse(user);
    }
    
    // 회원정보 삭제 (삭제하시겠습니까? (삭제 전에 필요))
    public UserInfoResponse delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 회원을 찾을 수 없습니다. 회원 식별자 id 값 : " + id));
        userRepository.delete(user);
        return convertToUserInfoResponse(user);
    }

    // 전체 유저 정보 가져오기 (Admin 권한으로 변경하거나 삭제 필요)
    public List<UserInfoResponse> getUserInfoAll() {
        return userRepository.findAll().stream()
                .map(this::convertToUserInfoResponse)
                .collect(Collectors.toList());
    }
    
    // 단일 유저 정보 가져오기
    public UserInfoResponse getUserInfo(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 회원을 찾을 수 없습니다. 회원 식별자 id 값 : " + id));
        return convertToUserNicknameEmailResponse(user);
    }


    private UserInfoResponse convertToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userStatus(user.getUserStatus())
                .registeredAt(user.getRegisteredAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UserInfoResponse convertToUserNicknameEmailResponse(User user) {
        return UserInfoResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    private User convertDtoToEntity(SaveUserRequest requestDto) {
        User user = new User();
        user.setUserId(requestDto.getUserId());
        user.setEmail(requestDto.getEmail());
        user.setNickname(requestDto.getNickname());
        user.setPassword(requestDto.getPassword());
        return user;
    }
}
