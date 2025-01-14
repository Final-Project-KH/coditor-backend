package com.kh.totalproject.service;

import com.kh.totalproject.dto.request.UserRequest;
import com.kh.totalproject.dto.response.BoardResponse;
import com.kh.totalproject.dto.response.UserResponse;
import com.kh.totalproject.entity.Board;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.repository.BoardRepository;
import com.kh.totalproject.repository.UserRepository;
import com.kh.totalproject.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 내 정보 보기
    public UserResponse listMyProfile(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", ""); // Bearer 제거
        log.info("Bearer 제거한 토큰 : {} ", token);
        jwtUtil.getAuthentication(token); // 인증 정보 생성
        log.info("인증정보 생성 : {} ", jwtUtil.getAuthentication(token));
        Long id = jwtUtil.extractUserId(token); // 토큰에서 ID 추출
        log.info("토큰에서 ID 추출 : {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        return UserResponse.ofAll(user);
    }

    // 내 정보 수정
    public boolean modifyMember(UserRequest userRequest) {
        try {
            User user = userRepository.findById(userRequest.getId())
                    .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
            user.setNickname(userRequest.getNickname());
            user.setProfileUrl(userRequest.getProfileUrl());
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("회원 정보 수정 실패 하였습니다 : {}", e.getMessage());
            return false;
        }
    }

    // 내 비밀번호 변경
    public boolean changePw(Long id, String inputPw, String newPw) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        if (!passwordEncoder.matches(inputPw, user.getPassword())) {
            log.error("입력된 비밀번호 '{}'와 기존 비밀번호 '{}'가 일치하지 않습니다.", inputPw, user.getPassword());
            return false;
        } else {
            String newHashedPw = passwordEncoder.encode(newPw);
            user.setPassword(newHashedPw);
            userRepository.save(user);
            return true;
        }
    }

    // 내 작성글 보기
    public List<BoardResponse> myPostList(int size) {
        Pageable pageable = PageRequest.ofSize(size);
        List<Board> boards = boardRepository.findAll(pageable).getContent();

        return boards.stream()
                .map(BoardResponse::ofMyPost)
                .collect(Collectors.toList());
    }
}