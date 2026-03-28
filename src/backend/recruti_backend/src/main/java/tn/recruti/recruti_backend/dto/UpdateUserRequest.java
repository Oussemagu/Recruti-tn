package tn.recruti.recruti_backend.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private String nom;

    private String prenom;

    private LocalDate dateNaissance;

    @Email(message = "Invalid email format")
    private String email;

    private String skills;

    private String sexe;

    private String gouvernorat;

    private String poste;

    private String nomSociete;

    // Optional: new password (null means no change)
    private String password;

    // Optional: new CV as Base64 string (null means no change)
    private String cvGenerique;
}
