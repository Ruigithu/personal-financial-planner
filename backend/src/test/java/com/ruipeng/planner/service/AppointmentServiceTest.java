package com.ruipeng.planner.service;


import com.ruipeng.planner.dto.AppointmentCreateDto;
import com.ruipeng.planner.dto.AppointmentDetailsDto;
import com.ruipeng.planner.dto.OAuthSuccessEvent;
import com.ruipeng.planner.entity.*;
import com.ruipeng.planner.repository.AdvisorRepository;
import com.ruipeng.planner.repository.AppointmentRepository;
import com.ruipeng.planner.repository.FinancialPlanRepository;
import com.ruipeng.planner.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdvisorRepository advisorRepository;

    @Mock
    private FinancialPlanRepository financialPlanRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private GoogleCalendarService googleCalendarService;

    @Mock
    private EmailInvitationService emailInvitationService;

    @Mock
    private GoogleOAuthService googleOAuthService;

    @InjectMocks
    private AppointmentService appointmentService;

    private User mockUser;
    private Advisor mockAdvisor;
    private Appointment mockAppointment;
    private FinancialPlan mockFinancialPlan;

    @BeforeEach
    void setUp() {
        // Create mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setEmail("john.doe@example.com");
        mockUser.setRole(UserRole.USER);
        mockUser.setStatus(AccountStatus.ACTIVE);

        // Create mock advisor user
        User advisorUser = new User();
        advisorUser.setId(2L);
        advisorUser.setFirstName("Jane");
        advisorUser.setLastName("Smith");
        advisorUser.setEmail("jane.smith@example.com");
        advisorUser.setRole(UserRole.ADVISOR);

        // Create mock advisor
        mockAdvisor = new Advisor();
        mockAdvisor.setId(1L);
        mockAdvisor.setUser(advisorUser);
        mockAdvisor.setBio("Senior Financial Advisor");
        mockAdvisor.setExperienceYears(8);

        // Create mock appointment
        mockAppointment = new Appointment();
        mockAppointment.setId(1L);
        mockAppointment.setUser(mockUser);
        mockAppointment.setAdvisor(mockAdvisor);
        mockAppointment.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 10, 0));
        mockAppointment.setDurationMinutes(60);
        mockAppointment.setSessionType(SessionType.INITIAL_CONSULTATION);
        mockAppointment.setStatus(AppointmentStatus.CONFIRMED);
        mockAppointment.setBookingDate(LocalDateTime.now());
        mockAppointment.setUserNotes("Initial consultation");

        // Create mock financial plan
        mockFinancialPlan = new FinancialPlan();
        mockFinancialPlan.setId(1L);
        mockFinancialPlan.setUser(mockUser);
        mockFinancialPlan.setPlanName("Retirement Plan");
    }

    @Test
    void should_get_user_appointments() {
        // arrange
        List<Appointment> appointments = List.of(mockAppointment);
        when(appointmentRepository.findByUserId(1L)).thenReturn(appointments);

        // act
        List<AppointmentDetailsDto> result = appointmentService.getUserAppointments(1L);

        // assert
        assertThat(result).hasSize(1);
        AppointmentDetailsDto dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getAdvisorFirstName()).isEqualTo("Jane");
        assertThat(dto.getAdvisorLastName()).isEqualTo("Smith");
        assertThat(dto.getSessionType()).isEqualTo(SessionType.INITIAL_CONSULTATION+"");
        assertThat(dto.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED+"");

        verify(appointmentRepository).findByUserId(1L);
    }

    @Test
    void should_get_advisor_appointments() {
        // arrange
        List<Appointment> appointments = List.of(mockAppointment);
        when(appointmentRepository.findByAdvisorId(1L)).thenReturn(appointments);

        // act
        List<Appointment> result = appointmentService.getAdvisorAppointments(1L);

        // assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(appointmentRepository).findByAdvisorId(1L);
    }

    @Test
    void should_get_appointment_by_id() {
        // arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));

        // act
        Appointment result = appointmentService.getAppointmentById(1L);

        // assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUser().getFirstName()).isEqualTo("John");
        verify(appointmentRepository).findById(1L);
    }

    @Test
    void should_throw_exception_when_appointment_not_found() {
        // arrange
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> appointmentService.getAppointmentById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Appointment not found with id: 999");

        verify(appointmentRepository).findById(999L);
    }

    @Test
    void should_create_appointment_successfully() {
        // arrange
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setAdvisorId(1L);
        createDto.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 14, 0));
        createDto.setDurationMinutes(60);
        createDto.setSessionType(SessionType.INITIAL_CONSULTATION);
        createDto.setUserNotes("Test consultation");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(advisorRepository.findById(1L)).thenReturn(Optional.of(mockAdvisor));
        when(appointmentRepository.findByAdvisorIdAndAppointmentDateBetween(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>()); // No conflicting appointments
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);
        when(googleOAuthService.isUserAuthorized(1L)).thenReturn(false);

        // act
        AppointmentCreateDto result = appointmentService.createAppointment(createDto, 1L);

        // assert
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
        verify(advisorRepository).findById(1L);
        verify(appointmentRepository, times(1)).save(any(Appointment.class)); // Save twice: initial save + meeting link update
    }

    @Test
    void should_create_appointment_with_financial_plan() {
        // arrange
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setAdvisorId(1L);
        createDto.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 14, 0));
        createDto.setDurationMinutes(60);
        createDto.setSessionType(SessionType.FOLLOWUP_SESSION);
        createDto.setSharedPlanId(1L);
        createDto.setUserNotes("Plan review");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(advisorRepository.findById(1L)).thenReturn(Optional.of(mockAdvisor));
        when(financialPlanRepository.findById(1L)).thenReturn(Optional.of(mockFinancialPlan));
        when(appointmentRepository.findByAdvisorIdAndAppointmentDateBetween(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);
        when(googleOAuthService.isUserAuthorized(1L)).thenReturn(false);

        // act
        AppointmentCreateDto result = appointmentService.createAppointment(createDto, 1L);

        // assert
        assertThat(result.getId()).isEqualTo(1L);
        verify(financialPlanRepository).findById(1L);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void should_throw_exception_when_user_not_found_for_appointment() {
        // arrange
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setAdvisorId(1L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> appointmentService.createAppointment(createDto, 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
    }

    @Test
    void should_throw_exception_when_advisor_not_found_for_appointment() {
        // arrange
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setAdvisorId(999L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(advisorRepository.findById(999L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> appointmentService.createAppointment(createDto, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Advisor not found with id: 999");

        verify(advisorRepository).findById(999L);
    }

    @Test
    void should_throw_exception_when_time_slot_not_available() {
        // arrange
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setAdvisorId(1L);
        createDto.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 14, 0));
        createDto.setDurationMinutes(60);

        Appointment conflictingAppointment = new Appointment();
        conflictingAppointment.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(advisorRepository.findById(1L)).thenReturn(Optional.of(mockAdvisor));
        when(appointmentRepository.findByAdvisorIdAndAppointmentDateBetween(anyLong(), any(), any()))
                .thenReturn(List.of(conflictingAppointment)); // Conflicting appointment exists

        // act & assert
        assertThatThrownBy(() -> appointmentService.createAppointment(createDto, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Selected time slot is no longer available");
    }

    @Test
    void should_throw_exception_when_financial_plan_not_found() {
        // arrange
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setAdvisorId(1L);
        createDto.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 14, 0));
        createDto.setDurationMinutes(60);
        createDto.setSharedPlanId(999L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(advisorRepository.findById(1L)).thenReturn(Optional.of(mockAdvisor));
        when(appointmentRepository.findByAdvisorIdAndAppointmentDateBetween(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(financialPlanRepository.findById(999L)).thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> appointmentService.createAppointment(createDto, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Financial plan not found with id: 999");
    }

    @Test
    void should_update_appointment_status_to_confirmed() {
        // arrange
        mockAppointment.setStatus(AppointmentStatus.CONFIRMED);
        mockAppointment.setMeetingLink(null);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        // act
        Appointment result = appointmentService.updateAppointmentStatus(1L, AppointmentStatus.CONFIRMED);

        // assert
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(result.getMeetingLink()).isNotNull();
        verify(emailService).sendAppointmentConfirmation(mockAppointment);
        verify(appointmentRepository).save(mockAppointment);
    }

    @Test
    void should_update_appointment_status_to_cancelled() {
        // arrange
        mockAppointment.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        // act
        Appointment result = appointmentService.updateAppointmentStatus(1L, AppointmentStatus.CANCELLED);

        // assert
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(emailService).sendAppointmentCancellation(mockAppointment);
        verify(appointmentRepository).save(mockAppointment);
    }


    @Test
    void should_add_advisor_notes_to_appointment() {
        // arrange
        String notes = "Patient showed good understanding of financial concepts";
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        // act
        Appointment result = appointmentService.addAdvisorNotesToAppointment(1L, notes);

        // assert
        assertThat(result.getAdvisorNotes()).isEqualTo(notes);
        verify(appointmentRepository).save(mockAppointment);
    }

    @Test
    void should_generate_meeting_link_successfully() {
        // arrange
        mockAppointment.setMeetingLink(null);
        String expectedMeetingLink = "https://calendar.google.com/meet/abc-123";

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        when(googleCalendarService.createAppointmentEvent(mockAppointment, 1L))
                .thenReturn(expectedMeetingLink);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        // act
        appointmentService.generateMeetingLink(1L, 1L);

        // assert
        verify(googleCalendarService).createAppointmentEvent(mockAppointment, 1L);
        verify(emailInvitationService).sendMeetInvitation(mockAppointment, expectedMeetingLink);
        verify(appointmentRepository).save(mockAppointment);
    }

    @Test
    void should_throw_security_exception_when_user_not_authorized_for_meeting_link() {
        // arrange
        User otherUser = new User();
        otherUser.setId(999L);
        mockAppointment.setUser(otherUser);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));

        // act & assert
        assertThatThrownBy(() -> appointmentService.generateMeetingLink(1L, 1L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("User not authorized to access this appointment");
    }

    @Test
    void should_not_generate_meeting_link_when_already_exists() {
        // arrange
        mockAppointment.setMeetingLink("https://existing-meeting.com");

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));

        // act
        appointmentService.generateMeetingLink(1L, 1L);

        // assert
        verify(googleCalendarService, never()).createAppointmentEvent(any(), anyLong());
        verify(emailInvitationService, never()).sendMeetInvitation(any(), anyString());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void should_throw_runtime_exception_when_meeting_link_generation_fails() {
        // arrange
        mockAppointment.setMeetingLink(null);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        when(googleCalendarService.createAppointmentEvent(mockAppointment, 1L))
                .thenThrow(new RuntimeException("Google Calendar API error"));

        // act & assert
        assertThatThrownBy(() -> appointmentService.generateMeetingLink(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to generate meeting link");
    }

    @Test
    void should_handle_oauth_success_event() {
        // arrange
        OAuthSuccessEvent event = new OAuthSuccessEvent(this, 1L, 1L);
        mockAppointment.setMeetingLink(null);
        String expectedMeetingLink = "https://calendar.google.com/meet/xyz-789";

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        when(googleCalendarService.createAppointmentEvent(mockAppointment, 1L))
                .thenReturn(expectedMeetingLink);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        // act
        appointmentService.handleOAuthSuccessEvent(event);

        // assert
        verify(googleCalendarService).createAppointmentEvent(mockAppointment, 1L);
        verify(emailInvitationService).sendMeetInvitation(mockAppointment, expectedMeetingLink);
    }

    @Test
    void should_handle_oauth_success_event_with_exception() {
        // arrange
        OAuthSuccessEvent event = new OAuthSuccessEvent(this, 1L, 1L);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(mockAppointment));
        when(googleCalendarService.createAppointmentEvent(mockAppointment, 1L))
                .thenThrow(new RuntimeException("OAuth error"));

        // act & assert
        // Should not throw exception, just log error
        appointmentService.handleOAuthSuccessEvent(event);

        verify(googleCalendarService).createAppointmentEvent(mockAppointment, 1L);
    }

    @Test
    void should_return_empty_list_when_user_has_no_appointments() {
        // arrange
        when(appointmentRepository.findByUserId(1L)).thenReturn(new ArrayList<>());

        // act
        List<AppointmentDetailsDto> result = appointmentService.getUserAppointments(1L);

        // assert
        assertThat(result).isEmpty();
        verify(appointmentRepository).findByUserId(1L);
    }

    @Test
    void should_return_empty_list_when_advisor_has_no_appointments() {
        // arrange
        when(appointmentRepository.findByAdvisorId(1L)).thenReturn(new ArrayList<>());

        // act
        List<Appointment> result = appointmentService.getAdvisorAppointments(1L);

        // assert
        assertThat(result).isEmpty();
        verify(appointmentRepository).findByAdvisorId(1L);
    }

    @Test
    void should_create_appointment_with_oauth_authorized_user() {
        // arrange
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setAdvisorId(1L);
        createDto.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 14, 0));
        createDto.setDurationMinutes(60);
        createDto.setSessionType(SessionType.INITIAL_CONSULTATION);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(advisorRepository.findById(1L)).thenReturn(Optional.of(mockAdvisor));
        when(appointmentRepository.findByAdvisorIdAndAppointmentDateBetween(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);
        when(googleOAuthService.isUserAuthorized(1L)).thenReturn(true);

        // Mock the generateMeetingLink call within createAppointment
        mockAppointment.setMeetingLink(null);
        String expectedMeetingLink = "https://calendar.google.com/meet/create-123";
        when(googleCalendarService.createAppointmentEvent(mockAppointment, 1L))
                .thenReturn(expectedMeetingLink);

        // act
        AppointmentCreateDto result = appointmentService.createAppointment(createDto, 1L);

        // assert
        assertThat(result.getId()).isEqualTo(1L);
        verify(googleOAuthService).isUserAuthorized(1L);
        // The method calls generateMeetingLink internally when user is authorized
        verify(googleCalendarService).createAppointmentEvent(mockAppointment, 1L);
    }
}
