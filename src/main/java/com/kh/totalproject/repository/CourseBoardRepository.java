package com.kh.totalproject.repository;


import com.kh.totalproject.entity.CodingBoard;
import com.kh.totalproject.entity.CourseBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseBoardRepository extends JpaRepository<CourseBoard, Long>, JpaSpecificationExecutor<CourseBoard> {
    @Query("SELECT b, COUNT(c) as commentCnt FROM CourseBoard b LEFT JOIN b.comments c GROUP BY b")
    Page<Object[]> findAllWithCommentCnt(Specification<CourseBoard> spec, Pageable pageable);
}
