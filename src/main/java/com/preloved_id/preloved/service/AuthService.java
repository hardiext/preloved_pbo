package com.preloved_id.preloved.service;

import com.preloved_id.preloved.dto.AuthResponse;
import com.preloved_id.preloved.dto.LoginRequest;
import com.preloved_id.preloved.dto.RegisterRequest;
import com.preloved_id.preloved.model.*;
import com.preloved_id.preloved.repository.BuyerRepository;
import com.preloved_id.preloved.repository.SellerRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final BuyerRepository buyerRepo;
    private final SellerRepository sellerRepo;
    private final JwtService jwtService;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public AuthService(
            BuyerRepository buyerRepo,
            SellerRepository sellerRepo,
            JwtService jwtService
    ) {
        this.buyerRepo = buyerRepo;
        this.sellerRepo = sellerRepo;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {

        // cek email duplicate
        if (buyerRepo.findByEmail(request.getEmail()).isPresent()
                || sellerRepo.findByEmail(request.getEmail()).isPresent()) {

            throw new RuntimeException(
                    "Email sudah digunakan"
            );
        }

        // default role buyer
        Role role = request.getRole() == null
                ? Role.BUYER
                : request.getRole();

        // REGISTER SELLER
        if (role == Role.SELLER) {

            Seller seller = new Seller();

            seller.setId(UUID.randomUUID().toString());
            seller.setNama(request.getNama());
            seller.setEmail(request.getEmail());

            // password wajib dihash
            seller.setPassword(
                    encoder.encode(request.getPassword())
            );

            seller.setStoreName(request.getStoreName());

            Profile profile = new Profile();
            profile.setId(UUID.randomUUID().toString());

            seller.setProfile(profile);

            sellerRepo.save(seller);

            String token = jwtService.generateToken(
                    seller.getEmail(),
                    role.name()
            );

            return new AuthResponse(
                    "Register seller berhasil",
                    token,
                    role.name()
            );
        }
          Buyer buyer = new Buyer();

        buyer.setId(UUID.randomUUID().toString());
        buyer.setNama(request.getNama());
        buyer.setEmail(request.getEmail());

        buyer.setPassword(
                encoder.encode(request.getPassword())
        );

        Profile profile = new Profile();
        profile.setId(UUID.randomUUID().toString());

        buyer.setProfile(profile);

        buyerRepo.save(buyer);

        String token = jwtService.generateToken(
                buyer.getEmail(),
                role.name()
        );

        return new AuthResponse(
                "Register buyer berhasil",
                token,
                role.name()
        );
    }

    public AuthResponse login(LoginRequest request) {

        // cek buyer
        var buyer = buyerRepo.findByEmail(request.getEmail());

        if (buyer.isPresent()) {

            if (encoder.matches(
                    request.getPassword(),
                    buyer.get().getPassword()
            )) {

                String token = jwtService.generateToken(
                        buyer.get().getEmail(),
                        Role.BUYER.name()
                );

                return new AuthResponse(
                        "Login berhasil",
                        token,
                        Role.BUYER.name()
                );
            }
        }

        // cek seller
        var seller = sellerRepo.findByEmail(request.getEmail());

        if (seller.isPresent()) {

            if (encoder.matches(
                    request.getPassword(),
                    seller.get().getPassword()
            )) {

                String token = jwtService.generateToken(
                        seller.get().getEmail(),
                        Role.SELLER.name()
                );

                return new AuthResponse(
                        "Login berhasil",
                        token,
                        Role.SELLER.name()
                );
            }
        }

        throw new RuntimeException(
                "Email atau password salah"
        );
    }
}