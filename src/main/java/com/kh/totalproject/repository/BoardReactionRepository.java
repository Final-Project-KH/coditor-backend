package com.kh.totalproject.repository;

import com.kh.totalproject.entity.Board;
import com.kh.totalproject.entity.BoardReaction;
import com.kh.totalproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardReactionRepository extends JpaRepository<BoardReaction, Long> {
    Optional<BoardReaction> findByBoardAndUser(Board board, User user);
}
