package com.ruipeng.planner.repository;

import com.ruipeng.planner.entity.FinancialPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialPlanRepository extends JpaRepository<FinancialPlan, Long> {
    List<FinancialPlan> findByUserId(Long userId);
    Optional<FinancialPlan> findByShareCode(String shareCode);
}
