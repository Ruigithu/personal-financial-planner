package com.ruipeng.planner.service;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.ruipeng.planner.config.GoogleCalendarConfig;
import com.ruipeng.planner.entity.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.time.ZoneId;
import java.util.Arrays;



@Service
public class GoogleCalendarService {

    @Autowired
    private GoogleCalendarConfig googleCalendarConfig;

    @Autowired
    private GoogleOAuthService googleOAuthService;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);

    /**
     * 🎯 主要方法：使用OAuth创建事件
     */
    public String createAppointmentEvent(Appointment appointment, Long userId) {
        try {
            // 检查用户是否已授权OAuth
            if (!googleOAuthService.isUserAuthorized(userId)) {
                log.warn("User {} is not authorized for Google Calendar access", userId);
                throw new IllegalStateException("用户需要先授权Google Calendar访问权限");
            }

            log.info("Creating calendar event with OAuth for appointment: {}", appointment.getId());
            return createEventWithUserAuth(appointment, userId);

        } catch (Exception e) {
            log.error("Failed to create calendar event for appointment {}: {}",
                    appointment.getId(), e.getMessage(), e);
            throw new RuntimeException("创建日历事件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 🔐 使用用户OAuth创建事件
     */
    private String createEventWithUserAuth(Appointment appointment, Long userId) throws Exception {
        // 获取用户凭据
        Credential userCredential = googleOAuthService.getUserCredential(userId);

        // 创建用户授权的Calendar服务
        Calendar userCalendar = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                userCredential)
                .setApplicationName(googleCalendarConfig.getApplicationName())
                .build();

        // 创建带Google Meet的事件
        Event event = buildEventWithGoogleMeet(appointment);

        // 插入到用户主日历
        Event createdEvent = userCalendar.events()
                .insert("primary", event)
                .setConferenceDataVersion(1) // 关键：启用会议数据
                .setSendUpdates("all") // 发送邀请给所有参与者
                .execute();

        // 提取真实的Google Meet链接
        String meetLink = extractMeetLinkFromEvent(createdEvent);

        log.info("✅ OAuth calendar event created with Meet link: {}", meetLink);
        return meetLink;
    }

    /**
     * 🎯 构建带真实Google Meet的事件
     */
    private Event buildEventWithGoogleMeet(Appointment appointment) {
        Event event = new Event()
                .setSummary(buildEventTitle(appointment))
                .setDescription(buildEventDescription(appointment));

        // 设置时间
        setEventDateTime(event, appointment);

        // 🔑 关键：配置Google Meet会议
        ConferenceData conferenceData = new ConferenceData();
        conferenceData.setCreateRequest(new CreateConferenceRequest()
                .setRequestId(generateConferenceRequestId(appointment))
                .setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet")));

        event.setConferenceData(conferenceData);

        // 设置提醒
        setEventReminders(event);

        // 添加参与者
        addEventAttendees(event, appointment);

        return event;
    }

    // 保留所有现有的辅助方法
    private void addEventAttendees(Event event, Appointment appointment) {
        // ... 现有实现保持不变
    }

    private String extractMeetLinkFromEvent(Event event) {
        // ... 现有实现保持不变，但移除降级逻辑
        if (event.getConferenceData() != null &&
                event.getConferenceData().getEntryPoints() != null) {

            for (EntryPoint entryPoint : event.getConferenceData().getEntryPoints()) {
                if ("video".equals(entryPoint.getEntryPointType())) {
                    return entryPoint.getUri();
                }
            }
        }

        // 备用：从hangoutLink获取
        if (event.getHangoutLink() != null) {
            return event.getHangoutLink();
        }

        // 如果都没有，抛出异常而不是返回备用链接
        throw new RuntimeException("无法生成Google Meet链接");
    }

    private String generateConferenceRequestId(Appointment appointment) {
        return "appointment-" + appointment.getId() + "-" + System.currentTimeMillis();
    }

    /**
     * ⏰ 设置事件时间
     */
    private void setEventDateTime(Event event, Appointment appointment) {
        String timeZone = googleCalendarConfig.getTimeZone();

        DateTime startDateTime = new DateTime(
                appointment.getAppointmentDate()
                        .atZone(ZoneId.of(timeZone))
                        .toInstant()
                        .toEpochMilli()
        );
        event.setStart(new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(timeZone));

        DateTime endDateTime = new DateTime(
                appointment.getAppointmentDate()
                        .plusMinutes(appointment.getDurationMinutes())
                        .atZone(ZoneId.of(timeZone))
                        .toInstant()
                        .toEpochMilli()
        );
        event.setEnd(new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(timeZone));
    }

    // 保留原有的辅助方法
    private String buildEventTitle(Appointment appointment) {
        if (appointment.getUser() != null && appointment.getUser().getFirstName() != null) {
            return String.format("🎯 金融咨询 - %s", appointment.getUser().getFirstName());
        }
        return "🎯 金融咨询预约";
    }

    private String buildEventDescription(Appointment appointment) {
        StringBuilder desc = new StringBuilder();
        desc.append("📋 预约详情\n");
        desc.append("预约ID: ").append(appointment.getId()).append("\n");
        desc.append("时长: ").append(appointment.getDurationMinutes()).append(" 分钟\n");

        if (appointment.getUserNotes() != null) {
            desc.append("备注: ").append(appointment.getUserNotes()).append("\n");
        }

        return desc.toString();
    }

    private void setEventReminders(Event event) {
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(
                        new EventReminder().setMethod("popup").setMinutes(15),
                        new EventReminder().setMethod("email").setMinutes(24 * 60)
                ));
        event.setReminders(reminders);
    }

    // 原有方法保持兼容
    private String generateConsistentMeetLink(Appointment appointment) {
        String roomCode = generateConsistentRoomCode(appointment);
        return "https://meet.google.com/" + roomCode;
    }

    private String generateConsistentRoomCode(Appointment appointment) {
        String seedData = appointment.getId().toString() +
                appointment.getAppointmentDate().toLocalDate().toString() +
                (appointment.getUser() != null ? appointment.getUser().getId().toString() : "guest");

        int hash = Math.abs(seedData.hashCode());
        String part1 = generateCodePart(hash, 3);
        String part2 = generateCodePart(hash >> 8, 4);
        String part3 = generateCodePart(hash >> 16, 3);

        return part1 + "-" + part2 + "-" + part3;
    }

    private String generateCodePart(int seed, int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = Math.abs(seed + i) % chars.length();
            result.append(chars.charAt(index));
        }
        return result.toString();
    }
}