package com.ruipeng.planner.service;


import com.ruipeng.planner.entity.Appointment;
import com.ruipeng.planner.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class EmailService {

    private JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailService(TemplateEngine templateEngine, JavaMailSender mailSender) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;
    }

    @Async
    public void sendAppointmentConfirmation(Appointment appointment) {
        User user = appointment.getUser();
        User advisorUser = appointment.getAdvisor().getUser();

        // Send email to user
        sendEmail(
                user.getEmail(),
                "Your Appointment Confirmation",
                "appointment-confirmation-user",
                createAppointmentContext(appointment, false)
        );

        // Send email to advisor
        sendEmail(
                advisorUser.getEmail(),
                "New Appointment Scheduled",
                "appointment-confirmation-advisor",
                createAppointmentContext(appointment, true)
        );
    }

    @Async
    public void sendAppointmentCancellation(Appointment appointment) {
        User user = appointment.getUser();
        User advisorUser = appointment.getAdvisor().getUser();

        // Send email to user
        sendEmail(
                user.getEmail(),
                "Your Appointment Has Been Cancelled",
                "appointment-cancellation-user",
                createAppointmentContext(appointment, false)
        );

        // Send email to advisor
        sendEmail(
                advisorUser.getEmail(),
                "Appointment Cancellation",
                "appointment-cancellation-advisor",
                createAppointmentContext(appointment, true)
        );
    }

    private Map<String, Object> createAppointmentContext(Appointment appointment, boolean isForAdvisor) {
        Map<String, Object> variables = new HashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        variables.put("appointmentId", appointment.getId());
        variables.put("appointmentDate", appointment.getAppointmentDate().format(dateFormatter));
        variables.put("appointmentTime", appointment.getAppointmentDate().format(timeFormatter));
        variables.put("duration", appointment.getDurationMinutes());
        variables.put("sessionType", appointment.getSessionType().getDisplayName());
        variables.put("meetingLink", appointment.getMeetingLink());

        if (isForAdvisor) {
            User user = appointment.getUser();
            variables.put("clientName", user.getFirstName() + " " + user.getLastName());
            variables.put("clientEmail", user.getEmail());
        } else {
            User advisorUser = appointment.getAdvisor().getUser();
            variables.put("advisorName", advisorUser.getFirstName() + " " + advisorUser.getLastName());
            variables.put("advisorTitle", appointment.getAdvisor().getProfessionalTitle());
        }

        return variables;
    }

    private void sendEmail(String to, String subject, String template, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }

            String htmlContent = templateEngine.process(template, context);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email sent successfully to {} with template {}", to, template);

        } catch (MessagingException e) {
            // Log the error and continue execution
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            // Catch all other exceptions (including template engine exceptions)
            log.error("Unexpected error sending email to {} with template {}: {}", to, template, e.getMessage(), e);
        }
    }
}