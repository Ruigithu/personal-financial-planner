package com.ruipeng.planner.repository;

import com.ruipeng.planner.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByAdvisorId(Long advisorId);
    List<AvailabilitySlot> findByAdvisorIdAndDayOfWeek(Long advisorId, Integer dayOfWeek);
    List<AvailabilitySlot> findByAdvisorIdAndSpecificDate(Long advisorId, LocalDate specificDate);

    @Query("SELECT a FROM AvailabilitySlot a WHERE a.advisor.id = :advisorId AND (a.recurring = true OR a.specificDate BETWEEN :startDate AND :endDate)")
    List<AvailabilitySlot> findAvailableSlotsByDateRange(Long advisorId, LocalDate startDate, LocalDate endDate);
}