package com.kh.totalproject.repository;

import com.kh.totalproject.entity.CodeChallengeInfo;
import com.kh.totalproject.entity.CodeChallengeSubmission;
import com.kh.totalproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeChallengeSubmissionRepository extends JpaRepository<CodeChallengeSubmission, Long> {
    Optional<CodeChallengeSubmission> findByCodeChallengeInfoAndUser(CodeChallengeInfo codeChallengeInfo, User user);
    List<CodeChallengeSubmission> findByUser(User user);
    int countByCodeChallengeInfo(CodeChallengeInfo codeChallengeInfo);
    int countByCodeChallengeInfoAndSuccess(CodeChallengeInfo codeChallengeInfo, Boolean success);
    int countByCodeChallengeInfoAndUser(CodeChallengeInfo codeChallengeInfo, User user);
}
