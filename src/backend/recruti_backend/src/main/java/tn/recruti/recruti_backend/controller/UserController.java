package tn.recruti.recruti_backend.controller;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tn.recruti.recruti_backend.dto.UpdateUserRequest;
import tn.recruti.recruti_backend.dto.UserDTO;
import tn.recruti.recruti_backend.dto.VerifyPasswordRequest;
import tn.recruti.recruti_backend.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class UserController {
    private final UserService userService;

    // ─── GET /api/users ──────────────────────────────────────────────────────────
    /**
     * Retrieve all user accounts.
     * Admin access only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ─── GET /api/users/profile ──────────────────────────────────────────────────
    /**
     * Retrieve the currently authenticated user's profile.
     * Accessible to all authenticated users.
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    // ─── GET /api/users/{id} ─────────────────────────────────────────────────────
    /**
     * Retrieve a specific user by their ID.
     * Only the user themselves or admins can view a profile.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ─── PUT /api/users/profile ──────────────────────────────────────────────────
    /**
     * Update the currently authenticated user's account information.
     * Only provided (non-null) fields are updated — acts as a partial update.
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(request));
    }

    // ─── PUT /api/users/{id} ─────────────────────────────────────────────────────
    /**
     * Update a specific user's account information.
     * Only admins or the user themselves can update a profile.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // ─── DELETE /api/users/profile ───────────────────────────────────────────────
    /**
     * Permanently delete the currently authenticated user's account and all associated data.
     * Accessible to all authenticated users (for their own account).
     */
    @DeleteMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCurrentUserAccount() {
        userService.deleteCurrentUserAccount();
        return ResponseEntity.noContent().build();
    }

    // ─── DELETE /api/users/{id} ──────────────────────────────────────────────────
    /**
     * Permanently delete a user account and all associated data.
     * Only admins or the user themselves can delete a profile.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ─── POST /api/users/profile/verify-password ─────────────────────────────────
    /**
     * Verify the current user's password before account deletion.
     * @param request VerifyPasswordRequest containing the plain text password
     * @return JSON object with "valid" boolean field
     */
    @PostMapping("/profile/verify-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> verifyCurrentUserPassword(
            @Valid @RequestBody VerifyPasswordRequest request) {
        boolean isValid = userService.verifyCurrentUserPassword(request.getPassword());
        if (!isValid) {
            return ResponseEntity.status(401).body(Map.of("valid", false));
        }
        return ResponseEntity.ok(Map.of("valid", true));
    }

    // ─── POST /api/users/{id}/verify-password ────────────────────────────────────
    /**
     * Verify a user's password before account deletion.
     * Only admins or the user themselves can verify a password.
     * @param id User ID
     * @param request VerifyPasswordRequest containing the plain text password
     * @return JSON object with "valid" boolean field
     */
    @PostMapping("/{id}/verify-password")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.user.id")
    public ResponseEntity<Map<String, Boolean>> verifyPassword(
            @PathVariable Long id,
            @Valid @RequestBody VerifyPasswordRequest request) {
        boolean isValid = userService.verifyPassword(id, request.getPassword());
        if (!isValid) {
            return ResponseEntity.status(401).body(Map.of("valid", false));
        }
        return ResponseEntity.ok(Map.of("valid", true));
    }
}
