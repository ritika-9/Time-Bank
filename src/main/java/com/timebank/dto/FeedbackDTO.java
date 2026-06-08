package com.timebank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FeedbackDTO {
    @NotNull
    private Long requestId;
    @NotNull
    private Long givenToUserId;
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    private String comment;
}