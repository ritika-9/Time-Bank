package com.timebank.controller;

import com.timebank.entity.Skill;
import com.timebank.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@CrossOrigin(origins ={ "http://localhost:4200", "http://127.0.0.1:4201"})
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @PostMapping
    public ResponseEntity<Skill> addSkill(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                skillService.addSkill(body.get("name"), body.get("category")));
    }
}