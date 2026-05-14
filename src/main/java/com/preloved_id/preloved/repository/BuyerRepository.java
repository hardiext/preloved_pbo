package com.preloved_id.preloved.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.preloved_id.preloved.model.Buyer;

import java.util.Optional;

public interface BuyerRepository extends JpaRepository<Buyer, String> {
    Optional<Buyer> findByEmail(String email); //otomatis request query where email = ?
}