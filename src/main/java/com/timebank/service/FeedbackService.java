package com.timebank.service;

import com.timebank.dto.FeedbackDTO;
import com.timebank.entity.*;
import com.timebank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    // ⭐ INTERVIEW QUESTION: "How do you calculate average rating?"
    // Answer: After every feedback submission, fetch all feedback
    // for that user and compute average using Java streams
    // mapToInt extracts rating, average() returns OptionalDouble
    @Transactional
    public Feedback submitFeedback(FeedbackDTO dto) {
        User currentUser = userService.getCurrentUser();

        HelpRequest request = helpRequestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != RequestStatus.COMPLETED) {
            throw new RuntimeException("Can only give feedback on completed requests");
        }

        // prevent duplicate feedback
        if (feedbackRepository.existsByGivenByAndRequest_Id(currentUser, dto.getRequestId())) {
            throw new RuntimeException("You have already given feedback for this request");
        }

        User givenTo = userRepository.findById(dto.getGivenToUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Feedback feedback = Feedback.builder()
                .rating(dto.getRating())
                .comment(dto.getComment())
                .givenBy(currentUser)
                .givenTo(givenTo)
                .request(request)
                .build();

        feedbackRepository.save(feedback);

        // recalculate average rating
        List<Feedback> allFeedback = feedbackRepository.findByGivenTo(givenTo);
        double avgRating = allFeedback.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        givenTo.setRating(Math.round(avgRating * 10.0) / 10.0);
        userRepository.save(givenTo);

        return feedback;
    }
}