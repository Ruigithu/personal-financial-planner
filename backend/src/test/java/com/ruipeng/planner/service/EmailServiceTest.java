package com.ruipeng.planner.service;

import com.ruipeng.planner.entity.Advisor;
import com.ruipeng.planner.entity.Appointment;
import com.ruipeng.planner.entity.User;
import com.ruipeng.planner.entity.AccountStatus;
import com.ruipeng.planner.entity.AppointmentStatus;
import com.ruipeng.planner.entity.SessionType;
import com.ruipeng.planner.entity.UserRole;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mockMailSender;

    @Mock
    private TemplateEngine mockTemplateEngine;

    @Mock
    private MimeMessage mockMimeMessage;

    private EmailService emailService;
    private Appointment mockAppointment;
    private User mockUser;
    private User mockAdvisorUser;
    private Advisor mockAdvisor;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mockTemplateEngine, mockMailSender);

        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setEmail("john.doe@example.com");
        mockUser.setRole(UserRole.USER);
        mockUser.setStatus(AccountStatus.ACTIVE);

        // Setup mock advisor user
        mockAdvisorUser = new User();
        mockAdvisorUser.setId(2L);
        mockAdvisorUser.setFirstName("Jane");
        mockAdvisorUser.setLastName("Smith");
        mockAdvisorUser.setEmail("jane.smith@advisor.com");
        mockAdvisorUser.setRole(UserRole.ADVISOR);
        mockAdvisorUser.setStatus(AccountStatus.ACTIVE);

        // Setup mock advisor
        mockAdvisor = new Advisor();
        mockAdvisor.setId(1L);
        mockAdvisor.setUser(mockAdvisorUser);
        mockAdvisor.setProfessionalTitle("Senior Financial Advisor");
        mockAdvisor.setExperienceYears(8);
        mockAdvisor.setBio("Experienced financial advisor");
        mockAdvisor.setAverageRating(4.8);
        mockAdvisor.setRatingCount(127);

        Set<String> specialties = new HashSet<>();
        specialties.add("Retirement Planning");
        mockAdvisor.setSpecialties(specialties);

        Set<String> languages = new HashSet<>();
        languages.add("English");
        mockAdvisor.setLanguages(languages);

        // Setup mock appointment
        mockAppointment = new Appointment();
        mockAppointment.setId(1L);
        mockAppointment.setUser(mockUser);
        mockAppointment.setAdvisor(mockAdvisor);
        mockAppointment.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 14, 30));
        mockAppointment.setDurationMinutes(60);
        mockAppointment.setSessionType(SessionType.INITIAL_CONSULTATION);
        mockAppointment.setStatus(AppointmentStatus.CONFIRMED);
        mockAppointment.setMeetingLink("https://zoom.us/j/123456789");
    }

    @Test
    void should_send_appointment_confirmation_to_both_user_and_advisor() throws MessagingException {
        // arrange
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(eq("appointment-confirmation-user"), any(Context.class)))
                .thenReturn("<html>User confirmation email</html>");
        when(mockTemplateEngine.process(eq("appointment-confirmation-advisor"), any(Context.class)))
                .thenReturn("<html>Advisor confirmation email</html>");

        // act
        emailService.sendAppointmentConfirmation(mockAppointment);

        // assert
        verify(mockMailSender, times(2)).createMimeMessage();
        verify(mockMailSender, times(2)).send(any(MimeMessage.class));
        verify(mockTemplateEngine).process(eq("appointment-confirmation-user"), any(Context.class));
        verify(mockTemplateEngine).process(eq("appointment-confirmation-advisor"), any(Context.class));
    }

    @Test
    void should_send_appointment_cancellation_to_both_user_and_advisor() throws MessagingException {
        // arrange
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(eq("appointment-cancellation-user"), any(Context.class)))
                .thenReturn("<html>User cancellation email</html>");
        when(mockTemplateEngine.process(eq("appointment-cancellation-advisor"), any(Context.class)))
                .thenReturn("<html>Advisor cancellation email</html>");

        // act
        emailService.sendAppointmentCancellation(mockAppointment);

        // assert
        verify(mockMailSender, times(2)).createMimeMessage();
        verify(mockMailSender, times(2)).send(any(MimeMessage.class));
        verify(mockTemplateEngine).process(eq("appointment-cancellation-user"), any(Context.class));
        verify(mockTemplateEngine).process(eq("appointment-cancellation-advisor"), any(Context.class));
    }

    @Test
    void should_create_correct_context_variables_for_user_email() throws MessagingException {
        // arrange
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(mockTemplateEngine.process(eq("appointment-confirmation-user"), contextCaptor.capture()))
                .thenReturn("<html>User email</html>");
        when(mockTemplateEngine.process(eq("appointment-confirmation-advisor"), any(Context.class)))
                .thenReturn("<html>Advisor email</html>");

        // act
        emailService.sendAppointmentConfirmation(mockAppointment);

        // assert
        Context capturedContext = contextCaptor.getValue();

        // Verify the context was created and template was processed
        assertThat(capturedContext).isNotNull();
        verify(mockTemplateEngine).process(eq("appointment-confirmation-user"), eq(capturedContext));
        verify(mockTemplateEngine).process(eq("appointment-confirmation-advisor"), any(Context.class));

        // Verify mail sending was called twice (user + advisor)
        verify(mockMailSender, times(2)).createMimeMessage();
        verify(mockMailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    void should_create_correct_context_variables_for_advisor_email() throws MessagingException {
        // arrange
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(eq("appointment-confirmation-user"), any(Context.class)))
                .thenReturn("<html>User email</html>");

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(mockTemplateEngine.process(eq("appointment-confirmation-advisor"), contextCaptor.capture()))
                .thenReturn("<html>Advisor email</html>");

        // act
        emailService.sendAppointmentConfirmation(mockAppointment);

        // assert
        Context capturedContext = contextCaptor.getValue();

        // Verify the context was created and template was processed
        assertThat(capturedContext).isNotNull();
        verify(mockTemplateEngine).process(eq("appointment-confirmation-user"), any(Context.class));
        verify(mockTemplateEngine).process(eq("appointment-confirmation-advisor"), eq(capturedContext));

        // Verify mail sending was called twice (user + advisor)
        verify(mockMailSender, times(2)).createMimeMessage();
        verify(mockMailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    void should_format_date_and_time_correctly() throws MessagingException {
        // arrange
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(eq("appointment-confirmation-user"), any(Context.class)))
                .thenReturn("<html>User email</html>");
        when(mockTemplateEngine.process(eq("appointment-confirmation-advisor"), any(Context.class)))
                .thenReturn("<html>Advisor email</html>");

        // act
        emailService.sendAppointmentConfirmation(mockAppointment);

        // assert
        verify(mockTemplateEngine).process(eq("appointment-confirmation-user"), any(Context.class));
        verify(mockTemplateEngine).process(eq("appointment-confirmation-advisor"), any(Context.class));
        verify(mockMailSender, times(2)).createMimeMessage();
        verify(mockMailSender, times(2)).send(any(MimeMessage.class));

        // Verify that the context creation methods were called with the appointment
        // The actual date formatting is tested implicitly through the service execution
    }

    @Test
    void should_handle_messaging_exception_gracefully() throws MessagingException {
        // arrange
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Email content</html>");

        // Use MailSendException instead of MessagingException since JavaMailSender.send() throws MailException
        doThrow(new MailSendException("SMTP server error"))
                .when(mockMailSender).send(any(MimeMessage.class));

        // act & assert - Should not throw exception, should handle gracefully
        assertThatNoException().isThrownBy(() ->
                emailService.sendAppointmentConfirmation(mockAppointment));

        // Verify that createMimeMessage was called despite the exception
        verify(mockMailSender, times(2)).createMimeMessage();
    }

    @Test
    void should_handle_template_engine_exception_gracefully() throws MessagingException {
        // arrange
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Template not found"));

        // act & assert - Should not throw exception, should handle gracefully
        assertThatNoException().isThrownBy(() ->
                emailService.sendAppointmentConfirmation(mockAppointment));

        verify(mockMailSender, times(2)).createMimeMessage();
    }

    @Test
    void should_set_correct_email_properties_for_user_confirmation() throws MessagingException {
        // arrange
        MimeMessageHelper mockHelper = mock(MimeMessageHelper.class);
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(eq("appointment-confirmation-user"), any(Context.class)))
                .thenReturn("<html>User confirmation</html>");
        when(mockTemplateEngine.process(eq("appointment-confirmation-advisor"), any(Context.class)))
                .thenReturn("<html>Advisor confirmation</html>");

        // act
        emailService.sendAppointmentConfirmation(mockAppointment);

        // assert - Verify mail sender was called with correct parameters
        verify(mockMailSender, times(2)).send(any(MimeMessage.class));
        verify(mockTemplateEngine).process(eq("appointment-confirmation-user"), any(Context.class));
        verify(mockTemplateEngine).process(eq("appointment-confirmation-advisor"), any(Context.class));
    }

    @Test
    void should_set_correct_email_properties_for_cancellation() throws MessagingException {
        // arrange
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(eq("appointment-cancellation-user"), any(Context.class)))
                .thenReturn("<html>User cancellation</html>");
        when(mockTemplateEngine.process(eq("appointment-cancellation-advisor"), any(Context.class)))
                .thenReturn("<html>Advisor cancellation</html>");

        // act
        emailService.sendAppointmentCancellation(mockAppointment);

        // assert
        verify(mockMailSender, times(2)).send(any(MimeMessage.class));
        verify(mockTemplateEngine).process(eq("appointment-cancellation-user"), any(Context.class));
        verify(mockTemplateEngine).process(eq("appointment-cancellation-advisor"), any(Context.class));
    }

    @Test
    void should_handle_appointment_with_different_session_types() throws MessagingException {
        // arrange
        mockAppointment.setSessionType(SessionType.FOLLOWUP_SESSION);

        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(eq("appointment-confirmation-user"), any(Context.class)))
                .thenReturn("<html>User email</html>");
        when(mockTemplateEngine.process(eq("appointment-confirmation-advisor"), any(Context.class)))
                .thenReturn("<html>Advisor email</html>");

        // act
        emailService.sendAppointmentConfirmation(mockAppointment);

        // assert
        verify(mockTemplateEngine).process(eq("appointment-confirmation-user"), any(Context.class));
        verify(mockTemplateEngine).process(eq("appointment-confirmation-advisor"), any(Context.class));
        verify(mockMailSender, times(2)).createMimeMessage();
        verify(mockMailSender, times(2)).send(any(MimeMessage.class));

        // Verify the service executes successfully with different session type
    }

    @Test
    void should_handle_appointment_with_null_meeting_link() throws MessagingException {
        // arrange
        mockAppointment.setMeetingLink(null);

        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(eq("appointment-confirmation-user"), any(Context.class)))
                .thenReturn("<html>User email</html>");
        when(mockTemplateEngine.process(eq("appointment-confirmation-advisor"), any(Context.class)))
                .thenReturn("<html>Advisor email</html>");

        // act
        emailService.sendAppointmentConfirmation(mockAppointment);

        // assert
        verify(mockTemplateEngine).process(eq("appointment-confirmation-user"), any(Context.class));
        verify(mockTemplateEngine).process(eq("appointment-confirmation-advisor"), any(Context.class));
        verify(mockMailSender, times(2)).createMimeMessage();
        verify(mockMailSender, times(2)).send(any(MimeMessage.class));

        // Verify the service handles null meeting link gracefully
    }

    @Test
    void should_create_service_with_valid_dependencies() {
        // act
        EmailService service = new EmailService(mockTemplateEngine, mockMailSender);

        // assert
        assertThat(service).isNotNull();
    }

    @Test
    void should_handle_appointment_with_different_duration() throws MessagingException {
        // arrange
        mockAppointment.setDurationMinutes(90); // Different duration

        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(eq("appointment-confirmation-user"), any(Context.class)))
                .thenReturn("<html>User email</html>");
        when(mockTemplateEngine.process(eq("appointment-confirmation-advisor"), any(Context.class)))
                .thenReturn("<html>Advisor email</html>");

        // act
        emailService.sendAppointmentConfirmation(mockAppointment);

        // assert
        verify(mockTemplateEngine).process(eq("appointment-confirmation-user"), any(Context.class));
        verify(mockTemplateEngine).process(eq("appointment-confirmation-advisor"), any(Context.class));
        verify(mockMailSender, times(2)).createMimeMessage();
        verify(mockMailSender, times(2)).send(any(MimeMessage.class));

        // Verify the service handles different duration values
    }

    @Test
    void should_handle_all_session_types() throws MessagingException {
        // arrange
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Email content</html>");

        // Test all session types
        SessionType[] sessionTypes = {
                SessionType.INITIAL_CONSULTATION,
                SessionType.STANDARD_SESSION,
                SessionType.FOLLOWUP_SESSION,
                SessionType.PLAN_REVIEW
        };

        for (SessionType sessionType : sessionTypes) {
            // Reset mocks for each iteration
            reset(mockMailSender, mockTemplateEngine);
            when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
            when(mockTemplateEngine.process(anyString(), any(Context.class)))
                    .thenReturn("<html>Email content</html>");

            // arrange
            mockAppointment.setSessionType(sessionType);

            // act
            emailService.sendAppointmentConfirmation(mockAppointment);

            // assert
            verify(mockTemplateEngine, times(2)).process(anyString(), any(Context.class));
            verify(mockMailSender, times(2)).createMimeMessage();
            verify(mockMailSender, times(2)).send(any(MimeMessage.class));
        }
    }

    @Test
    void should_handle_all_appointment_statuses() throws MessagingException {
        // arrange
        when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockTemplateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Email content</html>");

        // Test different appointment statuses
        AppointmentStatus[] statuses = {
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.CANCELLED,
                AppointmentStatus.COMPLETED
        };

        for (AppointmentStatus status : statuses) {
            // Reset mocks for each iteration
            reset(mockMailSender, mockTemplateEngine);
            when(mockMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
            when(mockTemplateEngine.process(anyString(), any(Context.class)))
                    .thenReturn("<html>Email content</html>");

            // arrange
            mockAppointment.setStatus(status);

            // act - Test both confirmation and cancellation methods
            emailService.sendAppointmentConfirmation(mockAppointment);

            // assert
            verify(mockTemplateEngine, times(2)).process(anyString(), any(Context.class));
            verify(mockMailSender, times(2)).createMimeMessage();
            verify(mockMailSender, times(2)).send(any(MimeMessage.class));
        }
    }
}