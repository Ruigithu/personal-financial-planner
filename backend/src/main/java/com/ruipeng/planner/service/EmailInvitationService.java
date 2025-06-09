package com.ruipeng.planner.service;

import com.ruipeng.planner.entity.Appointment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailInvitationService {

    @Autowired
    private JavaMailSender mailSender;


    Logger log = LoggerFactory.getLogger(EmailInvitationService.class);

    /**
     * 发送 Google Meet 邀请邮件
     */
    public void sendMeetInvitation(Appointment appointment, String meetLink) {
        try {
            // 发送给客户
            sendMeetInvitationToClient(appointment, meetLink);

            // 发送给顾问
            sendMeetInvitationToAdvisor(appointment, meetLink);

            log.info("Meet invitations sent for appointment {}", appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send meet invitations for appointment {}", appointment.getId(), e);
        }
    }

    private void sendMeetInvitationToClient(Appointment appointment, String meetLink) throws MessagingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(appointment.getUser().getEmail());
        helper.setSubject("Financial Consultation Appointment - Google Meet Invitation");
        helper.setText(buildClientEmailContent(appointment, meetLink), true);

        mailSender.send(message);
        log.info("Meet invitation sent to client: {}", appointment.getUser().getEmail());
    }

    private void sendMeetInvitationToAdvisor(Appointment appointment, String meetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(appointment.getAdvisor().getUser().getEmail());
        helper.setSubject("Client Appointment - Google Meet Invitation");
        helper.setText(buildAdvisorEmailContent(appointment, meetLink), true);

        mailSender.send(message);
        log.info("Meet invitation sent to advisor: {}", appointment.getAdvisor().getUser().getEmail());
    }

    private String buildClientEmailContent(Appointment appointment, String meetLink) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy 'at' HH:mm");
        String appointmentTime = appointment.getAppointmentDate().format(formatter);

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c5aa0;">📅 Your Financial Consultation is Confirmed!</h2>
                    
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #2c5aa0;">Appointment Details</h3>
                        <p><strong>📅 Date & Time:</strong> %s (Dublin Time)</p>
                        <p><strong>⏱️ Duration:</strong> %d minutes</p>
                        <p><strong>👨‍💼 Advisor:</strong> %s %s</p>
                        <p><strong>🎯 Session Type:</strong> %s</p>
                    </div>

                    <div style="background-color: #e8f4fd; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2c5aa0;">
                        <h3 style="margin-top: 0; color: #2c5aa0;">🔗 Join the Meeting</h3>
                        <p>Click the link below to join your Google Meet consultation:</p>
                        <div style="text-align: center; margin: 20px 0;">
                            <a href="%s" style="background-color: #2c5aa0; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                                🎥 Join Google Meet
                            </a>
                        </div>
                        <p style="font-size: 14px; color: #666;">
                            <strong>Meeting Link:</strong> <a href="%s">%s</a>
                        </p>
                    </div>

                    %s

                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <h4 style="margin-top: 0; color: #856404;">📝 Before the Meeting</h4>
                        <ul style="color: #856404;">
                            <li>Test your camera and microphone</li>
                            <li>Prepare any questions or documents you'd like to discuss</li>
                            <li>Find a quiet space for the consultation</li>
                        </ul>
                    </div>

                    <div style="border-top: 1px solid #eee; padding-top: 20px; margin-top: 30px; font-size: 14px; color: #666;">
                        <p>Need to reschedule or have questions? Please contact us as soon as possible.</p>
                        <p>We look forward to helping you with your financial planning goals!</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                appointmentTime,
                appointment.getDurationMinutes(),
                appointment.getAdvisor().getUser().getFirstName(),
                appointment.getAdvisor().getUser().getLastName(),
                appointment.getSessionType(),
                meetLink,
                meetLink,
                meetLink,
                appointment.getUserNotes() != null && !appointment.getUserNotes().trim().isEmpty()
                        ? String.format("<div style=\"background-color: #f0f9ff; padding: 15px; border-radius: 8px; margin: 20px 0;\"><h4 style=\"margin-top: 0; color: #0c4a6e;\">📋 Your Notes</h4><p style=\"color: #0c4a6e;\">%s</p></div>", appointment.getUserNotes())
                        : ""
        );
    }

    private String buildAdvisorEmailContent(Appointment appointment, String meetLink) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy 'at' HH:mm");
        String appointmentTime = appointment.getAppointmentDate().format(formatter);

        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c5aa0;">👨‍💼 New Client Appointment Scheduled</h2>
                    
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #2c5aa0;">Client Information</h3>
                        <p><strong>👤 Client:</strong> %s %s</p>
                        <p><strong>📧 Email:</strong> %s</p>
                        <p><strong>📅 Date & Time:</strong> %s (Dublin Time)</p>
                        <p><strong>⏱️ Duration:</strong> %d minutes</p>
                        <p><strong>🎯 Session Type:</strong> %s</p>
                    </div>

                    <div style="background-color: #e8f4fd; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2c5aa0;">
                        <h3 style="margin-top: 0; color: #2c5aa0;">🔗 Meeting Room</h3>
                        <div style="text-align: center; margin: 20px 0;">
                            <a href="%s" style="background-color: #2c5aa0; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                                🎥 Join Google Meet
                            </a>
                        </div>
                        <p style="font-size: 14px; color: #666;">
                            <strong>Meeting Link:</strong> <a href="%s">%s</a>
                        </p>
                    </div>

                    %s

                    %s

                    <div style="border-top: 1px solid #eee; padding-top: 20px; margin-top: 30px; font-size: 14px; color: #666;">
                        <p><strong>Appointment ID:</strong> %s</p>
                        <p>The client has been sent the same meeting link and appointment details.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                appointment.getUser().getFirstName(),
                appointment.getUser().getLastName(),
                appointment.getUser().getEmail(),
                appointmentTime,
                appointment.getDurationMinutes(),
                appointment.getSessionType(),
                meetLink,
                meetLink,
                meetLink,
                appointment.getUserNotes() != null && !appointment.getUserNotes().trim().isEmpty()
                        ? String.format("<div style=\"background-color: #f0f9ff; padding: 15px; border-radius: 8px; margin: 20px 0;\"><h4 style=\"margin-top: 0; color: #0c4a6e;\">📋 Client Notes</h4><p style=\"color: #0c4a6e;\">%s</p></div>", appointment.getUserNotes())
                        : "",
                appointment.getSharedPlan() != null
                        ? String.format("<div style=\"background-color: #f0fdf4; padding: 15px; border-radius: 8px; margin: 20px 0;\"><h4 style=\"margin-top: 0; color: #166534;\">📊 Related Financial Plan</h4><p style=\"color: #166534;\">%s</p></div>", appointment.getSharedPlan().getPlanName())
                        : "",
                appointment.getId()
        );
    }
}
