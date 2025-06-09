package com.ruipeng.planner.repository;

import com.ruipeng.planner.entity.Advisor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvisorRepository extends JpaRepository<Advisor, Long> {
    Optional<Advisor> findByUserId(Long userId);

    @Query("SELECT a FROM Advisor a ORDER BY a.averageRating DESC")
    List<Advisor> findAllByOrderByRatingDesc();

    @Query("SELECT a FROM Advisor a WHERE :specialty MEMBER OF a.specialties")
    List<Advisor> findBySpecialty(String specialty);

    @Query("SELECT a FROM Advisor a WHERE :language MEMBER OF a.languages")
    List<Advisor> findByLanguage(String language);
}
