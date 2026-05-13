package com.preloved_id.preloved.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {
        return "JWT VALID";
    }

    @GetMapping("/api/buyer/test")
    public String buyerOnly() {
        return "BUYER ONLY";
    }

    @GetMapping("/api/seller/test")
    public String sellerOnly() {
        return "SELLER ONLY";
    }
}