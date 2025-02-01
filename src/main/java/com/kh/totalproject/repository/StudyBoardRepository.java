package com.kh.totalproject.repository;

import com.kh.totalproject.constant.Status;
import com.kh.totalproject.entity.StudyBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyBoardRepository extends JpaRepository<StudyBoard, Long>, JpaSpecificationExecutor<StudyBoard> {
    @Query("""
    SELECT b, COUNT(c) AS commentCnt
    FROM StudyBoard b
    LEFT JOIN b.comments c
    WHERE (:status IS NULL OR b.status = :status)
    GROUP BY b
    ORDER BY
        CASE WHEN :sortBy = 'commentCnt' THEN COUNT(c) END DESC,
        CASE WHEN :sortBy = 'createdAt' THEN b.createdAt END DESC
""")
    Page<Object[]> findAllWithCommentCntForStudy(@Param("status") Status status,
                                                 @Param("sortBy") String sortBy,
                                                 Specification<StudyBoard> spec, Pageable pageable);
}
