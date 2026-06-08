package com.timebank.repository;

import com.timebank.entity.Session;
import com.timebank.entity.SessionStatus;
import com.timebank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByStatus(SessionStatus status);
    List<Session> findByOfferedBy(User user);
    List<Session> findByBookedBy(User user);
    List<Session> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description);
}