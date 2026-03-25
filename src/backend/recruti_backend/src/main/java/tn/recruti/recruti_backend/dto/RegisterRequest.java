package tn.recruti.recruti_backend.dto;

import lombok.Data;
import tn.recruti.recruti_backend.enums.Role;
import java.time.LocalDate;

/**
 * Objet de transfert de données (DTO) pour l'inscription d'un nouvel utilisateur.
 * Reçu depuis le frontend sous forme JSON via le endpoint POST /api/auth/register.
 * Ne contient PAS l'ID (généré automatiquement par la BDD).
 */
@Data
public class RegisterRequest {

    // Informations personnelles obligatoires
    private String nom;
    private String prenom;
    private String email;
    private String password;        // sera hashé avec BCrypt avant stockage en BDD

    // Rôle choisi lors de l'inscription : RECRUITER ou CANDIDATE
    private Role role;

    private LocalDate dateNaissance;

    // Informations optionnelles selon le rôle
    private String sexe;
    private String gouvernorat;
    private String poste;           // utilisé surtout pour les candidats
    private String nomSociete;      // utilisé surtout pour les recruteurs
    private String skills;          // compétences du candidat
}