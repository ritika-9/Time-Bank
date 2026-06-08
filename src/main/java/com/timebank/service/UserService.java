package com.timebank.service;

import com.timebank.dto.UpdateProfileDTO;
import com.timebank.dto.UserProfileResponse;
import com.timebank.entity.*;
import com.timebank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;

    // ⭐ INTERVIEW QUESTION: "How do you get the current logged in user?"
    // Answer: Spring Security stores authenticated user in SecurityContextHolder
    // Our JwtFilter puts the user there on every request after validating token
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserProfileResponse getProfile() {
        User user = getCurrentUser();
        return buildProfileResponse(user);
    }

    public UserProfileResponse getProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileDTO dto) {
        User user = getCurrentUser();

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getAvailability() != null) user.setAvailability(dto.getAvailability());

        // update skills offered
        if (dto.getSkillsOffered() != null) {
            // remove existing offered skills
            List<UserSkill> existing = userSkillRepository
                    .findByUserAndType(user, SkillType.OFFERED);
            userSkillRepository.deleteAll(existing);

            // add new ones
            dto.getSkillsOffered().forEach(skillId -> {
                Skill skill = skillRepository.findById(skillId)
                        .orElseThrow(() -> new RuntimeException("Skill not found: " + skillId));
                userSkillRepository.save(UserSkill.builder()
                        .user(user)
                        .skill(skill)
                        .type(SkillType.OFFERED)
                        .build());
            });
        }

        // update skills needed
        if (dto.getSkillsNeeded() != null) {
            List<UserSkill> existing = userSkillRepository
                    .findByUserAndType(user, SkillType.NEEDED);
            userSkillRepository.deleteAll(existing);

            dto.getSkillsNeeded().forEach(skillId -> {
                Skill skill = skillRepository.findById(skillId)
                        .orElseThrow(() -> new RuntimeException("Skill not found: " + skillId));
                userSkillRepository.save(UserSkill.builder()
                        .user(user)
                        .skill(skill)
                        .type(SkillType.NEEDED)
                        .build());
            });
        }

        userRepository.save(user);
        return buildProfileResponse(user);
    }

    // builds the response DTO from User entity
    private UserProfileResponse buildProfileResponse(User user) {
        List<UserSkill> userSkills = userSkillRepository.findByUser(user);

        List<String> offered = userSkills.stream()
                .filter(us -> us.getType() == SkillType.OFFERED)
                .map(us -> us.getSkill().getName())
                .collect(Collectors.toList());

        List<String> needed = userSkills.stream()
                .filter(us -> us.getType() == SkillType.NEEDED)
                .map(us -> us.getSkill().getName())
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bio(user.getBio())
                .credits(user.getCredits())
                .reservedCredits(user.getReservedCredits())
                .rating(user.getRating())
                .availability(user.getAvailability())
                .skillsOffered(offered)
                .skillsNeeded(needed)
                .build();
    }
}