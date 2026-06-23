package com.timebank.controller;

import com.timebank.dto.*;
import com.timebank.entity.Transaction;
import com.timebank.service.HelpRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@CrossOrigin(origins ={ "http://localhost:4200", "http://127.0.0.1:4201"})
public class HelpRequestController {

    private final HelpRequestService helpRequestService;

    @PostMapping
    public ResponseEntity<HelpRequestResponse> createRequest(
            @Valid @RequestBody HelpRequestDTO dto) {
        return ResponseEntity.ok(helpRequestService.createRequest(dto));
    }

    @GetMapping
    public ResponseEntity<List<HelpRequestResponse>> getAllOpenRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(
                helpRequestService.getAllOpenRequests(keyword, category));
    }

    @GetMapping("/my")
    public ResponseEntity<List<HelpRequestResponse>> getMyRequests() {
        return ResponseEntity.ok(helpRequestService.getMyRequests());
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<ApplicationResponse> applyToRequest(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationDTO dto) {
        return ResponseEntity.ok(helpRequestService.applyToRequest(id, dto));
    }

    @GetMapping("/{id}/applicants")
    public ResponseEntity<List<ApplicationResponse>> getApplicants(
            @PathVariable Long id) {
        return ResponseEntity.ok(helpRequestService.getApplicants(id));
    }

    @PutMapping("/{requestId}/accept/{applicantId}")
    public ResponseEntity<HelpRequestResponse> acceptApplicant(
            @PathVariable Long requestId,
            @PathVariable Long applicantId) {
        return ResponseEntity.ok(
                helpRequestService.acceptApplicant(requestId, applicantId));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<HelpRequestResponse> completeRequest(
            @PathVariable Long id) {
        return ResponseEntity.ok(helpRequestService.completeRequest(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<HelpRequestResponse> cancelRequest(
            @PathVariable Long id) {
        return ResponseEntity.ok(helpRequestService.cancelRequest(id));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getMyTransactions() {
        return ResponseEntity.ok(helpRequestService.getMyTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HelpRequestResponse> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(helpRequestService.getRequestById(id));
    }

}