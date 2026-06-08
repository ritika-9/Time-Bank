package com.timebank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationDTO {
    @NotBlank
    private String message;
}