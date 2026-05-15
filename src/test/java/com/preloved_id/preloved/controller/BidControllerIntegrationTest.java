// test/java/com/preloved_id/preloved/controller/BidControllerIntegrationTest.java
package com.preloved_id.preloved.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.preloved_id.preloved.dto.BidRequest;
import com.preloved_id.preloved.dto.CounterOfferRequest;
import com.preloved_id.preloved.model.*;
import com.preloved_id.preloved.model.enums.BidStatus;
import com.preloved_id.preloved.repository.BidRepository;
import com.preloved_id.preloved.repository.ProductRepository;
import com.preloved_id.preloved.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("BidController Integration Tests")
class BidControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BidRepository bidRepository;

    private Product testProduct;
    private Buyer buyer;
    private Seller seller;

    @BeforeEach
    void setUp() {
        // Bersihkan data sebelumnya
        bidRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Buat Seller (extends User)
        seller = new Seller();
        seller.setId("seller-001");
        seller.setEmail("seller@test.com");
        seller.setNama("Test Seller");
        seller.setPassword("password");
        seller.setStoreName("Test Store");
        seller.setTotalSpent(0);
        seller = (Seller) userRepository.save(seller);

        // Buat Buyer (extends User)
        buyer = new Buyer();
        buyer.setId("buyer-001");
        buyer.setEmail("buyer@test.com");
        buyer.setNama("Test Buyer");
        buyer.setPassword("password");
        buyer.setTotalSpent(0.0);
        buyer = (Buyer) userRepository.save(buyer);

        // Buat Product
        testProduct = new Product();
        testProduct.setName("Vintage Sneakers");
        testProduct.setPrice(1_000_000.0);
        testProduct.setStatus("AVAILABLE");
        testProduct.setSeller(seller);
        testProduct = productRepository.save(testProduct);
    }

    // ==================== PLACE BID TESTS ====================

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("POST /api/bids/place - Should place bid successfully")
    void placeBid_WithValidRequest_ShouldReturn200() throws Exception {
        BidRequest request = new BidRequest();
        request.setProductId(testProduct.getId());
        request.setBidPrice(600_000.0);

        mockMvc.perform(post("/api/bids/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidPrice").value(600_000.0))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.buyerId").value("buyer-001"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("POST /api/bids/place - Should reject bid below 50% of original price")
    void placeBid_WithPriceTooLow_ShouldReturn400() throws Exception {
        BidRequest request = new BidRequest();
        request.setProductId(testProduct.getId());
        request.setBidPrice(400_000.0);

        mockMvc.perform(post("/api/bids/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Minimal penawaran: Rp 500000")));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("POST /api/bids/place - Should reject bid on already sold product")
    void placeBid_OnSoldProduct_ShouldReturn400() throws Exception {
        testProduct.setStatus("SOLD");
        productRepository.save(testProduct);

        BidRequest request = new BidRequest();
        request.setProductId(testProduct.getId());
        request.setBidPrice(600_000.0);

        mockMvc.perform(post("/api/bids/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("not available for bidding")));
    }

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("POST /api/bids/place - Should reject seller bidding on own product")
    void placeBid_OnOwnProduct_ShouldReturn400() throws Exception {
        BidRequest request = new BidRequest();
        request.setProductId(testProduct.getId());
        request.setBidPrice(600_000.0);

        mockMvc.perform(post("/api/bids/place")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("cannot bid on your own product")));
    }

    // ==================== ACCEPT BID TESTS ====================

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("POST /api/bids/{bidId}/accept - Should reject non-seller")
    void acceptBid_AsBuyer_ShouldReturn403() throws Exception {
        Bid bid = createTestBid();

        mockMvc.perform(post("/api/bids/{bidId}/accept", bid.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("POST /api/bids/{bidId}/accept - Seller should accept bid successfully")
    void acceptBid_AsSeller_ShouldReturn200() throws Exception {
        Bid bid = createTestBid();

        mockMvc.perform(post("/api/bids/{bidId}/accept", bid.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    // ==================== CANCEL BID TESTS ====================

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("POST /api/bids/{bidId}/cancel - Buyer should cancel own bid")
    void cancelBid_AsBuyer_ShouldReturn200() throws Exception {
        Bid bid = createTestBid();

        mockMvc.perform(post("/api/bids/{bidId}/cancel", bid.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("POST /api/bids/{bidId}/cancel - Should fail when cancelling already accepted bid")
    void cancelBid_OnAcceptedBid_ShouldReturn400() throws Exception {
        Bid bid = createTestBid();

        bid.setStatus(BidStatus.ACCEPTED);
        bidRepository.save(bid);

        mockMvc.perform(post("/api/bids/{bidId}/cancel", bid.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("no longer valid")));
    }

    // ==================== COUNTER OFFER TESTS ====================

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("POST /api/bids/counter-offer - Seller should make counter offer")
    void counterOffer_AsSeller_ShouldReturn200() throws Exception {
        Bid bid = createTestBid();

        CounterOfferRequest request = new CounterOfferRequest();
        request.setBidId(bid.getId());
        request.setCounterPrice(700_000.0);

        mockMvc.perform(post("/api/bids/counter-offer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COUNTERED"))
                .andExpect(jsonPath("$.counterOfferPrice").value(700_000.0));
    }

    // ==================== GET HIGHEST BID TESTS ====================

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("GET /api/bids/product/{productId}/highest - Should return highest bid")
    void getHighestBid_ShouldReturnHighestBid() throws Exception {
        createBidWithPrice(buyer, 600_000.0);

        Buyer anotherBuyer = new Buyer();
        anotherBuyer.setId("buyer-002");
        anotherBuyer.setEmail("buyer2@test.com");
        anotherBuyer.setNama("Another Buyer");
        anotherBuyer.setPassword("password");
        anotherBuyer.setTotalSpent(0.0);
        anotherBuyer = (Buyer) userRepository.save(anotherBuyer);
        createBidWithPrice(anotherBuyer, 650_000.0);

        mockMvc.perform(get("/api/bids/product/{productId}/highest", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bidPrice").value(650_000.0));
    }

    // ==================== BID HISTORY TESTS ====================

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("GET /api/bids/buyer/history - Should return buyer's bid history with pagination")
    void getBuyerHistory_ShouldReturnPagedResults() throws Exception {
        createBidWithPrice(buyer, 600_000.0);
        createBidWithPrice(buyer, 550_000.0);
        createBidWithPrice(buyer, 500_000.0);

        mockMvc.perform(get("/api/bids/buyer/history")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    @WithMockUser(username = "seller@test.com", roles = "SELLER")
    @DisplayName("GET /api/bids/seller/history - Should return seller's bid history")
    void getSellerHistory_AsSeller_ShouldReturnResults() throws Exception {
        createTestBid();

        mockMvc.perform(get("/api/bids/seller/history")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "buyer@test.com", roles = "BUYER")
    @DisplayName("GET /api/bids/seller/history - Should deny buyer access to seller history")
    void getSellerHistory_AsBuyer_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/bids/seller/history"))
                .andExpect(status().isForbidden());
    }

    // ==================== HELPER METHODS ====================

    private Bid createTestBid() {
        return createBidWithPrice(buyer, 600_000.0);
    }

    private Bid createBidWithPrice(User buyer, Double price) {
        Bid bid = Bid.builder()
                .bidPrice(price)
                .status(BidStatus.PENDING)
                .product(testProduct)
                .buyer(buyer)
                .build();
        return bidRepository.save(bid);
    }
}