package com.kh.totalproject.repository;


import com.kh.totalproject.entity.CourseBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseBoardRepository extends JpaRepository<CourseBoard, Long>, JpaSpecificationExecutor<CourseBoard> {
    @Query("""
    SELECT b, COUNT(c) 
    FROM CourseBoard b
    LEFT JOIN b.comments c
    WHERE (:status IS NULL)
    GROUP BY b
    ORDER BY
        CASE WHEN :sortBy = 'commentCnt' THEN COUNT(c) END DESC,
        CASE WHEN :sortBy = 'createdAt' THEN b.createdAt END DESC
""")
    Page<Object[]> findAllWithCommentCntForCourse(@Param("sortBy") String sortBy,
                                                  Specification<CourseBoard> spec, Pageable pageable);
}


