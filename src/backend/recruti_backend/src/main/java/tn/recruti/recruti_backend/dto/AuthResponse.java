package tn.recruti.recruti_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import tn.recruti.recruti_backend.enums.Role;
/**
 * DTO renvoyé au frontend après une inscription ou connexion réussie.
 * Contient le token JWT àstocker coté client (localSotrage ou sessionStorage)
 * et les infos de base de l'utilisateur pour affichage immédiat sans appel supplementaire
 */

@Data
@AllArgsConstructor
public class AuthResponse {

	//token JWT à inclure sans chaque requete suivante
	//Format dans le header HTTP : "Authorization : Bearer <token>"
	private String token;
	private Long id ;
	//Informations du user connecté
	private String email;
	private String nom;
	private String prenom;
	
    // Rôle renvoyé pour que le frontend puisse rediriger vers
    // le bon dashboard (recruteur ou candidat)
    private Role role;

}
