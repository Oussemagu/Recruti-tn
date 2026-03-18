package tn.recruti.recruti_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.recruti.recruti_backend.model.User;

public interface UserRepository extends JpaRepository<User,Long> {
}
