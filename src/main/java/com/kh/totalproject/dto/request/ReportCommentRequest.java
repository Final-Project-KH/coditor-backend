package com.kh.totalproject.dto.request;

import com.kh.totalproject.entity.ReportBoard;
import com.kh.totalproject.entity.ReportComment;
import com.kh.totalproject.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCommentRequest {
    private Long reportId;
    private Long commentId;
    private String content;

    public ReportComment toAddComment(User user, ReportBoard reportBoard) {
        return ReportComment.builder()
                .content(this.content)
                .user(user)
                .reportBoard(reportBoard)
                .build();
    }

    public ReportComment toModifyComment(User user, ReportBoard reportBoard, ReportComment existingData) {
        return ReportComment.builder()
                .id(commentId)
                .content(this.content != null ? this.content : existingData.getContent())
                .user(user)
                .reportBoard(reportBoard)
                .createdAt(existingData.getCreatedAt())
                .build();
    }
}
