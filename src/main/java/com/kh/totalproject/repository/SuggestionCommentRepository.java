package com.kh.totalproject.repository;

import com.kh.totalproject.entity.SuggestionComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestionCommentRepository extends JpaRepository<SuggestionComment, Long> {
    Page<SuggestionComment> findBySuggestionBoard_Id(Long suggestionId, Pageable pageable);
}
