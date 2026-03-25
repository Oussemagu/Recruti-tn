package tn.recruti.recruti_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.recruti.recruti_backend.repository.UserRepository;

/**
 * Implémentation de l'interface UserDetailsService de Spring Security.
 * Spring appelle automatiquement cette classe lors de l'authentification
 * pour charger l'utilisateur depuis la base de données via son email.
 *
 * Elle fait le lien entre Spring Security et notre UserRepository JPA.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    // Repository JPA pour accéder à la table "users" en base de données
    private final UserRepository userRepository;

    /**
     * Recherche un utilisateur en base via son email (utilisé comme username).
     * Appelé automatiquement par Spring Security lors de la vérification du token JWT
     * dans JwtFilter, ainsi que lors de l'authentification dans AuthService.
     *
     * @param email l'email de l'utilisateur à charger
     * @return UserDetailsImpl wrappant l'entité User trouvée
     * @throws UsernameNotFoundException si aucun utilisateur ne correspond à cet email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserDetailsImpl::new)  // convertit User en UserDetailsImpl
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}