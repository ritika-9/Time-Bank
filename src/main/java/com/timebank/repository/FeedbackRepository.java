package com.timebank.repository;

import com.timebank.entity.Feedback;
import com.timebank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByGivenTo(User user);
    boolean existsByGivenByAndRequest_Id(User givenBy, Long requestId);
}