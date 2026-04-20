package tn.recruti.recruti_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.recruti.recruti_backend.service.DashboardService;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
 
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/role-stats")
    public ResponseEntity<Map<String, Long>> getRoleStatistics() {
        return ResponseEntity.ok(dashboardService.getRoleStatistics());
    }

    @GetMapping("/skill-stats")
    public ResponseEntity<Map<String, Integer>> getSkillStatistics() {
        return ResponseEntity.ok(dashboardService.getSkillStatistics());
    }
}