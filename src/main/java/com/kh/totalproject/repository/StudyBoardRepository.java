package com.kh.totalproject.repository;

import com.kh.totalproject.entity.CodingBoard;
import com.kh.totalproject.entity.StudyBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyBoardRepository extends JpaRepository<StudyBoard, Long> {
    Page<StudyBoard> findAll(Pageable pageable);
    Optional<StudyBoard> findById(Long id);
}
