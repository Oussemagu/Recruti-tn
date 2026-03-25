package tn.recruti.recruti_backend.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.recruti.recruti_backend.model.User;

import java.util.Collection;
import java.util.List;

/**
 * Adaptateur entre notre entité User (JPA) et l'interface UserDetails (Spring Security).
 * Spring Security ne connaît pas notre User directement — il a besoin de cette classe
 * pour accéder au mot de passe, au username et aux rôles de manière standardisée.
 */
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    // Notre entité User récupérée depuis la base de données
    private final User user;

    /**
     * Retourne les autorités (rôles) de l'utilisateur.
     * Spring Security attend le format "ROLE_XXX".
     * Ex: Role.RECRUITER → "ROLE_RECRUITER"
     * Utilisé par SecurityConfig pour les règles .hasRole("RECRUITER")
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    /**
     * Retourne le mot de passe hashé (BCrypt) stocké en base.
     * Spring Security le compare automatiquement lors de l'authentification.
     */
    @Override
    public String getPassword() { return user.getPassword(); }

    /**
     * Retourne l'identifiant unique de l'utilisateur.
     * On utilise l'email comme username dans notre application.
     */
    @Override
    public String getUsername() { return user.getEmail(); }

    /**
     * Expose l'entité User complète pour un accès depuis d'autres composants
     * (ex: récupérer le nom ou le rôle depuis le SecurityContext).
     */
    public User getUser() { return user; }

    // Les méthodes ci-dessous retournent true par défaut.
    // À personnaliser si vous ajoutez des fonctionnalités
    // comme la vérification d'email, le blocage de compte, etc.
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return true; }
}