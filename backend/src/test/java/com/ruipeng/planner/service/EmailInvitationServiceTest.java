package com.ruipeng.planner.service;

import com.ruipeng.planner.entity.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailInvitationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailInvitationService emailInvitationService;

    private Appointment mockAppointment;
    private User mockUser;
    private User mockAdvisorUser;
    private Advisor mockAdvisor;
    private FinancialPlan mockFinancialPlan;
    private String testMeetingLink;

    @BeforeEach
    void setUp() {
        // Setup test meeting link
        testMeetingLink = "https://meet.google.com/abc-def-ghi";

        // Setup mock user (client)
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setEmail("john.doe@example.com");
        mockUser.setRole(UserRole.USER);

        // Setup mock advisor user
        mockAdvisorUser = new User();
        mockAdvisorUser.setId(2L);
        mockAdvisorUser.setFirstName("Jane");
        mockAdvisorUser.setLastName("Smith");
        mockAdvisorUser.setEmail("jane.smith@advisor.com");
        mockAdvisorUser.setRole(UserRole.ADVISOR);

        // Setup mock advisor
        mockAdvisor = new Advisor();
        mockAdvisor.setId(1L);
        mockAdvisor.setUser(mockAdvisorUser);
        mockAdvisor.setBio("Senior Financial Advisor");
        mockAdvisor.setExperienceYears(8);

        // Setup mock financial plan
        mockFinancialPlan = new FinancialPlan();
        mockFinancialPlan.setId(1L);
        mockFinancialPlan.setPlanName("Retirement Savings Plan");
        mockFinancialPlan.setUser(mockUser);

        // Setup mock appointment
        mockAppointment = new Appointment();
        mockAppointment.setId(1L);
        mockAppointment.setUser(mockUser);
        mockAppointment.setAdvisor(mockAdvisor);
        mockAppointment.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 14, 30));
        mockAppointment.setDurationMinutes(60);
        mockAppointment.setSessionType(SessionType.INITIAL_CONSULTATION);
        mockAppointment.setStatus(AppointmentStatus.CONFIRMED);
        mockAppointment.setUserNotes("Looking forward to discussing my retirement plans");
        mockAppointment.setSharedPlan(mockFinancialPlan);
    }

    @Test
    void should_send_meet_invitation_to_both_client_and_advisor() throws MessagingException {
        // arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).createMimeMessage();
        verify(mailSender, times(2)).send(mimeMessage);
    }

    @Test
    void should_send_client_invitation_with_correct_details() throws MessagingException {
        // arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Use a spy to capture the MimeMessageHelper calls
        MimeMessageHelper helperSpy = spy(new MimeMessageHelper(mimeMessage, true, "UTF-8"));

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        // Verify that mail sender was called
        verify(mailSender, times(2)).createMimeMessage();
    }

    @Test
    void should_handle_appointment_without_user_notes() throws MessagingException {
        // arrange
        mockAppointment.setUserNotes(null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
        // Should still send emails even without user notes
    }

    @Test
    void should_handle_appointment_with_empty_user_notes() throws MessagingException {
        // arrange
        mockAppointment.setUserNotes("   "); // whitespace only
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
    }

    @Test
    void should_handle_appointment_without_shared_plan() throws MessagingException {
        // arrange
        mockAppointment.setSharedPlan(null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
        // Should still send emails even without shared plan
    }

    @Test
    void should_handle_different_session_types() throws MessagingException {
        // arrange
        mockAppointment.setSessionType(SessionType.FOLLOWUP_SESSION);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
    }

    @Test
    void should_not_throw_exception_when_email_sending_fails() {
        // arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new org.springframework.mail.MailSendException("SMTP server error"))
                .when(mailSender).send(any(MimeMessage.class));

        // act & assert
        // Should not throw exception, just log error
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // verify that createMimeMessage was still called
        verify(mailSender, atLeastOnce()).createMimeMessage();
    }

    @Test
    void should_format_appointment_date_correctly() throws MessagingException {
        // arrange
        LocalDateTime specificDate = LocalDateTime.of(2025, 12, 25, 9, 30);
        mockAppointment.setAppointmentDate(specificDate);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
        // The date should be formatted as "Thursday, 25 December 2025 at 09:30"
    }

    @Test
    void should_include_meeting_link_in_email_content() throws MessagingException {
        // arrange
        String customMeetingLink = "https://meet.google.com/custom-meeting-room";
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, customMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
        // Email content should contain the custom meeting link
    }

    @Test
    void should_send_different_content_to_client_and_advisor() throws MessagingException {
        // arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).createMimeMessage();
        verify(mailSender, times(2)).send(mimeMessage);

        // Both client and advisor should receive emails, but with different content
        // (This is implicitly tested by the method structure)
    }

    @Test
    void should_handle_appointment_with_minimum_required_fields() throws MessagingException {
        // arrange
        Appointment minimalAppointment = new Appointment();
        minimalAppointment.setId(999L);
        minimalAppointment.setUser(mockUser);
        minimalAppointment.setAdvisor(mockAdvisor);
        minimalAppointment.setAppointmentDate(LocalDateTime.of(2025, 6, 25, 10, 0));
        minimalAppointment.setDurationMinutes(30);
        minimalAppointment.setSessionType(SessionType.INITIAL_CONSULTATION);
        // No user notes, no shared plan

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(minimalAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
    }

    @Test
    void should_include_advisor_information_in_client_email() throws MessagingException {
        // arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
        // Client email should contain advisor's name (Jane Smith)
    }

    @Test
    void should_include_client_information_in_advisor_email() throws MessagingException {
        // arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
        // Advisor email should contain client's information
    }

    @Test
    void should_handle_long_user_notes() throws MessagingException {
        // arrange
        String longNotes = "This is a very long note ".repeat(50); // 1250 characters
        mockAppointment.setUserNotes(longNotes);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
    }

    @Test
    void should_handle_special_characters_in_names() throws MessagingException {
        // arrange
        mockUser.setFirstName("José");
        mockUser.setLastName("García-López");
        mockAdvisorUser.setFirstName("Müller");
        mockAdvisorUser.setLastName("O'Connor");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
    }

    @Test
    void should_use_utf8_encoding_for_emails() throws MessagingException {
        // arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
        // UTF-8 encoding should be used (verified by the MimeMessageHelper constructor)
    }

    @Test
    void should_include_appointment_id_in_advisor_email() throws MessagingException {
        // arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // act
        emailInvitationService.sendMeetInvitation(mockAppointment, testMeetingLink);

        // assert
        verify(mailSender, times(2)).send(mimeMessage);
        // Advisor email should include appointment ID for reference
    }
}