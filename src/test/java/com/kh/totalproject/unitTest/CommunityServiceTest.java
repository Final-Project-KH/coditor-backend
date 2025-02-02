///* 설계 단계에서 서비스 계층 구현 전에 커뮤니티 게시글 조회 및 비즈니스 로직 테스트 */
//package com.kh.totalproject.unitTest;
//
//import com.kh.totalproject.constant.Role;
//import com.kh.totalproject.entity.CodingBoard;
//import com.kh.totalproject.entity.User;
//import com.kh.totalproject.repository.CodingBoardRepository;
//import com.kh.totalproject.repository.UserRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.TestPropertySource;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Slf4j
//@TestPropertySource(locations = "classpath:application-test.properties")
//@SpringBootTest
//public class CommunityServiceTest {
//    @Autowired
//    CodingBoardRepository codingBoardRepository;
//    @Autowired
//    UserRepository userRepository;
//
//    @Test
//    @DisplayName("게시판 선택 옵션 (게시판 생성시 먼저 선택")
//    public void BoardSelection() {
//        switch ("CODING") {
//            case "CODING" : {
//                System.out.println("코딩 게시판 선택 하셨습니다.");
//                break;
//            }
//            case "COURSE" : {
//                System.out.println("진로 게시판 선택 하셨습니다.");
//                break;
//            }
//            case "STUDY" : {
//                System.out.println("스터디 게시판 선택 하셨습니다.");
//                break;
//            }
//            case "TEAM" : {
//                System.out.println("팀 게시판 선택 하셨습니다.");
//                break;
//            }
//            default: {
//                System.out.println("잘못된 게시판 선택 입니다.");
//            }
//        }
//    }
//
//    @Test
//    @DisplayName("코딩질문 게시판 글 작성하기")
//    public void createCodingPost() {
//        CodingBoard codingBoard = new CodingBoard();
//        User user = new User();
//        user.setNickname("국누나짱짱맨11");
//        user.setUserKey(3L);
//        user.setUserId("testtest1123");
//        user.setEmail("testemail12wqewqe324@gmail.com");
//        user.setPassword("testasdfsdf124@!");
//        user.setRole(Role.USER);
//        user.setProfileUrl("string");
//        user = userRepository.save(user);
//        codingBoard.setTitle("테스트 제목입니다.");
//        codingBoard.setContent("테스트 내용입니다.");
//        codingBoard.setImgUrl("www.firebase.blahblah.com");
//        codingBoard.setSolution(Solution.UNSOLVED);
//        codingBoard.setCreatedAt(LocalDateTime.now());
//        codingBoard.setCommentCnt(0);
//        codingBoardRepository.save(codingBoard);
//
//        assertEquals("테스트 제목입니다.", codingBoard.getTitle(), "제목이 올바르지 않습니다.");
//        assertEquals("테스트 내용입니다.", codingBoard.getContent(), "내용이 올바르지 않습니다.");
//        assertEquals("www.firebase.blahblah.com", codingBoard.getImgUrl(), "이미지 URL 이 올바르지 않습니다.");
//        assertEquals(Solution.UNSOLVED, codingBoard.getSolution(), "문제 해결 상태가 올바르지 않습니다.");
//        assertNotNull(user.getUserKey(), "User 정상적으로 저장되지 않았습니다.");
//    }
//
//    @Test
//    @DisplayName("코딩질문 게시글 전체 조회")
//    public void listAllCodingPost() {
//        List<CodingBoard> codingBoards = codingBoardRepository.findAll();
//        for (CodingBoard codingBoard : codingBoards) {
//            log.info("전체글 : {} ", codingBoard);
//        }
//    }
//
//    @Test
//    @DisplayName("코딩질문 게시글 삭제")
//    public void deleteCodingPost() {
//        Long postId = 1L;
//        assertTrue(codingBoardRepository.findById(postId).isPresent(), "게시글이 존재해야 합니다.");
//        codingBoardRepository.deleteById(postId);
//        assertFalse(codingBoardRepository.findById(postId).isPresent(), "게시글이 삭제되지 않았습니다.");
//    }
//}
