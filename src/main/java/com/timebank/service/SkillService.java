package com.timebank.service;

import com.timebank.entity.Skill;
import com.timebank.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    // ⭐ INTERVIEW QUESTION: "How do you prevent duplicate skills?"
    // Answer: Check by name case-insensitively before creating
    // existsByNameIgnoreCase is a derived query — Spring generates SQL automatically
    public Skill addSkill(String name, String category) {
        // if skill already exists return existing one
        if (skillRepository.existsByNameIgnoreCase(name)) {
            return skillRepository.findByNameIgnoreCase(name)
                    .orElseThrow(() -> new RuntimeException("Skill not found"));
        }

        // auto categorize if category not provided
        if (category == null || category.isBlank()) {
            category = autoCategory(name);
        }

        return skillRepository.save(Skill.builder()
                .name(name)
                .category(category)
                .build());
    }

    // simple keyword based auto categorization
    // ⭐ INTERVIEW QUESTION: "How does auto categorization work?"
    // Answer: keyword matching against skill name — no AI needed for basic cases
    private String autoCategory(String skillName) {
        String lower = skillName.toLowerCase();
        if (lower.matches(".*(java|python|spring|react|angular|node|code|programming|sql|api).*"))
            return "Programming";
        if (lower.matches(".*(ml|ai|machine learning|deep learning|nlp|data science).*"))
            return "AI/ML";
        if (lower.matches(".*(resume|career|interview|job|linkedin|portfolio).*"))
            return "Career";
        if (lower.matches(".*(design|figma|ui|ux|photoshop|illustrator).*"))
            return "Design";
        if (lower.matches(".*(math|physics|chemistry|biology|science).*"))
            return "Academics";
        return "General";
    }
}