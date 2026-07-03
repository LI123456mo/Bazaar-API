package com.conel.market.models.user;

import com.conel.market.models.user.request.ChangePasswordRequest;
import com.conel.market.models.user.request.UserProfileUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
            @RequestBody UserProfileUpdateRequest request
    ){
        UserResponse updatedUser = userService.updateProfileInfo(request,authenticatedUser.getId());
        return ResponseEntity.ok(updatedUser);
    }
}
