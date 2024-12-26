package com.kh.totalproject.service;


import com.kh.totalproject.dto.request.SaveUserRequest;
import com.kh.totalproject.dto.response.UserInfoResponse;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    
    // 회원 가입 여부
    public boolean isUser(String email){
        return userRepository.existsByEmail(email);
        // 회원 가입 진행 중 email 확인 (선행 메소드)
    }
    
    // 회원 가입 (반환 타입 - UserInfoResponse)
    public UserInfoResponse saveUser(SaveUserRequest requestDto){
        User user = new User();
        user.setPassword(requestDto.getPassword());
        user.setEmail(requestDto.getEmail());
        user.setNickname(requestDto.getNickname());
        userRepository.save(user);

        return convertToUserInfoResponse(user);
    }

    // 회원 가입 (반환 타입 - boolean)
    public boolean signUp(SaveUserRequest requestDto){
        if(isUser(requestDto.getEmail())){
            User user = convertDtoToEntity(requestDto);
            userRepository.save(user);
            return true;
        }
        else return false;
    }

    public UserInfoResponse update(Long id, SaveUserRequest requestDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 회원을 찾을 수 없습니다. 회원 식별자 id 값 : " + id));
        if (requestDto.getPassword() != null) {
            user.setPassword(requestDto.getPassword());
        }

        user.setEmail(requestDto.getEmail());
        user.setNickname(requestDto.getNickname());

        // Flush 작업 수행 : 영속성 컨텍스트에 쌓인 변경 내용을 데이터베이스에 반영(시간 정보가 업데이트 되도록 SQL 즉시 실행, COMMIT은 수행되지 않음)
        // COMMIT은 메서드 종료 시 수행됨
        userRepository.saveAndFlush(user);
        return convertToUserInfoResponse(user);
    }

    public UserInfoResponse delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 회원을 찾을 수 없습니다. 회원 식별자 id 값 : " + id));
        userRepository.delete(user);
        return convertToUserInfoResponse(user);
    }

    public List<UserInfoResponse> getUserInfoAll() {
        return userRepository.findAll().stream()
                .map(this::convertToUserInfoResponse)
                .collect(Collectors.toList());
    }

    public UserInfoResponse getUserInfo(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 회원을 찾을 수 없습니다. 회원 식별자 id 값 : " + id));
        return convertToUserInfoResponse(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 회원을 찾을 수 없습니다. 회원 식별자 id 값 : " + id));
    }

    private UserInfoResponse convertToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userStatus(user.getUserStatus())
                .registeredAt(user.getRegisteredAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private User convertDtoToEntity(SaveUserRequest requestDto) {
        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setNickname(requestDto.getNickname());
        user.setPassword(requestDto.getPassword());
        return user;
    }
}
