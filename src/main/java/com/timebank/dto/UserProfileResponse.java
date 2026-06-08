package com.timebank.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String bio;
    private Double credits;
    private Double reservedCredits;
    private Double rating;
    private String availability;
    private List<String> skillsOffered;
    private List<String> skillsNeeded;
}