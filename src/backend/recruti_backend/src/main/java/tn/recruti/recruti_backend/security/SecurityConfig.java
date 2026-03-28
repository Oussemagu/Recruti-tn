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
 * Configuration centrale de spring Security
 * Définit :
 * - Quelles routes sont pulbiques ou ârtagées 
 * - la politique de  la session (STATELESS car on utilise JWT)
 * - L'encodeur de mot de passe (bcrypt)
 * -la configuration CORS pour autoriser le frontend angular
 * - L'injection du filtre JWT dans la chaine de filtres
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity //permet d utiliser @PreAuthorize sur les methodes des controllers
@RequiredArgsConstructor
public class SecurityConfig {
	//filtre JWT custom a injecter dans la chaine des filtres Spring
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
		//desactive CSRF  car  on utilise JWT (stateless et non pas les sessions/cookies
		.csrf(csrf -> csrf.disable())
		//ACTIVE la config CORS definie dans corsConfigurationSource()
		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
		//Pas de session HTTP coté serveur -  chaque requete est autonome grace au JWT
		.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		//Definition des regles d'acces par rpute
		.authorizeHttpRequests(auth -> auth
				//Routes publiques : inscription et connexion accessibles sans token 
				.requestMatchers("/api/auth/**").permitAll()
				//Routes utilisateurs nécessitent authentification (l'autho se fait via @PreAuthorize au niveau des endpoints)
				.requestMatchers("/api/users/**").authenticated()
				//Routes accessibles aux deux roles authentifies
				.requestMatchers("/api/offers/**").hasAnyRole("RECRUITER","CANDIDATE")
				//Routes reservees aux recruteurs uniquement
			    .requestMatchers("/api/admin/**").hasRole("RECRUITER")
			    //Toute autre route nécessite d'etre authentifié
			    .anyRequest().authenticated()
			    
		)
		//Injecte notre filtre JwtFilter AVANT le filtre d'authentification par defaut de spring 
		//Ainsi , chaque requete passe d  abord par notre filtre JWT
		.addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
	
	/**
	 * Bean  d encodage des mots de passe avec Bcrypt
	 * Bcrypt : alog de hachage sécurisé avec sel automatique
	 * Utilisé dans AuthService  pour hasher le mot de passe à l'inscription
	 * et par Spring Security pour le comparer lors de la connexion
	 *
	 */
	@Bean 
	public PasswordEncoder passwordEncoder() {
		return new  BCryptPasswordEncoder();
	}
	
	/**
	 * Expose l'authenticationManager comme Bean Spring.
	 * Utilisé dans AuthService pour déclancher l'authentification
	 *  (verification mail + mdp ) via Spring Security
	 */
	 @Bean
	    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
	            throws Exception {
	        return config.getAuthenticationManager();
	    }
	/**
	 * Configuration cors pour autoriser les requetes depuis le FE Angular
	 * Sans cette configuration le naviguateur bloquerait toutes les requetes
	 * *cross-origin (Angular sur 4200: Spring sur :8080)
	 */
	 @Bean 
	 public CorsConfigurationSource corsConfigurationSource() {
		 CorsConfiguration  config = new CorsConfiguration();
		 
		 //lit les origines  a partir de applications.properties
		 config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
		 
		 //Methodes HTTP autoriséees 
		 config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
		 
		 //Tous les headers sont autorisés (notamment "Authorization" pour le JWT)
		 config.setAllowedHeaders(List.of("*"));
		 
	      // Autorise l'envoi de cookies/credentials dans les requêtes cross-origin
          config.setAllowCredentials(true);

          // Applique cette configuration à toutes les routes
          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.setCorsConfigurations(java.util.Map.of("/**", config));
          return source;
        	 }
	
	
}
