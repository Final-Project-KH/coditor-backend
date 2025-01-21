package com.kh.totalproject.repository;

import com.kh.totalproject.entity.CodingBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodingBoardRepository extends JpaRepository<CodingBoard, Long> {
    Page<CodingBoard> findAll(Pageable pageable);
    Optional<CodingBoard> findById(Long id);
}
