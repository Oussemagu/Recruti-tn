package tn.recruti.recruti_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.recruti.recruti_backend.enums.Role;
import tn.recruti.recruti_backend.model.User;
import tn.recruti.recruti_backend.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    public Map<String, Long> getRoleStatistics() {
        List<User> allUsers = userRepository.findAll();
        
        long candidatCount = allUsers.stream()
            .filter(u -> u.getRole() == Role.CANDIDAT)
            .count();
        
        long recruteurCount = allUsers.stream()
            .filter(u -> u.getRole() == Role.RECRUTEUR)
            .count();
        
        long adminCount = allUsers.stream()
            .filter(u -> u.getRole() == Role.ADMIN)
            .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("CANDIDAT", candidatCount);
        stats.put("RECRUTEUR", recruteurCount);
        stats.put("ADMIN", adminCount);
        
        return stats;
    }

    public Map<String, Integer> getSkillStatistics() {
        List<User> candidats = userRepository.findAll().stream()
            .filter(u -> u.getRole() == Role.CANDIDAT && u.getSkills() != null && !u.getSkills().isEmpty())
            .collect(Collectors.toList());

        Map<String, Integer> skillCount = new HashMap<>();

        for (User candidat : candidats) {
            String[] skills = candidat.getSkills().split(",");
            for (String skill : skills) {
                String normalizedSkill = skill.trim().toLowerCase();
                if (!normalizedSkill.isEmpty()) {
                    skillCount.put(normalizedSkill, skillCount.getOrDefault(normalizedSkill, 0) + 1);
                }
            }
        }

        // Sort by count and return top skills
        return skillCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10) // Top 10 skills
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
}