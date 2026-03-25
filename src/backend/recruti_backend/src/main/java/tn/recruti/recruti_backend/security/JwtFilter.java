//" Pour traiter le token, on utilise un filter qui va l'extraire du header, le valider 
//puis ajouter 
//au contexte de Spring uneauthentication correspondant a l'utilisateur pour lequel le token a ete emis :
//notre client est authentifie pour le reste de sa requete.

package tn.recruti.recruti_backend.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre JWT exécuté UNE SEULE FOIS par requête HTTP.
 * Son rôle : intercepter chaque requête, extraire le token JWT
 * depuis le header Authorization, le valider, puis authentifier
 * l'utilisateur dans le contexte de sécurité Spring.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    // Utilitaire pour générer/valider/lire les tokens JWT
    private final JwtUtil jwtUtil;

    // Service pour charger l'utilisateur depuis la base de données via son email
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Méthode principale du filtre, appelée automatiquement
     * par Spring pour chaque requête entrante.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // 1. Lire le header "Authorization" de la requête HTTP
        //    Format attendu : "Bearer eyJhbGciOiJIUzI1NiJ9..."
        String authHeader = request.getHeader("Authorization");

        // 2. Si le header est absent ou ne commence pas par "Bearer ",
        //    on laisse passer la requête sans authentification
        //    (elle sera bloquée plus tard par SecurityConfig si la route est protégée)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Extraire le token en supprimant le préfixe "Bearer " (7 caractères)
        String token = authHeader.substring(7);

        // 4. Vérifier que le token est valide (signature correcte + non expiré)
        //    Si invalide, on laisse passer sans authentifier
        if (!jwtUtil.isTokenValid(token)) {
            chain.doFilter(request, response);
            return;
        }

        // 5. Extraire l'email de l'utilisateur depuis le payload du token
        String email = jwtUtil.extractEmail(token);

        // 6. Si l'email est présent ET que l'utilisateur n'est pas déjà authentifié
        //    dans le contexte Spring (évite de ré-authentifier inutilement)
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 7. Charger les détails de l'utilisateur depuis la base de données
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 8. Créer un objet d'authentification Spring avec :
            //    - l'utilisateur (principal)
            //    - null pour les credentials (pas besoin du mot de passe ici)
            //    - ses rôles/autorités (ex: ROLE_RECRUITER)
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

            // 9. Ajouter les détails de la requête HTTP (IP, session, etc.)
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 10. Placer l'authentification dans le SecurityContext
            //     À partir de ce moment, Spring considère l'utilisateur comme connecté
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 11. Continuer la chaîne de filtres vers le controller
        chain.doFilter(request, response);
    }
}
