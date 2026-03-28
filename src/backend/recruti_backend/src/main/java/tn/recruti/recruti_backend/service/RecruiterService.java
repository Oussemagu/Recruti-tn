package tn.recruti.recruti_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tn.recruti.recruti_backend.model.Recruiter;
import tn.recruti.recruti_backend.repository.RecruiterRepository;

@Service
public class RecruiterService {
	private final RecruiterRepository recruiterRepo;
	@Autowired
	public RecruiterService(RecruiterRepository recruiterRepo) {
	super();
	this.recruiterRepo = recruiterRepo;
	}
	public List<Recruiter> getRecruiters()
	{
		/*ArrayList<Recruiter> arrayRecruiter = new ArrayList<>();
		arrayRecruiter.add(new Recruiter("ahmed","ahmed@gmail.com", LocalDate.of(2016, 11,29),8));
	    return arrayRecruiter;*/
		return recruiterRepo.findAll();
	}
}
