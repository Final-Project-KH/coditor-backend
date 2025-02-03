package com.kh.totalproject.entity;

import com.kh.totalproject.constant.ChallengeDifficulty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class CodeChallengeInfo {
    // 직접 관리 (data.sql 파일로 관리)
    @Id
    private Long questionId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String cond;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeDifficulty difficulty;

    private Integer memoryLimit;

    private Integer runningTimeLimit;
}
