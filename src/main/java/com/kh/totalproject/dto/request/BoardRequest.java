package com.kh.totalproject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardRequest {
    private String title;
    private String content;
    private String imgUrl;
    private String name;
}