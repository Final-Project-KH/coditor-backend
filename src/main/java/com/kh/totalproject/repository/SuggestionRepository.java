package com.kh.totalproject.repository;

import com.kh.totalproject.entity.SuggestionBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestionRepository extends JpaRepository<SuggestionBoard, Long> {
    @Query("SELECT s FROM SuggestionBoard s WHERE s.user.userKey = :userKey")
    Page<SuggestionBoard> findByUserKey(Long userKey, Pageable pageable);
}
