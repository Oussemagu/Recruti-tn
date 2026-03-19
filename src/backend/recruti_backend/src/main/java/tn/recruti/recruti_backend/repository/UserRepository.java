package tn.recruti.recruti_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.recruti.recruti_backend.model.User;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
