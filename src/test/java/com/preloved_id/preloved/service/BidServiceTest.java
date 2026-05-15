// test/java/com/preloved_id/preloved/service/BidServiceTest.java
package com.preloved_id.preloved.service;

import com.preloved_id.preloved.exception.BidException;
import com.preloved_id.preloved.model.*;
import com.preloved_id.preloved.model.enums.BidStatus;
import com.preloved_id.preloved.repository.BidRepository;
import com.preloved_id.preloved.repository.ProductRepository;
import com.preloved_id.preloved.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BidService Unit Tests")
class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BidCacheService bidCacheService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private BidService bidService;

    private Product product;
    private Buyer buyer;
    private Seller seller;

    @BeforeEach
    void setUp() {
        // Buat Seller (extends User)
        seller = new Seller();
        seller.setId("seller-001");
        seller.setEmail("seller@test.com");
        seller.setNama("Test Seller");
        seller.setPassword("password");
        seller.setStoreName("Test Store");
        seller.setTotalSpent(0);

        // Buat Buyer (extends User)
        buyer = new Buyer();
        buyer.setId("buyer-001");
        buyer.setEmail("buyer@test.com");
        buyer.setNama("Test Buyer");
        buyer.setPassword("password");
        buyer.setTotalSpent(0.0);

        // Buat Product
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(1_000_000.0);
        product.setStatus("AVAILABLE");
        product.setSeller(seller);
    }

    // ==================== PLACE BID TESTS ====================

    @Test
    @DisplayName("Should successfully place valid bid")
    void placeBid_WithValidPrice_ShouldSucceed() {
        // Given
        Double validBidPrice = 600_000.0;

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));
        when(bidRepository.findTopByProductIdAndStatusOrderByBidPriceDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> {
            Bid bid = invocation.getArgument(0);
            bid.setId(1L);
            return bid;
        });

        // When
        Bid result = bidService.placeBid(1L, "buyer@test.com", validBidPrice);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBidPrice()).isEqualTo(validBidPrice);
        assertThat(result.getStatus()).isEqualTo(BidStatus.PENDING);

        verify(eventPublisher).publishEvent(any());
        verify(bidCacheService).clearHighestBidCache(1L);
    }

    @Test
    @DisplayName("Should reject bid below 50% of original price")
    void placeBid_WithPriceBelowMinimum_ShouldThrowException() {
        // Given
        Double tooLowPrice = 400_000.0; // 40% dari harga asli (di bawah 50%)

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));

        // When & Then
        assertThatThrownBy(() -> bidService.placeBid(1L, "buyer@test.com", tooLowPrice))
                .isInstanceOf(BidException.class)
                .hasMessageContaining("Minimal penawaran: Rp 500000");
    }

    @Test
    @DisplayName("Should reject bid on own product")
    void placeBid_OnOwnProduct_ShouldThrowException() {
        // Given
        Double validPrice = 600_000.0;

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));

        // When & Then
        assertThatThrownBy(() -> bidService.placeBid(1L, "seller@test.com", validPrice))
                .isInstanceOf(BidException.class)
                .hasMessageContaining("cannot bid on your own product");
    }

    @Test
    @DisplayName("Should reject bid on already sold product")
    void placeBid_OnSoldProduct_ShouldThrowException() {
        // Given
        product.setStatus("SOLD");
        Double validPrice = 600_000.0;

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));

        // When & Then
        assertThatThrownBy(() -> bidService.placeBid(1L, "buyer@test.com", validPrice))
                .isInstanceOf(BidException.class)
                .hasMessageContaining("not available for bidding");
    }

    @Test
    @DisplayName("Should reject bid without minimum increment")
    void placeBid_WithoutMinimumIncrement_ShouldThrowException() {
        // Given
        Double firstBid = 600_000.0;
        Double secondBid = 603_000.0; // Hanya naik Rp3.000 (harus minimal Rp5.000)

        Bid existingBid = new Bid();
        existingBid.setId(1L);
        existingBid.setBidPrice(firstBid);
        existingBid.setStatus(BidStatus.PENDING);
        existingBid.setProduct(product);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));
        when(bidRepository.findTopByProductIdAndStatusOrderByBidPriceDesc(1L, BidStatus.PENDING))
                .thenReturn(Optional.of(existingBid));

        // When & Then
        assertThatThrownBy(() -> bidService.placeBid(1L, "buyer@test.com", secondBid))
                .isInstanceOf(BidException.class)
                .hasMessageContaining("minimal harus Rp 5000 lebih tinggi");
    }

    @Test
    @DisplayName("Should reject bid when buyer has too many active bids (anti-spam)")
    void placeBid_WithTooManyActiveBids_ShouldThrowException() {
        // Given
        Double validPrice = 600_000.0;

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));
        when(bidRepository.findTopByProductIdAndStatusOrderByBidPriceDesc(any(), any()))
                .thenReturn(Optional.empty());
        when(bidRepository.countByProductIdAndBuyerIdAndStatus(1L, "buyer-001", BidStatus.PENDING))
                .thenReturn(3L); // Sudah 3 bid aktif

        // When & Then
        assertThatThrownBy(() -> bidService.placeBid(1L, "buyer@test.com", validPrice))
                .isInstanceOf(BidException.class)
                .hasMessageContaining("sudah memiliki 3 penawaran aktif");
    }

    // ==================== ACCEPT BID TESTS ====================

    @Test
    @DisplayName("Should successfully accept bid as seller")
    void acceptBid_AsSeller_ShouldSucceed() {
        // Given
        Bid bid = createPendingBid();

        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(bidRepository.findByProductIdOrderByBidPriceDesc(product.getId()))
                .thenReturn(java.util.List.of(bid));
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        // When
        Bid result = bidService.acceptBid(1L, "seller@test.com");

        // Then
        assertThat(result.getStatus()).isEqualTo(BidStatus.ACCEPTED);
        assertThat(product.getStatus()).isEqualTo("SOLD");
        verify(eventPublisher).publishEvent(any());
        verify(transactionService).createPendingTransaction(any(), any(), any());
    }

    @Test
    @DisplayName("Should reject accept bid when user is not the seller")
    void acceptBid_AsNonSeller_ShouldThrowException() {
        // Given
        Bid bid = createPendingBid();
        User anotherUser = new Buyer();
        anotherUser.setId("buyer-002");
        anotherUser.setEmail("other@test.com");

        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() -> bidService.acceptBid(1L, "other@test.com"))
                .isInstanceOf(BidException.class)
                .hasMessageContaining("Only the product seller");
    }

    // ==================== CANCEL BID TESTS ====================

    @Test
    @DisplayName("Should successfully cancel bid as buyer")
    void cancelBid_AsBuyer_ShouldSucceed() {
        // Given
        Bid bid = createPendingBid();

        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));
        when(bidRepository.save(any(Bid.class))).thenReturn(bid);

        // When
        Bid result = bidService.cancelBid(1L, "buyer@test.com");

        // Then
        assertThat(result.getStatus()).isEqualTo(BidStatus.CANCELLED);
        verify(bidCacheService).clearHighestBidCache(product.getId());
    }

    @Test
    @DisplayName("Should reject cancel bid when user is not the buyer")
    void cancelBid_AsNonBuyer_ShouldThrowException() {
        // Given
        Bid bid = createPendingBid();

        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(seller));

        // When & Then
        assertThatThrownBy(() -> bidService.cancelBid(1L, "other@test.com"))
                .isInstanceOf(BidException.class)
                .hasMessageContaining("Only the bidder");
    }

    // ==================== GET HIGHEST BID TESTS ====================

    @Test
    @DisplayName("Should return highest bid from cache")
    void getHighestBid_ShouldReturnFromCache() {
        // Given
        Bid highestBid = createPendingBid();
        highestBid.setBidPrice(650_000.0);

        when(bidCacheService.getHighestBidWithCache(product.getId())).thenReturn(highestBid);

        // When
        Bid result = bidService.getHighestBid(product.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBidPrice()).isEqualTo(650_000.0);
        verify(bidCacheService).getHighestBidWithCache(product.getId());
        verifyNoInteractions(bidRepository);
    }

    // ==================== HELPER METHODS ====================

    private Bid createPendingBid() {
        Bid bid = new Bid();
        bid.setId(1L);
        bid.setBidPrice(600_000.0);
        bid.setStatus(BidStatus.PENDING);
        bid.setProduct(product);
        bid.setBuyer(buyer);
        return bid;
    }
}