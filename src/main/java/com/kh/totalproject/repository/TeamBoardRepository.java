package com.kh.totalproject.repository;

import com.kh.totalproject.constant.Status;
import com.kh.totalproject.entity.TeamBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamBoardRepository extends JpaRepository<TeamBoard, Long>, JpaSpecificationExecutor<TeamBoard> {
    @Query("""
    SELECT b, COUNT(c) AS commentCnt
    FROM TeamBoard b
    LEFT JOIN b.comments c
    WHERE (:status IS NULL OR b.status = :status)
    GROUP BY b
    ORDER BY
        CASE WHEN :sortBy = 'commentCnt' THEN COUNT(c) END DESC,
        CASE WHEN :sortBy = 'createdAt' THEN b.createdAt END DESC
""")
    Page<Object[]> findAllWithCommentCntForTeam(@Param("status") Status status,
                                                @Param("sortBy") String sortBy,
                                                Specification<TeamBoard> spec, Pageable pageable);
}

