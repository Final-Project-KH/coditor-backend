package com.kh.totalproject.util;
import com.kh.totalproject.constant.Role;
import com.kh.totalproject.exception.BadRequestException;
import com.kh.totalproject.exception.ForbiddenException;
import com.kh.totalproject.exception.UnauthenticatedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SecurityUtil {
    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     * 인증되지 않았거나 사용자 정보를 찾을 수 없는 경우 예외를 던집니다.
     *
     * @return 인증된 사용자의 ID
     * @throws UnauthenticatedException 인증 정보가 없거나 CustomUserDetails 형식이 아닐 때
     */
    public static Long getCurrentUserIdOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.info("[SecurityUtil.getCurrentUserIdOrThrow] 사용자가 인증되지 않았습니다.");
            throw new UnauthenticatedException();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            log.info("[SecurityUtil.getCurrentUserIdOrThrow] 인증된 사용자 정보를 찾을 수 없습니다.");
            throw new UnauthenticatedException();
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        Long userId = userDetails.getUserKey();
        if (userId == null) {
            log.info("[SecurityUtil.getCurrentUserIdOrThrow] 사용자 ID를 찾을 수 없습니다.");
            throw new UnauthenticatedException();
        }

        return userId;
    }

    // Context Header 에서 들어온 Access Token 으로 유저 Role 추출 하는 메서드 반환 값은 True/False 이므로
    // AdminController 에서 !isAdminUser 로 반환값 False 일때 BadRequest 반환 값 처리해야함
    public static boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("로그인이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new BadRequestException("잘못된 사용자 정보입니다.");
        }

        boolean isAdmin = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ADMIN"));
        if (!isAdmin) {
            throw new BadRequestException("관리자만 접근 가능합니다.");
        }

        return true;
    }

}