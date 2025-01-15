/* Unit 테스트는 단위테스트로써 실질적으로 DB에 값이 담기는 것을 확이 하기보다
*  각 Service 계층의 메서드가 잘 작동하는지 성공 / 실패 여부를 확인 하기 위한 테스트 */
package com.kh.totalproject.unitTest;

import com.kh.totalproject.dto.MailBody;
import com.kh.totalproject.dto.request.UserRequest;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.repository.UserRepository;
import com.kh.totalproject.service.AuthService;
import com.kh.totalproject.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// 진행중.......
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock   // 실제 DB 를 추가 하기보다 Mocking DB 를 임시 주입
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks    // 실제 테스트 대상
    private AuthService authService;

    @Test
    void testJoinUser_Success() {
        UserRequest userRequest = new UserRequest();
        userRequest.setUserId("testId");
        userRequest.setPassword("Test1234@");
        userRequest.setEmail("testEmail@gmail.com");
        userRequest.setNickname("테스터1");
        userRequest.setOtp(123456);

        Mockito.when(userRepository.existsByEmail("testEmail@gmail.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("Test1234@")).thenReturn("asdSN@!kdm@MF@LFI#Osd");

        doNothing().when(emailService).sendVerificationEmail(any(MailBody.class));

        AuthService spyService = spy(authService);
        doReturn(true).when(spyService).validateOtpForJoin(123456, "testEmail@gmail.com");

        boolean result = spyService.signUp(userRequest);

        assertTrue(result);
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(any(MailBody.class));
    }
}
