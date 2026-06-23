package com.timebank.service;

import com.timebank.dto.*;
import com.timebank.entity.*;
import com.timebank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HelpRequestService {

    private final HelpRequestRepository helpRequestRepository;
    private final ApplicationRepository applicationRepository;
    private final SkillRepository skillRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    // ⭐ INTERVIEW QUESTION: "Explain the escrow system"
    // Answer: When a request is posted, credits are RESERVED (locked) immediately
    // This prevents double spending — user can't use same credits elsewhere
    // On completion → reserved credits transfer to helper
    // On cancellation → reserved credits return to user
    // This is same concept used in payment gateways and fintech apps
    @Transactional
    public HelpRequestResponse createRequest(HelpRequestDTO dto) {
        User currentUser = userService.getCurrentUser();

        // check available credits (total minus already reserved)
        double availableCredits = currentUser.getCredits()
                - currentUser.getReservedCredits();

        if (availableCredits < dto.getHoursRequired()) {
            throw new RuntimeException(
                    "Insufficient credits. Available: " + availableCredits +
                            ", Required: " + dto.getHoursRequired());
        }

        Skill skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        // RESERVE credits immediately (escrow)
        currentUser.setReservedCredits(
                currentUser.getReservedCredits() + dto.getHoursRequired());
        userRepository.save(currentUser);

        HelpRequest request = HelpRequest.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .skill(skill)
                .hoursRequired(dto.getHoursRequired())
                .status(RequestStatus.OPEN)
                .createdBy(currentUser)
                .creditsReserved(true)
                .createdAt(LocalDateTime.now())
                .build();

        helpRequestRepository.save(request);
        return mapToResponse(request);
    }

    // ⭐ INTERVIEW QUESTION: "How do you handle search?"
    // Answer: Spring Data JPA derived query with ContainingIgnoreCase
    // generates SQL LIKE '%keyword%' automatically
    // No manual SQL needed
    public List<HelpRequestResponse> getAllOpenRequests(String keyword, String category) {
        List<HelpRequest> requests;

        if (keyword != null && !keyword.isBlank()) {
            requests = helpRequestRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            keyword, keyword);
        } else if (category != null && !category.isBlank()) {
            requests = helpRequestRepository.findBySkillCategory(category);
        } else {
            requests = helpRequestRepository.findByStatus(RequestStatus.OPEN);
        }

        return requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<HelpRequestResponse> getMyRequests() {
        User currentUser = userService.getCurrentUser();
        return helpRequestRepository.findByCreatedBy(currentUser)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ⭐ INTERVIEW QUESTION: "How does the application system work?"
    // Answer: Instead of first-come-first-served, users APPLY to requests
    // Creator reviews all applicants and picks who they want
    // This prevents race conditions and gives creator control
    @Transactional
    public ApplicationResponse applyToRequest(Long requestId, ApplicationDTO dto) {
        User currentUser = userService.getCurrentUser();

        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // cannot apply to own request
        if (request.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Cannot apply to your own request");
        }

        // only apply to open requests
        if (request.getStatus() != RequestStatus.OPEN) {
            throw new RuntimeException("Request is no longer open");
        }

        // prevent duplicate applications
        if (applicationRepository.existsByRequestAndApplicant(request, currentUser)) {
            throw new RuntimeException("You have already applied to this request");
        }

        Application application = Application.builder()
                .request(request)
                .applicant(currentUser)
                .message(dto.getMessage())
                .status(ApplicationStatus.PENDING)
                .build();

        applicationRepository.save(application);

        // notify request creator
        notificationService.createNotification(
                request.getCreatedBy(),
                currentUser.getName() + " applied to your request: " + request.getTitle(),
                NotificationType.APPLICATION_RECEIVED,
                request.getId()
        );

        return mapToApplicationResponse(application);
    }

    // get all applicants for a request (only creator can see this)
    public List<ApplicationResponse> getApplicants(Long requestId) {
        User currentUser = userService.getCurrentUser();

        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only request creator can view applicants");
        }

        return applicationRepository.findByRequest(request)
                .stream()
                .map(this::mapToApplicationResponse)
                .collect(Collectors.toList());
    }

    // ⭐ INTERVIEW QUESTION: "What is @Transactional and why is it important here?"
    // Answer: @Transactional ensures ALL operations succeed or ALL fail together
    // If we accept applicant but notification fails → everything rolls back
    // Prevents data inconsistency — no half-completed states
    // This is called ACID compliance (Atomicity)
    @Transactional
    public HelpRequestResponse acceptApplicant(Long requestId, Long applicantId) {
        User currentUser = userService.getCurrentUser();

        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only request creator can accept applicants");
        }

        if (request.getStatus() != RequestStatus.OPEN) {
            throw new RuntimeException("Request is no longer open");
        }

        User acceptedApplicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new RuntimeException("Applicant not found"));

        // accept this application
        Application accepted = applicationRepository
                .findByRequestAndApplicant(request, acceptedApplicant)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        accepted.setStatus(ApplicationStatus.ACCEPTED);
        applicationRepository.save(accepted);

        // reject all other applications
        List<Application> others = applicationRepository
                .findByRequestAndStatus(request, ApplicationStatus.PENDING);
        others.stream()
                .filter(a -> !a.getApplicant().getId().equals(applicantId))
                .forEach(a -> {
                    a.setStatus(ApplicationStatus.REJECTED);
                    // notify rejected applicants
                    notificationService.createNotification(
                            a.getApplicant(),
                            "Your application for '" + request.getTitle() + "' was not selected",
                            NotificationType.APPLICATION_REJECTED,
                            request.getId()
                    );
                });
        applicationRepository.saveAll(others);

        // update request status
        request.setAcceptedBy(acceptedApplicant);
        request.setStatus(RequestStatus.ACCEPTED);
        helpRequestRepository.save(request);

        // notify accepted applicant
        notificationService.createNotification(
                acceptedApplicant,
                "Your application for '" + request.getTitle() + "' was accepted! Chat is now open.",
                NotificationType.APPLICATION_ACCEPTED,
                request.getId()
        );

        return mapToResponse(request);
    }

    // ⭐ INTERVIEW QUESTION: "Walk me through the credit transfer logic"
    // Answer:
    // 1. Creator marks request complete
    // 2. Reserved credits released from creator's reservedCredits
    // 3. Those credits deducted from creator's actual credits
    // 4. Same amount added to helper's credits
    // 5. Transaction record created for audit trail
    // 6. Request marked COMPLETED
    // All in one @Transactional block — either all happens or nothing
    @Transactional
    public HelpRequestResponse completeRequest(Long requestId) {
        User currentUser = userService.getCurrentUser();

        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only request creator can mark complete");
        }

        if (request.getStatus() != RequestStatus.ACCEPTED) {
            throw new RuntimeException("Request must be accepted before completing");
        }

        User requester = request.getCreatedBy();
        User helper = request.getAcceptedBy();
        Double hours = request.getHoursRequired();

        // release reserved credits and deduct from actual balance
        requester.setReservedCredits(requester.getReservedCredits() - hours);
        requester.setCredits(requester.getCredits() - hours);

        // add credits to helper
        helper.setCredits(helper.getCredits() + hours);

        userRepository.save(requester);
        userRepository.save(helper);

        // create transaction record (audit trail)
        transactionRepository.save(Transaction.builder()
                .sender(requester)
                .receiver(helper)
                .hours(hours)
                .request(request)
                .build());

        request.setStatus(RequestStatus.COMPLETED);
        helpRequestRepository.save(request);

        // notify helper
        notificationService.createNotification(
                helper,
                requester.getName() + " marked your session complete. +" + hours + " credits added!",
                NotificationType.CREDITS_RECEIVED,
                request.getId()
        );

        return mapToResponse(request);
    }

    // cancel request and refund reserved credits
    @Transactional
    public HelpRequestResponse cancelRequest(Long requestId) {
        User currentUser = userService.getCurrentUser();

        HelpRequest request = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only request creator can cancel");
        }

        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed request");
        }

        // refund reserved credits back to user
        if (request.getCreditsReserved()) {
            currentUser.setReservedCredits(
                    currentUser.getReservedCredits() - request.getHoursRequired());
            userRepository.save(currentUser);
        }

        request.setStatus(RequestStatus.OPEN);
        helpRequestRepository.save(request);

        return mapToResponse(request);
    }

    public List<Transaction> getMyTransactions() {
        User currentUser = userService.getCurrentUser();
        return transactionRepository
                .findBySenderOrReceiverOrderByTimestampDesc(currentUser, currentUser);
    }

    private HelpRequestResponse mapToResponse(HelpRequest request) {
        int applicantCount = applicationRepository
                .findByRequest(request).size();

        return HelpRequestResponse.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .skillName(request.getSkill().getName())
                .skillCategory(request.getSkill().getCategory())
                .hoursRequired(request.getHoursRequired())
                .status(request.getStatus())
                .createdByName(request.getCreatedBy().getName())
                .createdByRating(request.getCreatedBy().getRating())
                .acceptedByName(request.getAcceptedBy() != null ?
                        request.getAcceptedBy().getName() : null)
                .applicantCount(applicantCount)
                .createdAt(request.getCreatedAt())
                .build();
    }

    private ApplicationResponse mapToApplicationResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .requestId(app.getRequest().getId())
                .requestTitle(app.getRequest().getTitle())
                .applicantId(app.getApplicant().getId())
                .applicantName(app.getApplicant().getName())
                .applicantRating(app.getApplicant().getRating())
                .message(app.getMessage())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .build();
    }

    public HelpRequestResponse getRequestById(Long id) {
        HelpRequest request = helpRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return mapToResponse(request);
    }
}