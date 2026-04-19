package tn.recruti.recruti_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.recruti.recruti_backend.dto.AuthResponse;
import tn.recruti.recruti_backend.dto.LoginRequest;
import tn.recruti.recruti_backend.dto.RegisterRequest;
import tn.recruti.recruti_backend.service.AuthService; 

/**
 * Controller REST exposant les endpoints publics d'authentification.
 * ce sont des routes accessibles sans token jwt (declarées dans SpringConfig)
 * 
 * Base URL : /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController{
	// Délègue toute la logique métier au service AuthService
	private final AuthService authService;    
    /**
     * POST /api/auth/register
     * Inscrit un nouvel utilisateur ( recruteur ou candidat)
     * @param request corps JSON contenant les dx d'inscription
     * @return 200 OK + token JWT et infos utilisateur 
     * 
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (@RequestBody RegisterRequest request){
    	return ResponseEntity.ok(authService.register(request));
    }
    
    /**
     * POST /api/auth/login
     * Conncete un utilisateur existant
     * 
     * @param request corps JSON contenant email+password
     * @return 200 OK + token JWT et infos user
     * 401 Unauthorized si  les identifiants sont incorrects
     * 
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody LoginRequest request){
    	return ResponseEntity.ok(authService.login(request));
    }
}