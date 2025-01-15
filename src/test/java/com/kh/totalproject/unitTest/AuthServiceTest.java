/* 설계 단계에서 비즈니스 로직이 작동 하는지 확인하는 유닛 테스트 */
package com.kh.totalproject.unitTest;

import com.kh.totalproject.constant.Role;
import com.kh.totalproject.entity.User;
import com.kh.totalproject.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
public class AuthServiceTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("이메일 여부 확인")
    public void EmailValidation() {
        String email = "testemail@gmail.com";
        Optional <User> findUser = userRepository.findByEmail(email);
        assertTrue(findUser.isPresent(), "존재하는 이메일이 아닙니다.");
    }

    @Test
    @DisplayName("회원가입 테스트")
    public void joinUserWithAllTest() {
        // 인스턴스
        User user = new User();
        user.setUserId("testId");
        user.setPassword(passwordEncoder.encode("testPw"));
        user.setEmail("testemail@gmail.com");
        user.setNickname("cook");
        user.setProfileUrl(null);
        user.setRole(Role.USER);

        User saveUser = userRepository.save(user);
        
        boolean isSaved = saveUser.getUserId().equals(user.getUserId());
        
        assertTrue(isSaved);
    }

    @Test
    @DisplayName("이메일을 통해 아이디 찾기")
    public void findIdByEmail() {
        String inputEmail = "testemail@gmail.com";
        Optional<User> isExisted = userRepository.findByEmail(inputEmail);

        assertTrue(isExisted.isPresent());
        assertEquals(inputEmail, isExisted.get().getEmail());
    }

    @Test
    @DisplayName("비밀번호 재설정")
    public void resetPw() {
        String email = "testmail@gmail.com";
        String newPw = "test1";

        Optional<User> findUserByEmail = userRepository.findByEmail(email);
        assertTrue(findUserByEmail.isPresent());    // 유저가 존재할시
        User userToUpdate = findUserByEmail.get();  // 존재하는 유저를 userToUpdate 에 할당
        userToUpdate.setPassword(passwordEncoder.encode(newPw));    // 새 비밀번호 암호화


        User updateUser = userRepository.save(userToUpdate);
        assertTrue(passwordEncoder.matches(newPw, updateUser.getPassword()), "비밀번호가 일치하지 않습니다.");
    }
}
