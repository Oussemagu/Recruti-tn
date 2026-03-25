package tn.recruti.recruti_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.recruti.recruti_backend.model.User;

public interface UserRepository extends JpaRepository<User,Long> {
	
    // Appelée dans UserDetailsServiceImpl.loadUserByUsername()
    // et dans AuthService.login() pour récupérer l'utilisateur après authentification
    Optional<User> findByEmail(String email);

    // Appelée dans AuthService.register() pour vérifier
    // qu'un compte avec cet email n'existe pas déjà
    boolean existsByEmail(String email);

}
