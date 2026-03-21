package tn.recruti.recruti_backend.model;

import java.time.LocalDate;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table
public class Recruiter {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String email;
	private LocalDate dob;
	private Integer age;
	public Recruiter() {
		
	}
	
	public Recruiter(String name, String email, LocalDate dob, Integer age) {
		super();
		this.name = name;
		this.email = email;
		this.dob = dob;
		this.age = age;
	}
	
	public Long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getEmail() {
		return email;
	}
	public LocalDate getDob() {
		return dob;
	}
	public Integer getAge() {
		return age;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setDob(LocalDate dob) {
		this.dob = dob;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	

}
