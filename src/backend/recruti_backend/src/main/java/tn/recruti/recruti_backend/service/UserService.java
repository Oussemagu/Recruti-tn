package tn.recruti.recruti_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.recruti.recruti_backend.Exception.RessourceNotFoundException;
import tn.recruti.recruti_backend.dto.UpdateUserRequest;
import tn.recruti.recruti_backend.dto.UserDTO;
import tn.recruti.recruti_backend.model.User;
import tn.recruti.recruti_backend.repository.UserRepository;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Consult ────────────────────────────────────────────────────────────────

    /**
     * Get a single user by ID.
     */
    public UserDTO getUserById(Long id) {
        User user = findUserOrThrow(id);
        return toDTO(user);
    }

    /**
     * Get all users (admin use case).
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── Modify ─────────────────────────────────────────────────────────────────

    /**
     * Partially update a user's account fields.
     * Only non-null fields in the request are applied.
     */
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        if (request.getNom() != null)           user.setNom(request.getNom());
        if (request.getPrenom() != null)        user.setPrenom(request.getPrenom());
        if (request.getDateNaissance() != null) user.setDateNaissance(request.getDateNaissance());
        if (request.getSkills() != null)        user.setSkills(request.getSkills());
        if (request.getSexe() != null)          user.setSexe(request.getSexe());
        if (request.getGouvernorat() != null)   user.setGouvernorat(request.getGouvernorat());
        if (request.getPoste() != null)         user.setPoste(request.getPoste());
        if (request.getNomSociete() != null)    user.setNomSociete(request.getNomSociete());

        // Email uniqueness check before updating
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Hash new password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Decode Base64 CV if provided
        if (request.getCvGenerique() != null && !request.getCvGenerique().isBlank()) {
            user.setCvGenerique(Base64.getDecoder().decode(request.getCvGenerique()));
        }

        return toDTO(userRepository.save(user));
    }

    // ─── Delete ─────────────────────────────────────────────────────────────────

    /**
     * Delete a user account by ID.
     * Cascades to Passages, Plannifications, and Notifications via JPA.
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("User not found with id: " + id));
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setRole(user.getRole());
        dto.setDateNaissance(user.getDateNaissance());
        dto.setEmail(user.getEmail());
        dto.setSkills(user.getSkills());
        dto.setSexe(user.getSexe());
        dto.setGouvernorat(user.getGouvernorat());
        dto.setPoste(user.getPoste());
        dto.setNomSociete(user.getNomSociete());

        // Encode binary CV to Base64 for safe JSON transport
        if (user.getCvGenerique() != null) {
            dto.setCvGenerique(Base64.getEncoder().encodeToString(user.getCvGenerique()));
        }

        return dto;
    }
}
