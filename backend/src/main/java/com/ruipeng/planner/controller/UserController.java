package com.ruipeng.planner.controller;

import com.ruipeng.planner.dto.MessageResponse;
import com.ruipeng.planner.dto.PasswordChangeRequest;
import com.ruipeng.planner.dto.UserProfileUpdateDto;
import com.ruipeng.planner.entity.User;
import com.ruipeng.planner.entity.UserProfile;
import com.ruipeng.planner.config.security.UserDetailsImpl;
import com.ruipeng.planner.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")

public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADVISOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProfile() {
        Long userId = getCurrentUserId();
        try {
            UserProfile profile = userService.getUserProfileByUserId(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADVISOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserProfileUpdateDto profileUpdate) {
        Long userId = getCurrentUserId();
        try {
            UserProfile updatedProfile = userService.updateUserProfile(userId, profileUpdate);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('USER') or hasRole('ADVISOR') or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest passwordRequest) {
        Long userId = getCurrentUserId();
        try {
            userService.updateUserPassword(userId, passwordRequest.getCurrentPassword(), passwordRequest.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("Password updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}
