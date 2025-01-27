package com.kh.totalproject.util;
import com.kh.totalproject.exception.UnauthenticatedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtil {
//    /**
//     * 현재 인증된 사용자의 ID를 반환합니다.
//     * 인증되지 않았거나 사용자 정보를 찾을 수 없는 경우 예외를 던집니다.
//     *
//     * @return 인증된 사용자의 ID
//     * @throws UnauthenticatedException 인증 정보가 없거나 CustomUserDetails 형식이 아닐 때
//     */
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
}