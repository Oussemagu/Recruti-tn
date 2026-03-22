package tn.recruti.recruti_backend.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.recruti.recruti_backend.model.Recruiter;
import tn.recruti.recruti_backend.service.RecruiterService;

@RestController
@RequestMapping(path="api/recruteur")
public class RecruteurController {
	private final RecruiterService recruiterService;
	@Autowired
    public  	RecruteurController(RecruiterService recruiterService) {
    	this.recruiterService=recruiterService;
    }
	@GetMapping(value="all")
	public List<Recruiter> getRecruiters(){
		return recruiterService.getRecruiters();
	}
}
