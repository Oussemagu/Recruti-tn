package tn.recruti.recruti_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.recruti.recruti_backend.dto.AuthResponse;
import tn.recruti.recruti_backend.dto.LoginRequest;
import tn.recruti.recruti_backend.dto.RegisterRequest;
import tn.recruti.recruti_backend.model.User;
import tn.recruti.recruti_backend.repository.UserRepository;
import tn.recruti.recruti_backend.security.JwtUtil;

/**
 * Service gérant la logique métier de l'authentification :
 * - Inscription : création du compte + génération du token JWT
 * - Connexion : vérification des identifiants + génération du token JWT
 *
 * C'est ici que transite toute donnée sensible (mot de passe, token).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;    // BCrypt pour hasher les mots de passe
    private final JwtUtil jwtUtil;                    // pour générer les tokens JWT
    private final AuthenticationManager authenticationManager; // pour vérifier les identifiants

    /**
     * Inscrit un nouvel utilisateur dans la base de données.
     * Étapes :
     * 1. Vérifie que l'email n'est pas déjà utilisé
     * 2. Crée l'entité User avec le mot de passe hashé
     * 3. Sauvegarde en base
     * 4. Génère et retourne un token JWT
     *
     * @param request données d'inscription envoyées par le frontend
     * @return AuthResponse contenant le token JWT et les infos utilisateur
     */
    public AuthResponse register(RegisterRequest request) {

        // Vérifie l'unicité de l'email avant création
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Construction de l'entité User à partir du DTO
        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        // Hashage du mot de passe avec BCrypt — le mot de passe en clair n'est JAMAIS stocké
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setDateNaissance(request.getDateNaissance());
        user.setSexe(request.getSexe());
        user.setGouvernorat(request.getGouvernorat());
        user.setPoste(request.getPoste());
        user.setNomSociete(request.getNomSociete());
        user.setSkills(request.getSkills());
        // CV initialisé vide — à uploader séparément via un endpoint dédié
        user.setCvGenerique(new byte[0]);

        // Persistance en base de données
        userRepository.save(user);

        // Génération du token JWT avec l'email comme subject
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());


        // Retourne le token + infos utilisateur au controller
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getNom(), user.getPrenom(), user.getRole());
    }

    /**
     * Authentifie un utilisateur existant.
     * Étapes :
     * 1. Vérifie email + mot de passe via Spring Security (AuthenticationManager)
     *    → lève une exception automatiquement si les identifiants sont incorrects
     * 2. Récupère l'utilisateur depuis la base
     * 3. Génère et retourne un token JWT
     *
     * @param request email et mot de passe envoyés par le frontend
     * @return AuthResponse contenant le token JWT et les infos utilisateur
     */
    public AuthResponse login(LoginRequest request) {

        // Spring Security vérifie automatiquement :
        // - que l'email existe en base (via UserDetailsServiceImpl)
        // - que le mot de passe correspond au hash BCrypt stocké
        // Si invalide → lève BadCredentialsException (retournée comme 401 au client)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Récupération de l'utilisateur pour construire la réponse
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Génération d'un nouveau token JWT à chaque connexion
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        return new AuthResponse(token,user.getId(), user.getEmail(), user.getNom(), user.getPrenom(), user.getRole());
    }
}