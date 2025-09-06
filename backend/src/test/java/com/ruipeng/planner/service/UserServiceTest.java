package com.ruipeng.planner.service;

import com.ruipeng.planner.dto.UserProfileUpdateDto;
import com.ruipeng.planner.entity.User;
import com.ruipeng.planner.entity.UserProfile;
import com.ruipeng.planner.entity.EducationLevel; // 需要添加这个枚举导入
import com.ruipeng.planner.repository.UserProfileRepository;
import com.ruipeng.planner.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private UserProfile mockUserProfile;
    private UserProfileUpdateDto mockUpdateDto;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setPasswordHash("encodedPassword");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        mockUserProfile = new UserProfile();
        mockUserProfile.setId(1L);
        mockUserProfile.setUser(mockUser); // UserProfile 通过 User 对象关联，不是 userId
        mockUserProfile.setAge(25);
        mockUserProfile.setOccupation("Engineer");
        mockUserProfile.setEducationLevel(EducationLevel.BACHELOR); // 假设是枚举
        mockUserProfile.setMonthlyIncome(new BigDecimal("10000"));
        mockUserProfile.setMonthlyExpenses(new BigDecimal("5000"));
        mockUserProfile.setTotalSavings(new BigDecimal("50000"));
        mockUserProfile.setTotalDebt(new BigDecimal("0"));
        mockUserProfile.setRiskTolerance(3); // 现在是 Integer 类型
        mockUserProfile.setInvestmentHorizon("Long-term");
        mockUserProfile.setRetirementAge(65);
        mockUserProfile.setMonthlySavings(new BigDecimal("2000"));

        mockUpdateDto = new UserProfileUpdateDto();
        mockUpdateDto.setAge(30);
        mockUpdateDto.setOccupation("Senior Engineer");
        mockUpdateDto.setMonthlyIncome(new BigDecimal("15000"));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(mockUser.getId(), result.getId());
        assertEquals(mockUser.getEmail(), result.getEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowEntityNotFoundException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserById(userId)
        );
        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserProfileByUserId_WhenProfileExists_ShouldReturnProfile() {
        // Given
        Long userId = 1L;
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(mockUserProfile));

        // When
        UserProfile result = userService.getUserProfileByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(mockUserProfile.getUser().getId(), result.getUser().getId());
        assertEquals(mockUserProfile.getAge(), result.getAge());
        verify(userProfileRepository).findByUserId(userId);
    }

    @Test
    void getUserProfileByUserId_WhenProfileNotExists_ShouldThrowEntityNotFoundException() {
        // Given
        Long userId = 999L;
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserProfileByUserId(userId)
        );
        assertEquals("User profile not found for user id: " + userId, exception.getMessage());
        verify(userProfileRepository).findByUserId(userId);
    }

    @Test
    void updateUserProfile_WhenProfileExists_ShouldUpdateAndReturnProfile() {
        // Given
        Long userId = 1L;
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(mockUserProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(mockUserProfile);

        // When
        UserProfile result = userService.updateUserProfile(userId, mockUpdateDto);

        // Then
        assertNotNull(result);
        assertEquals(mockUpdateDto.getAge(), mockUserProfile.getAge());
        assertEquals(mockUpdateDto.getOccupation(), mockUserProfile.getOccupation());
        assertEquals(mockUpdateDto.getMonthlyIncome(), mockUserProfile.getMonthlyIncome());

        verify(userProfileRepository).findByUserId(userId);
        verify(userProfileRepository).save(mockUserProfile);
    }

    @Test
    void updateUserProfile_WhenProfileNotExists_ShouldThrowEntityNotFoundException() {
        // Given
        Long userId = 999L;
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUserProfile(userId, mockUpdateDto)
        );
        assertEquals("User profile not found for user id: " + userId, exception.getMessage());
        verify(userProfileRepository).findByUserId(userId);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void updateUserProfile_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Given
        Long userId = 1L;
        UserProfileUpdateDto partialUpdate = new UserProfileUpdateDto();
        partialUpdate.setAge(35); // 只更新年龄

        Integer originalAge = mockUserProfile.getAge();
        String originalOccupation = mockUserProfile.getOccupation();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(mockUserProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(mockUserProfile);

        // When
        UserProfile result = userService.updateUserProfile(userId, partialUpdate);

        // Then
        assertNotNull(result);
        assertEquals(35, mockUserProfile.getAge()); // 年龄应该被更新
        assertEquals(originalOccupation, mockUserProfile.getOccupation()); // 职业应该保持不变

        verify(userProfileRepository).findByUserId(userId);
        verify(userProfileRepository).save(mockUserProfile);
    }

    @Test
    void updateUserProfile_WithNullFields_ShouldNotUpdateNullFields() {
        // Given
        Long userId = 1L;
        UserProfileUpdateDto updateWithNulls = new UserProfileUpdateDto();
        // 所有字段都为 null

        Integer originalAge = mockUserProfile.getAge();
        String originalOccupation = mockUserProfile.getOccupation();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(mockUserProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(mockUserProfile);

        // When
        UserProfile result = userService.updateUserProfile(userId, updateWithNulls);

        // Then
        assertNotNull(result);
        assertEquals(originalAge, mockUserProfile.getAge());
        assertEquals(originalOccupation, mockUserProfile.getOccupation());

        verify(userProfileRepository).save(mockUserProfile);
    }

    @Test
    void updateUserPassword_WhenCurrentPasswordCorrect_ShouldUpdatePassword() {
        // Given
        Long userId = 1L;
        String currentPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedNewPassword = "encodedNewPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(currentPassword, mockUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // When
        userService.updateUserPassword(userId, currentPassword, newPassword);

        // Then
        assertEquals(encodedNewPassword, mockUser.getPasswordHash());
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(currentPassword, "encodedPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUserPassword_WhenUserNotExists_ShouldThrowEntityNotFoundException() {
        // Given
        Long userId = 999L;
        String currentPassword = "oldPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUserPassword(userId, currentPassword, newPassword)
        );
        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserPassword_WhenCurrentPasswordIncorrect_ShouldThrowIllegalArgumentException() {
        // Given
        Long userId = 1L;
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(currentPassword, mockUser.getPasswordHash())).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUserPassword(userId, currentPassword, newPassword)
        );
        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(currentPassword, mockUser.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }
}
