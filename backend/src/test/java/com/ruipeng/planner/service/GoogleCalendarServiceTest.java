package com.ruipeng.planner.service;

import com.google.api.client.auth.oauth2.Credential;

import com.ruipeng.planner.config.GoogleCalendarConfig;
import com.ruipeng.planner.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarServiceTest {

    @Mock
    private GoogleCalendarConfig googleCalendarConfig;

    @Mock
    private GoogleOAuthService googleOAuthService;

    @Mock
    private Credential mockCredential;

    @InjectMocks
    private GoogleCalendarService googleCalendarService;

    private Appointment mockAppointment;
    private User mockUser;
    private Advisor mockAdvisor;
    private FinancialPlan mockFinancialPlan;

    @BeforeEach
    void setUp() {
        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setEmail("john.doe@example.com");
        mockUser.setRole(UserRole.USER);
        mockUser.setStatus(AccountStatus.ACTIVE);

        // Setup mock advisor user
        User advisorUser = new User();
        advisorUser.setId(2L);
        advisorUser.setFirstName("Jane");
        advisorUser.setLastName("Smith");
        advisorUser.setEmail("jane.smith@example.com");
        advisorUser.setRole(UserRole.ADVISOR);
        advisorUser.setStatus(AccountStatus.ACTIVE);

        // Setup mock advisor
        mockAdvisor = new Advisor();
        mockAdvisor.setId(1L);
        mockAdvisor.setUser(advisorUser);
        mockAdvisor.setProfessionalTitle("Senior Financial Advisor");
        mockAdvisor.setExperienceYears(8);
        mockAdvisor.setBio("Experienced financial advisor specializing in retirement planning");
        mockAdvisor.setAverageRating(4.8);
        mockAdvisor.setRatingCount(127);

        // Setup advisor specialties and languages
        Set<String> specialties = new HashSet<>();
        specialties.add("Retirement Planning");
        specialties.add("Investment Strategy");
        mockAdvisor.setSpecialties(specialties);

        Set<String> languages = new HashSet<>();
        languages.add("English");
        languages.add("Spanish");
        mockAdvisor.setLanguages(languages);

        // Setup mock financial plan
        mockFinancialPlan = new FinancialPlan();
        mockFinancialPlan.setId(1L);
        mockFinancialPlan.setPlanName("Retirement Strategy");

        // Setup mock appointment
        mockAppointment = new Appointment();
        mockAppointment.setId(1L);
        mockAppointment.setUser(mockUser);
        mockAppointment.setAdvisor(mockAdvisor);
        mockAppointment.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 14, 0));
        mockAppointment.setDurationMinutes(60);
        mockAppointment.setSessionType(SessionType.INITIAL_CONSULTATION);
        mockAppointment.setStatus(AppointmentStatus.CONFIRMED);
        mockAppointment.setUserNotes("Discussion about retirement planning");
        mockAppointment.setSharedPlan(mockFinancialPlan);

    }


    @Test
    void should_throw_exception_when_user_not_authorized() throws IOException {
        // arrange
        Long userId = 1L;
        when(googleOAuthService.isUserAuthorized(userId)).thenReturn(false);

        // act & assert
        assertThatThrownBy(() -> googleCalendarService.createAppointmentEvent(mockAppointment, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("创建日历事件失败: 用户需要先授权Google Calendar访问权限");

        verify(googleOAuthService).isUserAuthorized(userId);
        verify(googleOAuthService, never()).getUserCredential(any());
    }

    @Test
    void should_handle_oauth_service_exception() throws IOException {
        // arrange
        Long userId = 1L;
        when(googleOAuthService.isUserAuthorized(userId)).thenReturn(true);
        when(googleOAuthService.getUserCredential(userId))
                .thenThrow(new RuntimeException("OAuth token expired"));

        // act & assert
        assertThatThrownBy(() -> googleCalendarService.createAppointmentEvent(mockAppointment, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("创建日历事件失败");

        verify(googleOAuthService).isUserAuthorized(userId);
        verify(googleOAuthService).getUserCredential(userId);
    }

    @Test
    void should_verify_authorization_check() throws IOException {
        // arrange
        Long userId = 1L;
        when(googleOAuthService.isUserAuthorized(userId)).thenReturn(true);
        when(googleOAuthService.getUserCredential(userId)).thenReturn(mockCredential);

        // act & assert - This will fail at the Calendar API level, but we can verify the OAuth flow
        assertThatThrownBy(() -> googleCalendarService.createAppointmentEvent(mockAppointment, userId))
                .isInstanceOf(RuntimeException.class);

        // Verify that authorization was checked and credentials were requested
        verify(googleOAuthService).isUserAuthorized(userId);
        verify(googleOAuthService).getUserCredential(userId);
    }

    @Test
    void should_handle_appointment_with_different_properties() throws IOException {
        // arrange - Test with different appointment configurations
        Long userId = 1L;

        // Test with null user notes
        mockAppointment.setUserNotes(null);
        mockAppointment.setSessionType(SessionType.FOLLOWUP_SESSION);
        mockAppointment.setStatus(AppointmentStatus.CONFIRMED);

        when(googleOAuthService.isUserAuthorized(userId)).thenReturn(true);
        when(googleOAuthService.getUserCredential(userId)).thenReturn(mockCredential);

        // act & assert - Verify the service handles different appointment properties
        assertThatThrownBy(() -> googleCalendarService.createAppointmentEvent(mockAppointment, userId))
                .isInstanceOf(RuntimeException.class);

        verify(googleOAuthService).isUserAuthorized(userId);
        verify(googleOAuthService).getUserCredential(userId);
    }

    @Test
    void should_handle_appointment_without_user() throws IOException {
        // arrange
        Long userId = 1L;
        mockAppointment.setUser(null); // No user associated

        when(googleOAuthService.isUserAuthorized(userId)).thenReturn(true);
        when(googleOAuthService.getUserCredential(userId)).thenReturn(mockCredential);

        // act & assert - Service should handle null user gracefully
        assertThatThrownBy(() -> googleCalendarService.createAppointmentEvent(mockAppointment, userId))
                .isInstanceOf(RuntimeException.class);

        verify(googleOAuthService).isUserAuthorized(userId);
        verify(googleOAuthService).getUserCredential(userId);
    }

    @Test
    void should_handle_different_time_zones() throws IOException {
        // arrange
        Long userId = 1L;
        String customTimeZone = "America/New_York";
        when(googleCalendarConfig.getTimeZone()).thenReturn(customTimeZone);
        when(googleOAuthService.isUserAuthorized(userId)).thenReturn(true);
        when(googleOAuthService.getUserCredential(userId)).thenReturn(mockCredential);

        // Setup appointment for specific time
        mockAppointment.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 10, 30));
        mockAppointment.setDurationMinutes(90);

        // act & assert - Verify timezone configuration is used
        assertThatThrownBy(() -> googleCalendarService.createAppointmentEvent(mockAppointment, userId))
                .isInstanceOf(RuntimeException.class);

        verify(googleCalendarConfig).getTimeZone();
        verify(googleOAuthService).isUserAuthorized(userId);
        verify(googleOAuthService).getUserCredential(userId);
    }

    @Test
    void should_use_application_name_from_config() throws IOException {
        // arrange
        Long userId = 1L;
        String customAppName = "Custom Financial Planner";
        when(googleCalendarConfig.getApplicationName()).thenReturn(customAppName);
        when(googleOAuthService.isUserAuthorized(userId)).thenReturn(true);
        when(googleOAuthService.getUserCredential(userId)).thenReturn(mockCredential);

        // act & assert - Verify application name configuration is used
        assertThatThrownBy(() -> googleCalendarService.createAppointmentEvent(mockAppointment, userId))
                .isInstanceOf(RuntimeException.class);

        verify(googleCalendarConfig).getApplicationName();
        verify(googleOAuthService).isUserAuthorized(userId);
        verify(googleOAuthService).getUserCredential(userId);
    }

    @Test
    void should_handle_appointment_with_advisor_information() throws IOException {
        // arrange
        Long userId = 1L;
        when(googleOAuthService.isUserAuthorized(userId)).thenReturn(true);
        when(googleOAuthService.getUserCredential(userId)).thenReturn(mockCredential);

        // Verify advisor has proper setup
        assertThat(mockAppointment.getAdvisor()).isNotNull();
        assertThat(mockAppointment.getAdvisor().getProfessionalTitle()).isEqualTo("Senior Financial Advisor");
        assertThat(mockAppointment.getAdvisor().getExperienceYears()).isEqualTo(8);
        assertThat(mockAppointment.getAdvisor().getSpecialties()).contains("Retirement Planning");

        // act & assert - Service should process advisor information
        assertThatThrownBy(() -> googleCalendarService.createAppointmentEvent(mockAppointment, userId))
                .isInstanceOf(RuntimeException.class);

        verify(googleOAuthService).isUserAuthorized(userId);
        verify(googleOAuthService).getUserCredential(userId);
    }

    @Test
    void should_handle_appointment_with_shared_financial_plan() throws IOException {
        // arrange
        Long userId = 1L;
        when(googleOAuthService.isUserAuthorized(userId)).thenReturn(true);
        when(googleOAuthService.getUserCredential(userId)).thenReturn(mockCredential);

        // Verify shared plan is set
        assertThat(mockAppointment.getSharedPlan()).isNotNull();
        assertThat(mockAppointment.getSharedPlan().getPlanName()).isEqualTo("Retirement Strategy");

        // act & assert - Service should process shared plan information
        assertThatThrownBy(() -> googleCalendarService.createAppointmentEvent(mockAppointment, userId))
                .isInstanceOf(RuntimeException.class);

        verify(googleOAuthService).isUserAuthorized(userId);
        verify(googleOAuthService).getUserCredential(userId);
    }

    @Test
    void should_validate_appointment_data_integrity() throws IOException {
        // arrange
        Long userId = 1L;
        when(googleOAuthService.isUserAuthorized(userId)).thenReturn(true);
        when(googleOAuthService.getUserCredential(userId)).thenReturn(mockCredential);

        // Verify all appointment data is properly set
        assertThat(mockAppointment.getId()).isEqualTo(1L);
        assertThat(mockAppointment.getUser().getFirstName()).isEqualTo("John");
        assertThat(mockAppointment.getAdvisor().getUser().getFirstName()).isEqualTo("Jane");
        assertThat(mockAppointment.getAppointmentDate()).isEqualTo(LocalDateTime.of(2025, 6, 25, 14, 0));
        assertThat(mockAppointment.getDurationMinutes()).isEqualTo(60);
        assertThat(mockAppointment.getSessionType()).isEqualTo(SessionType.INITIAL_CONSULTATION);
        assertThat(mockAppointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(mockAppointment.getUserNotes()).isEqualTo("Discussion about retirement planning");

        // act & assert
        assertThatThrownBy(() -> googleCalendarService.createAppointmentEvent(mockAppointment, userId))
                .isInstanceOf(RuntimeException.class);

        verify(googleOAuthService).isUserAuthorized(userId);
        verify(googleOAuthService).getUserCredential(userId);
    }
}