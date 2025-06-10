package com.ruipeng.planner.service;

import com.ruipeng.planner.dto.AdvisorProfileDto;
import com.ruipeng.planner.dto.AvailabilitySlotDto;
import com.ruipeng.planner.entity.*;
import com.ruipeng.planner.repository.AdvisorRepository;
import com.ruipeng.planner.repository.AppointmentRepository;
import com.ruipeng.planner.repository.AvailabilitySlotRepository;
import com.ruipeng.planner.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


@Service
public class AdvisorService {
    private final AdvisorRepository advisorRepository;
    private final UserRepository userRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AdvisorService(AdvisorRepository advisorRepository, UserRepository userRepository, AvailabilitySlotRepository availabilitySlotRepository, AppointmentRepository appointmentRepository) {
        this.advisorRepository = advisorRepository;
        this.userRepository = userRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<AdvisorProfileDto> getAllAdvisors() {
        List<Advisor> allAdvisors = advisorRepository.findAllByOrderByRatingDesc();
        return setAllAdvisorsProfileDto(allAdvisors);
    }

    protected List<AdvisorProfileDto> setAllAdvisorsProfileDto(List<Advisor> allAdvisors) {
        List<AdvisorProfileDto>result = new ArrayList<>();
        for(Advisor advisor : allAdvisors) {
            AdvisorProfileDto advisorProfileDto = new AdvisorProfileDto();
            advisorProfileDto.setId(advisor.getId());
            advisorProfileDto.setFirstName(advisor.getUser().getFirstName());
            advisorProfileDto.setLastName(advisor.getUser().getLastName());
            advisorProfileDto.setBio(advisor.getBio());
            advisorProfileDto.setExperienceYears(advisor.getExperienceYears());
            advisorProfileDto.setAverageRating(advisor.getAverageRating());
            advisorProfileDto.setProfileImageUrl(advisor.getProfileImageUrl());
            advisorProfileDto.setSpecialties(advisor.getSpecialties());

            result.add(advisorProfileDto);

        }
        return result;
    }

    public Advisor getAdvisorById(Long id) {
        return advisorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Advisor not found with id: " + id));
    }

    public AdvisorProfileDto getAdvisorProfileDtoById(Long id) {
        Optional<Advisor> advisor = advisorRepository.findById(id);
        AdvisorProfileDto advisorProfileDto = new AdvisorProfileDto();
        advisorProfileDto.setId(advisor.get().getId());
        advisorProfileDto.setFirstName(advisor.get().getUser().getFirstName());
        advisorProfileDto.setLastName(advisor.get().getUser().getLastName());
        advisorProfileDto.setBio(advisor.get().getBio());
        advisorProfileDto.setExperienceYears(advisor.get().getExperienceYears());
        advisorProfileDto.setAverageRating(advisor.get().getAverageRating());
        advisorProfileDto.setProfileImageUrl(advisor.get().getProfileImageUrl());
        advisorProfileDto.setSpecialties(advisor.get().getSpecialties());
        advisorProfileDto.setLanguages(advisor.get().getLanguages());
        return advisorProfileDto;
    }

    public List<Advisor> findAdvisorsBySpecialty(String specialty) {
        return advisorRepository.findBySpecialty(specialty);
    }

    public List<Advisor> findAdvisorsByLanguage(String language) {
        return advisorRepository.findByLanguage(language);
    }

    @Transactional
    public Advisor updateAdvisorProfile(Long advisorId, AdvisorProfileDto profileDto) {
        Advisor advisor = getAdvisorById(advisorId);

        if (profileDto.getProfessionalTitle() != null) {
            advisor.setProfessionalTitle(profileDto.getProfessionalTitle());
        }
        if (profileDto.getExperienceYears() != null) {
            advisor.setExperienceYears(profileDto.getExperienceYears());
        }
        if (profileDto.getBio() != null) {
            advisor.setBio(profileDto.getBio());
        }

        if (profileDto.getProfileImageUrl() != null) {
            advisor.setProfileImageUrl(profileDto.getProfileImageUrl());
        }
        if (profileDto.getSpecialties() != null) {
            advisor.setSpecialties(new HashSet<>(profileDto.getSpecialties()));
        }
        if (profileDto.getLanguages() != null) {
            advisor.setLanguages(new HashSet<>(profileDto.getLanguages()));
        }

        return advisorRepository.save(advisor);
    }

    @Transactional
    public AvailabilitySlot addAvailabilitySlot(Long advisorId, AvailabilitySlotDto slotDto) {
        Advisor advisor = getAdvisorById(advisorId);

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setAdvisor(advisor);
        slot.setDayOfWeek(slotDto.getDayOfWeek());
        slot.setStartTime(slotDto.getStartTime());
        slot.setEndTime(slotDto.getEndTime());
        slot.setRecurring(slotDto.isRecurring());
        slot.setSpecificDate(slotDto.getSpecificDate());

        return availabilitySlotRepository.save(slot);
    }

    @Transactional
    public void removeAvailabilitySlot(Long slotId) {
        if (!availabilitySlotRepository.existsById(slotId)) {
            throw new EntityNotFoundException("Availability slot not found with id: " + slotId);
        }
        availabilitySlotRepository.deleteById(slotId);
    }

    public List<AvailabilitySlot> getAdvisorAvailability(Long advisorId) {
        return availabilitySlotRepository.findByAdvisorId(advisorId);
    }

    public List<Map<String, Object>> getAvailableTimeSlots(Long advisorId, LocalDate startDate, LocalDate endDate) {
        // Get all availability slots for the advisor
        List<AvailabilitySlot> availabilitySlots = availabilitySlotRepository
                .findAvailableSlotsByDateRange(advisorId, startDate, endDate);

        // Get all existing appointments for the advisor in this date range
        List<Appointment> existingAppointments = appointmentRepository.findByAdvisorIdAndAppointmentDateBetween(
                advisorId,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        );

        // Map to store available time slots by date
        List<Map<String, Object>> availableSlots = new ArrayList<>();

        // Loop through each day in the date range
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1-7 for Monday-Sunday

            // Find recurring slots for this day of week and any specific slots for this date
            List<AvailabilitySlot> slotsForDay = availabilitySlots.stream()
                    .filter(slot -> (slot.isRecurring() && slot.getDayOfWeek() == dayOfWeek) ||
                            (!slot.isRecurring() && slot.getSpecificDate() != null &&
                                    slot.getSpecificDate().equals(currentDate)))
                    .toList();

            // For each slot, break it into 30-minute increments and check against existing appointments
            for (AvailabilitySlot slot : slotsForDay) {
                LocalTime start = slot.getStartTime();
                LocalTime end = slot.getEndTime();

                while (!start.plusMinutes(30).isAfter(end)) {
                    LocalDateTime slotStart = currentDate.atTime(start);
                    LocalDateTime slotEnd = currentDate.atTime(start.plusMinutes(30));

                    // Check if this time slot overlaps with any existing appointment
                    boolean isAvailable = existingAppointments.stream()
                            .noneMatch(appt -> {
                                LocalDateTime apptStart = appt.getAppointmentDate();
                                LocalDateTime apptEnd = apptStart.plusMinutes(appt.getDurationMinutes());
                                return (slotStart.isBefore(apptEnd) && apptStart.isBefore(slotEnd));
                            });

                    if (isAvailable) {
                        Map<String, Object> availableSlot = new HashMap<>();
                        availableSlot.put("date", currentDate.toString());
                        availableSlot.put("startTime", start.toString());
                        availableSlot.put("endTime", start.plusMinutes(30).toString());
                        availableSlots.add(availableSlot);
                    }

                    start = start.plusMinutes(30);
                }
            }
        }

        return availableSlots;
    }

    public Advisor getAdvisorByUserId(Long userId) {
        return advisorRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("No advisor found for user ID: " + userId));
    }
}
