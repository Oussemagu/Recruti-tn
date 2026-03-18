package tn.recruti.recruti_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.recruti.recruti_backend.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
}
