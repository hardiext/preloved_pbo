package com.preloved_id.preloved.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.preloved_id.preloved.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email); //otomatis request query where email = ?
}