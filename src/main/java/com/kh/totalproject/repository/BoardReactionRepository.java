package com.kh.totalproject.repository;

import com.kh.totalproject.entity.Board;
import com.kh.totalproject.entity.BoardReaction;
import com.kh.totalproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardReactionRepository extends JpaRepository<BoardReaction, Long> {
    Optional<BoardReaction> findByBoardAndUser(Board board, User user);

    @Query("SELECT COUNT(br) FROM BoardReaction br WHERE br.board.id = :boardId AND br.reaction = 'LIKE'")
    int countLikesByBoardId(@Param("boardId") Long boardId);

    @Query("SELECT COUNT(br) FROM BoardReaction br WHERE br.board.id = :boardId AND br.reaction = 'DISLIKE'")
    int countDislikesByBoardId(@Param("boardId") Long boardId);
}
