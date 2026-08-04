package com.example.demo.dto.post;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class UpdatePostRequest {

    @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
    private String title;

    private String content;

    private String image;

    @Size(max = 3, message = "카테고리는 최대 3개까지 선택할 수 있습니다.")
    private List<
            @NotNull(message = "카테고리 ID는 null일 수 없습니다.")
            @Positive(message = "카테고리 ID는 양수여야 합니다.")
                    Long
            > categoryIds;

    @AssertTrue(message = "수정할 내용을 입력해주세요.")
    public boolean hasUpdateField() {
        return title != null
                || content != null
                || image != null
                || categoryIds != null;
    }

    @AssertTrue(message = "제목을 올바르게 입력해주세요.")
    public boolean isTitleValid() {
        return title == null || !title.trim().isEmpty();
    }

    @AssertTrue(message = "내용을 올바르게 입력해주세요.")
    public boolean isContentValid() {
        return content == null || !content.trim().isEmpty();
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImage() {
        return image;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }
}