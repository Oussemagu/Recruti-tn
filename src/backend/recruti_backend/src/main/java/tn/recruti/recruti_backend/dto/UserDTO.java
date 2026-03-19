package tn.recruti.recruti_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.recruti.recruti_backend.enums.Role;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;

    @NotNull(message = "Last name is required")
    private String nom;

    @NotNull(message = "First name is required")
    private String prenom;

    @NotNull(message = "Role is required")
    private Role role;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateNaissance;

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String skills;

    private String sexe;

    private String gouvernorat;

    private String poste;

    private String nomSociete;

    // CV is exposed as Base64 string (optional, can be omitted for list views)
    private String cvGenerique;
}
