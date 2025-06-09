package com.ruipeng.planner.repository;

import com.ruipeng.planner.entity.Appointment;
import com.ruipeng.planner.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserId(Long userId);
    List<Appointment> findByAdvisorId(Long advisorId);
    List<Appointment> findByUserIdAndStatus(Long userId, AppointmentStatus status);
    List<Appointment> findByAdvisorIdAndStatus(Long advisorId, AppointmentStatus status);
    List<Appointment> findByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);
    List<Appointment> findByAdvisorIdAndAppointmentDateBetween(Long advisorId, LocalDateTime start, LocalDateTime end);
}
