package com.kh.totalproject.entity;

import com.kh.totalproject.constant.Language;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeChallengeSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;

    // 1명의 유저는 n개의 제출 1개의 제출은 1명의 유저
    @ManyToOne
    @JoinColumn(nullable = false, name="user_id")
    private User user;

    // 1개의 문제는 n개의 제출을 가질 수 있고, 1개의 제출은 1개의 문제를 가진다.
    @ManyToOne
    @JoinColumn(nullable = false, name="question_id")
    private CodeChallengeInfo codeChallengeInfo;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language codeLanguage;

    @Column(nullable = false)
    private Boolean success;

    @Column(nullable = false)
    private Float memoryUsage;

    @Column(nullable = false)
    private Integer runningTime;

    @Column(nullable = false)
    private Integer codeSize;

    @Column(nullable = false)
    private LocalDateTime submittedAt;
}
