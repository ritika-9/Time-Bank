package com.timebank.controller;

import com.timebank.dto.SessionDTO;
import com.timebank.dto.SessionResponse;
import com.timebank.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins ={ "http://localhost:4200", "http://127.0.0.1:4201"})
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody SessionDTO dto) {
        return ResponseEntity.ok(sessionService.createSession(dto));
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getAllSessions(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(sessionService.getAllAvailableSessions(keyword));
    }

    @PostMapping("/{id}/book")
    public ResponseEntity<SessionResponse> requestBooking(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.requestBooking(id));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<SessionResponse> confirmBooking(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        LocalDateTime scheduledTime = LocalDateTime.parse(body.get("scheduledTime"));
        return ResponseEntity.ok(sessionService.confirmBooking(id, scheduledTime));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<SessionResponse> completeSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.completeSession(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }
    @GetMapping("/my")
    public ResponseEntity<List<SessionResponse>> getMySessions() {
        return ResponseEntity.ok(sessionService.getMySessions());
    }
}