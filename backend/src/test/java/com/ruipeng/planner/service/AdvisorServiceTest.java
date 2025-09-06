package com.ruipeng.planner.service;

import com.ruipeng.planner.dto.AdvisorProfileDto;
import com.ruipeng.planner.dto.AvailabilitySlotDto;
import com.ruipeng.planner.entity.*;
import com.ruipeng.planner.repository.AdvisorRepository;
import com.ruipeng.planner.repository.AppointmentRepository;
import com.ruipeng.planner.repository.AvailabilitySlotRepository;
import com.ruipeng.planner.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdvisorServiceTest {

    @Mock
    private AdvisorRepository advisorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AdvisorService advisorService;

    private Advisor createMockAdvisor(Long id, String firstName, String lastName,
                                      String bio, Integer experienceYears, Double rating,
                                      String profileImageUrl, Set<String> specialties) {

        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com");
        user.setRole(UserRole.ADVISOR);
        user.setStatus(AccountStatus.ACTIVE);
        user.setRegistrationDate(LocalDateTime.now());
        user.setPasswordHash("hashedPassword123");

        Advisor advisor = new Advisor();
        advisor.setId(id);
        advisor.setUser(user);
        advisor.setBio(bio);
        advisor.setExperienceYears(experienceYears);
        advisor.setAverageRating(rating);
        advisor.setProfileImageUrl(profileImageUrl);
        advisor.setSpecialties(specialties);

        return advisor;
    }

    private List<Advisor> createDiverseMockAdvisorList() {
        List<Advisor> advisors = new ArrayList<>();

        advisors.add(createMockAdvisor(1L, "John", "Smith",
                "Senior Financial Advisor with expertise in wealth management",
                8, 4.8, "http://example.com/john_smith.jpg",
                Set.of("Financial Planning", "Investment Advisory", "Retirement Planning")));
        advisors.add(createMockAdvisor(2L, "Emily", "Johnson",
                "Investment specialist focused on portfolio optimization",
                5, 4.6, "http://example.com/emily_johnson.jpg",
                Set.of("Stock Investment", "Portfolio Management")));
        return advisors;
    }

    @Test
    void should_return_all_advisors() {
        // prepare
        List<Advisor> advisor_list = createDiverseMockAdvisorList();

        // arrange
        when(advisorRepository.findAllByOrderByRatingDesc()).thenReturn(advisor_list);

        // act
        List<AdvisorProfileDto> result = advisorService.getAllAdvisors();

        // assert
        assertThat(result).hasSize(2);

        AdvisorProfileDto firstAdvisor = result.get(0);
        assertThat(firstAdvisor.getId()).isEqualTo(1L);
        assertThat(firstAdvisor.getFirstName()).isEqualTo("John");
        assertThat(firstAdvisor.getLastName()).isEqualTo("Smith");
        assertThat(firstAdvisor.getBio()).isEqualTo("Senior Financial Advisor with expertise in wealth management");
        assertThat(firstAdvisor.getExperienceYears()).isEqualTo(8);
        assertThat(firstAdvisor.getAverageRating()).isEqualTo(4.8);
        assertThat(firstAdvisor.getProfileImageUrl()).isEqualTo("http://example.com/john_smith.jpg");
        assertThat(firstAdvisor.getSpecialties()).contains("Financial Planning", "Investment Advisory", "Retirement Planning");

        // 验证第二个顾问的信息
        AdvisorProfileDto secondAdvisor = result.get(1);
        assertThat(secondAdvisor.getId()).isEqualTo(2L);
        assertThat(secondAdvisor.getFirstName()).isEqualTo("Emily");
        assertThat(secondAdvisor.getLastName()).isEqualTo("Johnson");
        assertThat(secondAdvisor.getBio()).isEqualTo("Investment specialist focused on portfolio optimization");
        assertThat(secondAdvisor.getExperienceYears()).isEqualTo(5);
        assertThat(secondAdvisor.getAverageRating()).isEqualTo(4.6);

        // 验证Repository方法被调用
        verify(advisorRepository).findAllByOrderByRatingDesc();
    }

    @Test
    void should_return_advisor_with_required_id() {
        Advisor advisor = createMockAdvisor(2L, "Emily", "Johnson",
                "Investment specialist focused on portfolio optimization",
                5, 4.6, "http://example.com/emily_johnson.jpg",
                Set.of("Stock Investment", "Portfolio Management"));

        when(advisorRepository.findById(2L)).thenReturn(Optional.of(advisor));

        Advisor advisorById = advisorService.getAdvisorById(2L);
        assertThat(advisorById.getId()).isEqualTo(2L);
        assertThat(advisorById.getUser().getFirstName()).isEqualTo("Emily");
        assertThat(advisorById.getUser().getLastName()).isEqualTo("Johnson");
        assertThat(advisorById.getBio()).isEqualTo("Investment specialist focused on portfolio optimization");
        assertThat(advisorById.getExperienceYears()).isEqualTo(5);
        assertThat(advisorById.getAverageRating()).isEqualTo(4.6);
        verify(advisorRepository).findById(2L);
    }

    @Test
    void should_throw_exception_when_advisor_not_found_by_id() {
        // arrange
        when(advisorRepository.findById(999L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> advisorService.getAdvisorById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Advisor not found with id: 999");

        verify(advisorRepository).findById(999L);
    }

    @Test
    void should_return_advisor_profile_dto_by_id() {
        // arrange
        Advisor advisor = createMockAdvisor(1L, "John", "Smith",
                "Senior Financial Advisor", 8, 4.8,
                "http://example.com/john.jpg", Set.of("Finance"));
        advisor.setLanguages(Set.of("English", "Chinese"));

        when(advisorRepository.findById(1L)).thenReturn(Optional.of(advisor));

        // act
        AdvisorProfileDto result = advisorService.getAdvisorProfileDtoById(1L);

        // assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getBio()).isEqualTo("Senior Financial Advisor");
        assertThat(result.getExperienceYears()).isEqualTo(8);
        assertThat(result.getAverageRating()).isEqualTo(4.8);
        assertThat(result.getLanguages()).contains("English", "Chinese");

        verify(advisorRepository).findById(1L);
    }

    @Test
    void should_find_advisors_by_specialty() {
        // arrange
        List<Advisor> advisors = createDiverseMockAdvisorList();
        when(advisorRepository.findBySpecialty("Financial Planning")).thenReturn(advisors);

        // act
        List<Advisor> result = advisorService.findAdvisorsBySpecialty("Financial Planning");

        // assert
        assertThat(result).hasSize(2);
        verify(advisorRepository).findBySpecialty("Financial Planning");
    }

    @Test
    void should_find_advisors_by_language() {
        // arrange
        List<Advisor> advisors = createDiverseMockAdvisorList();
        when(advisorRepository.findByLanguage("English")).thenReturn(advisors);

        // act
        List<Advisor> result = advisorService.findAdvisorsByLanguage("English");

        // assert
        assertThat(result).hasSize(2);
        verify(advisorRepository).findByLanguage("English");
    }

    @Test
    void should_update_advisor_profile() {
        // arrange
        Advisor existingAdvisor = createMockAdvisor(1L, "John", "Smith",
                "Old bio", 5, 4.5, "old_url.jpg", Set.of("Old Specialty"));

        AdvisorProfileDto updateDto = new AdvisorProfileDto();
        updateDto.setProfessionalTitle("Senior Advisor");
        updateDto.setExperienceYears(10);
        updateDto.setBio("Updated bio");
        updateDto.setProfileImageUrl("new_url.jpg");
        updateDto.setSpecialties(Set.of("New Specialty", "Finance"));
        updateDto.setLanguages(Set.of("English", "Spanish"));

        when(advisorRepository.findById(1L)).thenReturn(Optional.of(existingAdvisor));
        when(advisorRepository.save(any(Advisor.class))).thenReturn(existingAdvisor);

        // act
        Advisor result = advisorService.updateAdvisorProfile(1L, updateDto);

        // assert
        assertThat(result.getProfessionalTitle()).isEqualTo("Senior Advisor");
        assertThat(result.getExperienceYears()).isEqualTo(10);
        assertThat(result.getBio()).isEqualTo("Updated bio");
        assertThat(result.getProfileImageUrl()).isEqualTo("new_url.jpg");
        assertThat(result.getSpecialties()).contains("New Specialty", "Finance");
        assertThat(result.getLanguages()).contains("English", "Spanish");

        verify(advisorRepository).findById(1L);
        verify(advisorRepository).save(existingAdvisor);
    }

    @Test
    void should_update_advisor_profile_with_null_values() {
        // arrange
        Advisor existingAdvisor = createMockAdvisor(1L, "John", "Smith",
                "Old bio", 5, 4.5, "old_url.jpg", Set.of("Old Specialty"));

        AdvisorProfileDto updateDto = new AdvisorProfileDto();
        // 所有字段都为null，不应该更新任何字段

        when(advisorRepository.findById(1L)).thenReturn(Optional.of(existingAdvisor));
        when(advisorRepository.save(any(Advisor.class))).thenReturn(existingAdvisor);

        // act
        Advisor result = advisorService.updateAdvisorProfile(1L, updateDto);

        // assert - 原始值应该保持不变
        assertThat(result.getBio()).isEqualTo("Old bio");
        assertThat(result.getExperienceYears()).isEqualTo(5);

        verify(advisorRepository).save(existingAdvisor);
    }

    @Test
    void should_add_availability_slot() {
        // arrange
        Advisor advisor = createMockAdvisor(1L, "John", "Smith", "Bio", 5, 4.5, "url", Set.of());

        AvailabilitySlotDto slotDto = new AvailabilitySlotDto();
        slotDto.setDayOfWeek(1); // Monday
        slotDto.setStartTime(LocalTime.of(9, 0));
        slotDto.setEndTime(LocalTime.of(17, 0));
        slotDto.setRecurring(true);

        AvailabilitySlot savedSlot = new AvailabilitySlot();
        savedSlot.setId(1L);

        when(advisorRepository.findById(1L)).thenReturn(Optional.of(advisor));
        when(availabilitySlotRepository.save(any(AvailabilitySlot.class))).thenReturn(savedSlot);

        // act
        AvailabilitySlot result = advisorService.addAvailabilitySlot(1L, slotDto);

        // assert
        assertThat(result.getId()).isEqualTo(1L);
        verify(advisorRepository).findById(1L);
        verify(availabilitySlotRepository).save(any(AvailabilitySlot.class));
    }

    @Test
    void should_remove_availability_slot() {
        // arrange
        when(availabilitySlotRepository.existsById(1L)).thenReturn(true);

        // act
        advisorService.removeAvailabilitySlot(1L);

        // assert
        verify(availabilitySlotRepository).existsById(1L);
        verify(availabilitySlotRepository).deleteById(1L);
    }

    @Test
    void should_throw_exception_when_removing_non_existent_slot() {
        // arrange
        when(availabilitySlotRepository.existsById(999L)).thenReturn(false);

        // act & assert
        assertThatThrownBy(() -> advisorService.removeAvailabilitySlot(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Availability slot not found with id: 999");

        verify(availabilitySlotRepository).existsById(999L);
        verify(availabilitySlotRepository, never()).deleteById(anyLong());
    }

    @Test
    void should_get_advisor_availability() {
        // arrange
        List<AvailabilitySlot> slots = new ArrayList<>();
        AvailabilitySlot slot1 = new AvailabilitySlot();
        slot1.setId(1L);
        slots.add(slot1);

        when(availabilitySlotRepository.findByAdvisorId(1L)).thenReturn(slots);

        // act
        List<AvailabilitySlot> result = advisorService.getAdvisorAvailability(1L);

        // assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(availabilitySlotRepository).findByAdvisorId(1L);
    }

    @Test
    void should_get_available_time_slots() {
        // arrange
        Long advisorId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 6); // Monday
        LocalDate endDate = LocalDate.of(2025, 1, 6);

        // Create availability slot for Monday 9:00-11:00
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setId(1L);
        slot.setDayOfWeek(1); // Monday
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(11, 0));
        slot.setRecurring(true);

        List<AvailabilitySlot> availabilitySlots = List.of(slot);

        // Create existing appointment 9:30-10:00
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(LocalDateTime.of(2025, 1, 6, 9, 30));
        appointment.setDurationMinutes(30);

        List<Appointment> existingAppointments = List.of(appointment);

        when(availabilitySlotRepository.findAvailableSlotsByDateRange(advisorId, startDate, endDate))
                .thenReturn(availabilitySlots);
        when(appointmentRepository.findByAdvisorIdAndAppointmentDateBetween(
                eq(advisorId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(existingAppointments);

        // act
        List<Map<String, Object>> result = advisorService.getAvailableTimeSlots(advisorId, startDate, endDate);

        // assert
        // Should have 3 slots: 9:00-9:30, 10:00-10:30, 10:30-11:00
        // 9:30-10:00 should be excluded due to existing appointment
        assertThat(result).hasSize(3);

        // Check first available slot
        Map<String, Object> firstSlot = result.get(0);
        assertThat(firstSlot.get("date")).isEqualTo("2025-01-06");
        assertThat(firstSlot.get("startTime")).isEqualTo("09:00");
        assertThat(firstSlot.get("endTime")).isEqualTo("09:30");

        verify(availabilitySlotRepository).findAvailableSlotsByDateRange(advisorId, startDate, endDate);
        verify(appointmentRepository).findByAdvisorIdAndAppointmentDateBetween(
                eq(advisorId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void should_get_advisor_by_user_id() {
        // arrange
        Advisor advisor = createMockAdvisor(1L, "John", "Smith", "Bio", 5, 4.5, "url", Set.of());
        when(advisorRepository.findByUserId(1L)).thenReturn(Optional.of(advisor));

        // act
        Advisor result = advisorService.getAdvisorByUserId(1L);

        // assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUser().getFirstName()).isEqualTo("John");
        verify(advisorRepository).findByUserId(1L);
    }

    @Test
    void should_throw_exception_when_advisor_not_found_by_user_id() {
        // arrange
        when(advisorRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> advisorService.getAdvisorByUserId(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No advisor found for user ID: 999");

        verify(advisorRepository).findByUserId(999L);
    }

    @Test
    void should_return_empty_list_when_no_advisors_exist() {
        // arrange
        when(advisorRepository.findAllByOrderByRatingDesc()).thenReturn(new ArrayList<>());

        // act
        List<AdvisorProfileDto> result = advisorService.getAllAdvisors();

        // assert
        assertThat(result).isEmpty();
        verify(advisorRepository).findAllByOrderByRatingDesc();
    }

    @Test
    void should_return_empty_list_for_available_time_slots_when_no_availability() {
        // arrange
        Long advisorId = 1L;
        LocalDate startDate = LocalDate.of(2025, 1, 6);
        LocalDate endDate = LocalDate.of(2025, 1, 6);

        when(availabilitySlotRepository.findAvailableSlotsByDateRange(advisorId, startDate, endDate))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findByAdvisorIdAndAppointmentDateBetween(
                eq(advisorId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // act
        List<Map<String, Object>> result = advisorService.getAvailableTimeSlots(advisorId, startDate, endDate);

        // assert
        assertThat(result).isEmpty();
    }
}