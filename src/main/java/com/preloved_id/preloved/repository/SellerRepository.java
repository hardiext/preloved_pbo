package com.preloved_id.preloved.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.preloved_id.preloved.model.Seller;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, String> {
    Optional<Seller> findByEmail(String email);//otomatis request query where email = ?
}