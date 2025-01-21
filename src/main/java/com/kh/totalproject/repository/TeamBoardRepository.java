package com.kh.totalproject.repository;

import com.kh.totalproject.entity.TeamBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamBoardRepository extends JpaRepository<TeamBoard, Long> {
    Page<TeamBoard> findAll(Pageable pageable);
    Optional<TeamBoard> findById(Long id);
}
