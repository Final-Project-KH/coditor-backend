package com.kh.totalproject.repository;

import com.kh.totalproject.constant.ChallengeDifficulty;
import com.kh.totalproject.entity.CodeChallengeInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeChallengeInfoRepository extends JpaRepository<CodeChallengeInfo, Long> {
    List<CodeChallengeInfo> findByDifficulty(ChallengeDifficulty difficulty);
}
