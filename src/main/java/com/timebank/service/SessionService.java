package com.timebank.service;

import com.timebank.dto.SessionDTO;
import com.timebank.dto.SessionResponse;
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
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public SessionResponse createSession(SessionDTO dto) {
        User currentUser = userService.getCurrentUser();

        // validate credit range
        if (dto.getCredits() < 1 || dto.getCredits() > 10) {
            throw new RuntimeException("Credits must be between 1 and 10");
        }

        Skill skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        Session session = Session.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .skill(skill)
                .credits(dto.getCredits())
                .duration(dto.getDuration())
                .status(SessionStatus.AVAILABLE)
                .offeredBy(currentUser)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        sessionRepository.save(session);
        return mapToResponse(session);
    }

    public List<SessionResponse> getAllAvailableSessions(String keyword) {
        List<Session> sessions;
        if (keyword != null && !keyword.isBlank()) {
            sessions = sessionRepository
                    .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            keyword, keyword);
        } else {
            sessions = sessionRepository.findByStatus(SessionStatus.AVAILABLE);
        }
        return sessions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ⭐ INTERVIEW QUESTION: "How does session booking work?"
    // Answer: Credits are reserved from student when they request booking
    // Session stays AVAILABLE until poster confirms
    // On confirmation → status changes to BOOKED
    // Credits only fully transfer on COMPLETION
    @Transactional
    public SessionResponse requestBooking(Long sessionId) {
        User currentUser = userService.getCurrentUser();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getOfferedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Cannot book your own session");
        }

        if (session.getStatus() != SessionStatus.AVAILABLE) {
            throw new RuntimeException("Session is not available");
        }

        // check student has enough credits
        double availableCredits = currentUser.getCredits()
                - currentUser.getReservedCredits();
        if (availableCredits < session.getCredits()) {
            throw new RuntimeException("Insufficient credits");
        }

        // reserve credits from student
        currentUser.setReservedCredits(
                currentUser.getReservedCredits() + session.getCredits());
        userRepository.save(currentUser);

        session.setBookedBy(currentUser);
        sessionRepository.save(session);

        // notify session poster
        notificationService.createNotification(
                session.getOfferedBy(),
                currentUser.getName() + " wants to book your session: " + session.getTitle(),
                NotificationType.SESSION_BOOKED,
                session.getId()
        );

        return mapToResponse(session);
    }

    @Transactional
    public SessionResponse confirmBooking(Long sessionId, LocalDateTime scheduledTime) {
        User currentUser = userService.getCurrentUser();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getOfferedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only session poster can confirm booking");
        }

        session.setStatus(SessionStatus.BOOKED);
        session.setScheduledTime(scheduledTime);
        sessionRepository.save(session);

        // notify student
        notificationService.createNotification(
                session.getBookedBy(),
                "Your booking for '" + session.getTitle() + "' is confirmed for "
                        + scheduledTime + ". Chat is now open!",
                NotificationType.SESSION_CONFIRMED,
                session.getId()
        );

        return mapToResponse(session);
    }

    @Transactional
    public SessionResponse completeSession(Long sessionId) {
        User currentUser = userService.getCurrentUser();

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getOfferedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only session poster can mark complete");
        }

        if (session.getStatus() != SessionStatus.BOOKED) {
            throw new RuntimeException("Session must be booked before completing");
        }

        User student = session.getBookedBy();
        User teacher = session.getOfferedBy();
        Double credits = session.getCredits();

        // release reserved credits and deduct from student
        student.setReservedCredits(student.getReservedCredits() - credits);
        student.setCredits(student.getCredits() - credits);

        // add to teacher
        teacher.setCredits(teacher.getCredits() + credits);

        userRepository.save(student);
        userRepository.save(teacher);

        // record transaction
        transactionRepository.save(Transaction.builder()
                .sender(student)
                .receiver(teacher)
                .hours(credits)
                .request(null)
                .build());

        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);

        notificationService.createNotification(
                student,
                "Session '" + session.getTitle() + "' completed. -"
                        + credits + " credits.",
                NotificationType.SESSION_COMPLETED,
                session.getId()
        );

        return mapToResponse(session);
    }

    private SessionResponse mapToResponse(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .title(session.getTitle())
                .description(session.getDescription())
                .skillName(session.getSkill().getName())
                .skillCategory(session.getSkill().getCategory())
                .credits(session.getCredits())
                .duration(session.getDuration())
                .status(session.getStatus())
                .offeredByName(session.getOfferedBy().getName())
                .offeredByRating(session.getOfferedBy().getRating())
                .bookedByName(session.getBookedBy() != null ?
                        session.getBookedBy().getName() : null)
                .scheduledTime(session.getScheduledTime())
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .build();
    }

    public SessionResponse getSessionById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return mapToResponse(session);
    }

    public List<SessionResponse> getMySessions() {
        User currentUser = userService.getCurrentUser();
        return sessionRepository.findByOfferedBy(currentUser)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}