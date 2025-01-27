package com.kh.totalproject.dto.request;

import com.kh.totalproject.entity.Board;
import com.kh.totalproject.entity.ReportBoard;
import com.kh.totalproject.entity.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {
    private Long reportId;
    private Long boardId;
    private String title;
    private String name;
    private String content;
    private String imgUrl;
    private List<String> report;

    public ReportBoard toCreateReportPost(User user, Board board) {
        return ReportBoard.builder()
                .id(reportId)
                .user(user)
                .board(board)
                .title(title)
                .content(content)
                .imgUrl(imgUrl)
                .report(report)
                .build();
    }

    public ReportBoard toModifyReportPost(ReportBoard existingData) {
        return ReportBoard.builder()
                .id(reportId)
                .user(existingData.getUser())
                .board(existingData.getBoard())
                .title(title != null ? title : existingData.getTitle())
                .content(content != null ? content : existingData.getContent())
                .imgUrl(imgUrl != null ? imgUrl : existingData.getImgUrl())
                .report(report != null ? report : existingData.getReport())
                .build();
    }
}
