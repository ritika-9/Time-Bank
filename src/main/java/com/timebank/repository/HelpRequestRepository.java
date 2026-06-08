package com.timebank.repository;

import com.timebank.entity.HelpRequest;
import com.timebank.entity.RequestStatus;
import com.timebank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {
    List<HelpRequest> findByStatus(RequestStatus status);
    List<HelpRequest> findByCreatedBy(User user);

    // search by keyword in title or description
    List<HelpRequest> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description);

    // find by skill category
    @Query("SELECT h FROM HelpRequest h WHERE h.skill.category = :category AND h.status = 'OPEN'")
    List<HelpRequest> findBySkillCategory(String category);
}