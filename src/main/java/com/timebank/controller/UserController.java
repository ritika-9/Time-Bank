package com.timebank.controller;

import com.timebank.dto.UpdateProfileDTO;
import com.timebank.dto.UserProfileResponse;
import com.timebank.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins ={ "http://localhost:4200", "http://127.0.0.1:4201"})
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfileById(id));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestBody UpdateProfileDTO dto) {
        return ResponseEntity.ok(userService.updateProfile(dto));
    }
}