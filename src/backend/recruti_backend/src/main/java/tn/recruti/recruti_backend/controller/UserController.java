package tn.recruti.recruti_backend.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.recruti.recruti_backend.dto.UpdateUserRequest;
import tn.recruti.recruti_backend.dto.UserDTO;
import tn.recruti.recruti_backend.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // ─── GET /api/users ──────────────────────────────────────────────────────────
    /**
     * Retrieve all user accounts.
     * Typically restricted to ADMIN role via Spring Security.
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ─── GET /api/users/{id} ─────────────────────────────────────────────────────
    /**
     * Retrieve a specific user by their ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ─── PUT /api/users/{id} ─────────────────────────────────────────────────────
    /**
     * Update a user's account information.
     * Only provided (non-null) fields are updated — acts as a partial update.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // ─── DELETE /api/users/{id} ──────────────────────────────────────────────────
    /**
     * Permanently delete a user account and all associated data.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
