package com.timebank.repository;

import com.timebank.entity.Application;
import com.timebank.entity.HelpRequest;
import com.timebank.entity.User;
import com.timebank.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByRequest(HelpRequest request);
    List<Application> findByApplicant(User applicant);
    boolean existsByRequestAndApplicant(HelpRequest request, User applicant);
    Optional<Application> findByRequestAndApplicant(HelpRequest request, User applicant);
    List<Application> findByRequestAndStatus(HelpRequest request, ApplicationStatus status);
}