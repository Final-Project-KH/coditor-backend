package com.kh.totalproject.repository;

import com.kh.totalproject.entity.CodingBoard;
import com.kh.totalproject.entity.CourseBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseBoardRepository extends JpaRepository<CourseBoard, Long> {
    Page<CourseBoard> findAll(Pageable pageable);
    Optional<CourseBoard> findById(Long id);
}
