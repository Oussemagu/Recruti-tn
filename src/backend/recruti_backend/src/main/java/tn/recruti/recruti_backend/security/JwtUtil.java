package tn.recruti.recruti_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utilitaire centralisé pour toutes les opérations liées aux tokens JWT :
 * - Génération d'un token après connexion
 * - Extraction de l'email depuis un token
 * - Validation d'un token (signature + expiration)
 *
 * Les valeurs jwt.secret et jwt.expiration sont lues depuis application.properties.
 */
@Component
public class JwtUtil {

    // Clé secrète utilisée pour signer et vérifier les tokens
    // Doit rester CONFIDENTIELLE — ne jamais la committer en clair sur Git
    @Value("${jwt.secret}")
    private String secret;

    // Durée de validité du token en millisecondes (ex: 86400000 = 24 heures)
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Convertit la clé secrète (String) en objet SecretKey utilisable par JJWT.
     * Utilise l'algorithme HMAC-SHA256.
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Génère un token JWT signé contenant l'email de l'utilisateur.
     * Appelé après une connexion ou inscription réussie.
     *
     * @param email identifiant unique de l'utilisateur
     * @return token JWT sous forme de String
     */
    public String generateToken(String email, Long id, String role) {
        return Jwts.builder()
                .subject(email)                                           // payload : email de l'utilisateur
                .claim("id", id)   
                .claim("role",role)
                .issuedAt(new Date())                                     // date de création du token
                .expiration(new Date(System.currentTimeMillis() + expiration)) // date d'expiration
                .signWith(getKey())                                       // signature HMAC-SHA256
                .compact();                                               // sérialisation en String
    }

    /**
     * Extrait l'email (subject) depuis un token JWT.
     * Appelé dans JwtFilter pour identifier l'utilisateur de la requête.
     *
     * @param token token JWT valide
     * @return email contenu dans le payload du token
     */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getKey())       // vérification de la signature
                .build()
                .parseSignedClaims(token)  // parsing du token
                .getPayload()
                .getSubject();             // récupération du champ "subject" = email
    }

    /**
     * Vérifie si un token est valide :
     * - Signature correcte (non falsifié)
     * - Non expiré
     *
     * @param token token JWT à vérifier
     * @return true si valide, false sinon
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token); // lève une exception si invalide ou expiré
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token falsifié, expiré, malformé ou vide
            return false;
        }
    }
}