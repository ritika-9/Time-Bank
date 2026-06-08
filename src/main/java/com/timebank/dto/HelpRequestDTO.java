package com.timebank.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HelpRequestDTO {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private Long skillId;
    @NotNull
    @Min(1)
    private Double hoursRequired;
}