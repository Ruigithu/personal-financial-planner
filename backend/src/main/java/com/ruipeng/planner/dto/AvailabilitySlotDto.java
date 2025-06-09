package com.ruipeng.planner.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruipeng.planner.entity.AvailabilitySlot;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilitySlotDto {
    private Integer dayOfWeek;
    private LocalDate specificDate;

    @NotNull
    @JsonFormat(pattern = "HH:mm")  // 格式化为 HH:mm，避免前端解析错误
    private LocalTime startTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull
    private boolean recurring;

    // 新增：前端需要的日期字段
    private String date; // yyyy-MM-dd 格式

    // 构造函数
    public AvailabilitySlotDto() {}

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalDate getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(LocalDate specificDate) {
        this.specificDate = specificDate;
    }

    public @NotNull LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(@NotNull LocalTime startTime) {
        this.startTime = startTime;
    }

    public @NotNull LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(@NotNull LocalTime endTime) {
        this.endTime = endTime;
    }

    @NotNull
    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(@NotNull boolean recurring) {
        this.recurring = recurring;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // 从实体转换的静态方法
    public static List<AvailabilitySlotDto> fromEntity(AvailabilitySlot entity, LocalDate targetDate) {
        List<AvailabilitySlotDto> slots = new ArrayList<>();

        if (entity.isRecurring()) {
            // 检查目标日期是否匹配周期性时段的星期几
            int targetDayOfWeek = targetDate.getDayOfWeek().getValue();
            if (entity.getDayOfWeek().equals(targetDayOfWeek)) {
                AvailabilitySlotDto dto = new AvailabilitySlotDto();
                dto.setDayOfWeek(entity.getDayOfWeek());
                dto.setStartTime(entity.getStartTime());
                dto.setEndTime(entity.getEndTime());
                dto.setRecurring(true);
                dto.setDate(targetDate.toString()); // yyyy-MM-dd
                slots.add(dto);
            }
        } else if (entity.getSpecificDate() != null && entity.getSpecificDate().equals(targetDate)) {
            // 非周期性时段，检查特定日期是否匹配
            AvailabilitySlotDto dto = new AvailabilitySlotDto();
            dto.setDayOfWeek(entity.getDayOfWeek());
            dto.setSpecificDate(entity.getSpecificDate());
            dto.setStartTime(entity.getStartTime());
            dto.setEndTime(entity.getEndTime());
            dto.setRecurring(false);
            dto.setDate(targetDate.toString());
            slots.add(dto);
        }

        return slots;
    }

}