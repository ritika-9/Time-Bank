package com.timebank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SessionDTO {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private Long skillId;
    @NotNull
    @Min(1)
    @Max(10)
    private Double credits;
    @NotNull
    @Min(1)
    private Double duration;
}