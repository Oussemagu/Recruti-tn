package tn.recruti.recruti_backend.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

/**
 * Configuration centrale de Spring Security
 * Définit :
 * - Quelles routes sont publiques ou protégées
 * - La politique de session (STATELESS car on utilise JWT)
 * - L'encodeur de mot de passe (BCrypt)
 * - La configuration CORS pour autoriser le frontend Angular
 * - L'injection du filtre JWT dans la chaîne de filtres
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permet d'utiliser @PreAuthorize sur les méthodes des controllers
@RequiredArgsConstructor
public class SecurityConfig {
    
    // Filtre JWT custom à injecter dans la chaîne des filtres Spring
    private final JwtFilter jwtFilter;
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    
    /**
     * Définit la chaîne de filtres de sécurité HTTP.
     * C'est ici que toutes les règles d'accès sont configurées.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // ✅ Configuration des headers de sécurité
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // Permet l'affichage dans iframe du même domaine
            )
            
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/candidatures/cv/**").permitAll() // ✅ Accès public aux CVs
                .anyRequest().permitAll() // Toutes les autres routes ouvertes (à sécuriser en production !)
            )
            
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
     
    /**
     * Bean d'encodage des mots de passe avec BCrypt
     * BCrypt : algo de hachage sécurisé avec sel automatique
     * Utilisé dans AuthService pour hasher le mot de passe à l'inscription
     * et par Spring Security pour le comparer lors de la connexion
     */
    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Expose l'AuthenticationManager comme Bean Spring.
     * Utilisé dans AuthService pour déclencher l'authentification
     * (vérification mail + mdp) via Spring Security
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * Configuration CORS pour autoriser les requêtes depuis le FE Angular
     * Sans cette configuration le navigateur bloquerait toutes les requêtes
     * cross-origin (Angular sur :4200, Spring sur :8080)
     */
    @Bean 
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Lit les origines à partir de application.properties
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        
        // Méthodes HTTP autorisées (ajout de PATCH pour vos endpoints)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Tous les headers sont autorisés (notamment "Authorization" pour le JWT)
        config.setAllowedHeaders(List.of("*"));
        
        // Autorise l'envoi de cookies/credentials dans les requêtes cross-origin
        config.setAllowCredentials(true);

        // Applique cette configuration à toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }
}