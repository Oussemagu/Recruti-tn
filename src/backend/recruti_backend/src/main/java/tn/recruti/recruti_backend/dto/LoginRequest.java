package tn.recruti.recruti_backend.dto;

import lombok.Data;

/**
 * DTO pout la connexion d'un utilisateur existant
 * Reçu depuis le FE via le endpoint POST /api/auth/login
 * Contient uniquement les identifiants necessaires a l authentification 
 */
@Data
public class LoginRequest {
   
	// Email utilisé comme identifiant unique (username)
    private String email;

    // Mot de passe en clair → sera comparé au hash BCrypt stocké en BDD
    private String password;
	
}
