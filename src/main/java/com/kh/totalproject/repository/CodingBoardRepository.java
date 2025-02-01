package com.kh.totalproject.repository;

import com.kh.totalproject.entity.CodingBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CodingBoardRepository extends JpaRepository<CodingBoard, Long>, JpaSpecificationExecutor<CodingBoard> {
    @Query("SELECT b, COUNT(c) as commentCnt FROM CodingBoard b LEFT JOIN b.comments c GROUP BY b")
    Page<Object[]> findAllWithCommentCnt(Specification<CodingBoard> spec, Pageable pageable);
}
