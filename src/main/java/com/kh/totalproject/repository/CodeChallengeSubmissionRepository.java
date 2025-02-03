package com.kh.totalproject.repository;

import com.kh.totalproject.entity.CodeChallengeSubmission;
import com.kh.totalproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeChallengeSubmissionRepository extends JpaRepository<CodeChallengeSubmission, Long> {
    List<CodeChallengeSubmission> findByUser(User user);
}
