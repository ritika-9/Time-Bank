package com.timebank.dto;

import lombok.Data;
import java.util.List;

@Data
public class UpdateProfileDTO {
    private String name;
    private String bio;
    private String availability;
    private List<Long> skillsOffered;
    private List<Long> skillsNeeded;
}