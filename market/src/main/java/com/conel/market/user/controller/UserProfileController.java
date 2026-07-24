package com.conel.market.user.controller;

import com.conel.market.dto.product.response.ProductResponse;
import com.conel.market.user.entity.User;
import com.conel.market.user.dto.response.UserResponse;
import com.conel.market.user.service.UserService;
import com.conel.market.user.dto.request.ChangePasswordRequest;
import com.conel.market.user.dto.request.UserProfileUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;


    @PutMapping("/profile/password")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User currentUser) {

        userService.changePassword(request, currentUser.getId());
        return ResponseEntity.ok("Password updated successfully.");
    }


    @PatchMapping("/profile/deactivate")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Void> deactivateAccount(@AuthenticationPrincipal User currentUser) {
        userService.deactivateAccount(currentUser.getId());
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{userId}/reactivate")
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<Void> reactivateAccount(@PathVariable String userId) {
        userService.reactivateAccount(userId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<Void> deleteAccount(@PathVariable String userId) {
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal User authenticatedUser
    ){
        UserResponse currentUser = userService.getUserById(authenticatedUser.getId());
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid
            @RequestBody UserProfileUpdateRequest request
    ){
        UserResponse updatedUser = userService.updateProfileInfo(request,authenticatedUser.getId());
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:manage')")
    public ResponseEntity<Page<UserResponse>> findAllUsers(
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable
    ){
        Page<UserResponse> responses=userService.findAll(pageable);
        return ResponseEntity.ok(responses);
    }
}
