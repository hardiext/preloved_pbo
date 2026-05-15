// service/BidService.java
package com.preloved_id.preloved.service;

import com.preloved_id.preloved.dto.CounterOfferRequest;
import com.preloved_id.preloved.event.BidAcceptedEvent;
import com.preloved_id.preloved.event.BidPlacedEvent;
import com.preloved_id.preloved.exception.BidException;
import com.preloved_id.preloved.model.Bid;
import com.preloved_id.preloved.model.Product;
import com.preloved_id.preloved.model.User;
import com.preloved_id.preloved.model.enums.BidStatus;
import com.preloved_id.preloved.repository.BidRepository;
import com.preloved_id.preloved.repository.ProductRepository;
import com.preloved_id.preloved.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    // ==================== BUSINESS RULES CONSTANTS ====================

    /** Maksimal potongan harga 50% dari harga asli */
    private static final double MAX_DISCOUNT_PERCENTAGE = 0.5;

    /** Minimal kenaikan bid Rp5.000 */
    private static final double MIN_BID_INCREMENT = 5000.0;

    /** Maksimal bid aktif per produk (anti-spam) */
    private static final int MAX_ACTIVE_BIDS_PER_PRODUCT = 3;

    // ==================== DEPENDENCIES ====================

    private final BidRepository bidRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BidCacheService bidCacheService;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionService transactionService;

    // ==================== PUBLIC METHODS ====================

    /**
     * Membuat penawaran baru (place bid)
     * 
     * @param productId  ID produk yang ditawar
     * @param buyerEmail Email pembeli yang melakukan penawaran
     * @param bidPrice   Harga yang ditawarkan
     * @return Bid yang berhasil disimpan
     * @throws BidException jika validasi gagal
     */
    @Transactional
    public Bid placeBid(Long productId, String buyerEmail, Double bidPrice) {
        Product product = getProductById(productId);
        User buyer = getUserByEmail(buyerEmail);

        // Validasi
        validateProductAvailability(product);
        validateSelfBid(product, buyer);
        validateBidPrice(product, bidPrice);
        validateMinimumIncrement(productId, bidPrice);
        validateActiveBidsLimit(productId, buyer.getId());

        try {
            // Proses bisnis
            rejectLowerBids(productId, bidPrice);
            Bid newBid = createAndSaveBid(product, buyer, bidPrice);

            // Update cache dan publish event
            bidCacheService.clearHighestBidCache(productId);
            eventPublisher.publishEvent(new BidPlacedEvent(this, newBid));

            log.info("Bid placed successfully - productId: {}, buyer: {}, price: {}",
                    productId, buyerEmail, bidPrice);
            return newBid;

        } catch (OptimisticLockingFailureException e) {
            throw new BidException("Harga penawaran sudah berubah, silakan muat ulang halaman.");
        }
    }

    /**
     * Seller menerima penawaran (accept bid)
     * 
     * @param bidId       ID penawaran yang diterima
     * @param sellerEmail Email seller yang menerima penawaran
     * @return Bid yang sudah di-ACCEPTED
     * @throws BidException jika validasi gagal
     */
    @Transactional
    public Bid acceptBid(Long bidId, String sellerEmail) {
        Bid bid = getBidById(bidId);
        validateSellerOwnership(bid, sellerEmail);
        validateBidPending(bid);

        try {
            // Terima bid ini
            bid.setStatus(BidStatus.ACCEPTED);

            // Tolak semua bid lain untuk produk yang sama
            rejectAllOtherBids(bid.getProduct().getId(), bidId);

            // Ubah status produk menjadi SOLD
            markProductAsSold(bid.getProduct());

            Bid acceptedBid = bidRepository.save(bid);

            // Update cache dan publish event
            bidCacheService.clearHighestBidCache(bid.getProduct().getId());
            eventPublisher.publishEvent(new BidAcceptedEvent(this, acceptedBid));

            // Trigger pembuatan transaksi (integrasi dengan modul Maleachi)
            transactionService.createPendingTransaction(
                    acceptedBid.getBuyer(),
                    acceptedBid.getProduct(),
                    acceptedBid.getBidPrice());

            log.info("Bid accepted - bidId: {}, productId: {}, buyer: {}",
                    bidId, bid.getProduct().getId(), bid.getBuyer().getEmail());
            return acceptedBid;

        } catch (OptimisticLockingFailureException e) {
            throw new BidException("Data sudah berubah, silakan coba lagi.");
        }
    }

    /**
     * Pembeli membatalkan penawaran (cancel bid)
     * 
     * @param bidId      ID penawaran yang dibatalkan
     * @param buyerEmail Email pembeli yang membatalkan
     * @return Bid yang sudah di-CANCELLED
     * @throws BidException jika validasi gagal
     */
    @Transactional
    public Bid cancelBid(Long bidId, String buyerEmail) {
        Bid bid = getBidById(bidId);
        validateBuyerOwnership(bid, buyerEmail);
        validateBidPending(bid);

        bid.setStatus(BidStatus.CANCELLED);
        bidCacheService.clearHighestBidCache(bid.getProduct().getId());

        log.info("Bid cancelled - bidId: {}", bidId);
        return bidRepository.save(bid);
    }

    /**
     * Seller memberikan harga balik (counter offer)
     * 
     * @param request     Request berisi bidId dan harga counter
     * @param sellerEmail Email seller
     * @return Bid yang sudah di-COUNTERED
     * @throws BidException jika validasi gagal
     */
    @Transactional
    public Bid counterOffer(CounterOfferRequest request, String sellerEmail) {
        Bid originalBid = getBidById(request.getBidId());
        validateSellerOwnership(originalBid, sellerEmail);
        validateCounterOfferPrice(originalBid.getProduct(), request.getCounterPrice());

        originalBid.setStatus(BidStatus.COUNTERED);
        originalBid.setCounterOfferPrice(request.getCounterPrice());

        log.info("Counter offer made - bidId: {}, counterPrice: {}",
                request.getBidId(), request.getCounterPrice());
        return bidRepository.save(originalBid);
    }

    /**
     * Menghapus bid yang kadaluarsa (dijalankan otomatis setiap jam)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireOldBids() {
        int expiredCount = bidRepository.expireOldBids(LocalDateTime.now());
        if (expiredCount > 0) {
            log.info("Expired {} old bids", expiredCount);
        }
    }

    /**
     * Mendapatkan bid tertinggi untuk suatu produk (dengan cache)
     * 
     * @param productId ID produk
     * @return Bid tertinggi atau null jika tidak ada
     */
    public Bid getHighestBid(Long productId) {
        return bidCacheService.getHighestBidWithCache(productId);
    }

    /**
     * Mendapatkan riwayat bid untuk pembeli tertentu (dengan pagination)
     * 
     * @param buyerEmail Email pembeli
     * @param page       Halaman (dimulai dari 0)
     * @param size       Jumlah per halaman
     * @return Page of Bid
     */
    public Page<Bid> getBuyerBidHistory(String buyerEmail, int page, int size) {
        User buyer = getUserByEmail(buyerEmail);
        return bidRepository.findByBuyerIdOrderByCreatedAtDesc(buyer.getId(), PageRequest.of(page, size));
    }

    /**
     * Mendapatkan riwayat bid untuk seller tertentu (dengan pagination)
     * 
     * @param sellerEmail Email seller
     * @param page        Halaman (dimulai dari 0)
     * @param size        Jumlah per halaman
     * @return Page of Bid
     */
    public Page<Bid> getSellerBidHistory(String sellerEmail, int page, int size) {
        User seller = getUserByEmail(sellerEmail);
        return bidRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId(), PageRequest.of(page, size));
    }

    // ==================== PRIVATE VALIDATION METHODS ====================

    /**
     * Ambil produk berdasarkan ID
     */
    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BidException("Product not found"));
    }

    /**
     * Ambil user berdasarkan email
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BidException("User not found"));
    }

    /**
     * Ambil bid berdasarkan ID
     */
    private Bid getBidById(Long bidId) {
        return bidRepository.findById(bidId)
                .orElseThrow(() -> new BidException("Bid not found"));
    }

    /**
     * Validasi: Produk harus tersedia untuk bidding
     */
    private void validateProductAvailability(Product product) {
        if (!"AVAILABLE".equals(product.getStatus())) {
            throw new BidException("Product is not available for bidding");
        }
    }

    /**
     * Validasi: Pembeli tidak boleh bid ke produk sendiri
     * Seller extends User, sehingga seller.getId() valid
     */
    private void validateSelfBid(Product product, User buyer) {
        String sellerUserId = product.getSeller().getId();
        if (sellerUserId.equals(buyer.getId())) {
            throw new BidException("You cannot bid on your own product");
        }
    }

    /**
     * Validasi: Harga bid minimal 50% dari harga asli (maksimal potongan 50%)
     * dan tidak boleh melebihi harga asli
     */
    private void validateBidPrice(Product product, Double bidPrice) {
        double minAllowedBid = product.getPrice() * (1 - MAX_DISCOUNT_PERCENTAGE);

        if (bidPrice < minAllowedBid) {
            throw new BidException(String.format(
                    "Penawaran terlalu rendah. Maksimal potongan harga adalah 50%%. Minimal penawaran: Rp %.0f",
                    minAllowedBid));
        }

        if (bidPrice > product.getPrice()) {
            throw new BidException("Penawaran tidak boleh melebihi harga asli produk");
        }
    }

    /**
     * Validasi: Harga bid minimal harus lebih tinggi Rp5.000 dari bid tertinggi
     */
    private void validateMinimumIncrement(Long productId, Double bidPrice) {
        Bid highestBid = bidRepository.findTopByProductIdAndStatusOrderByBidPriceDesc(productId, BidStatus.PENDING)
                .orElse(null);

        if (highestBid != null && bidPrice < highestBid.getBidPrice() + MIN_BID_INCREMENT) {
            throw new BidException(String.format(
                    "Penawaran minimal harus Rp %.0f lebih tinggi dari penawaran tertinggi (Rp %.0f)",
                    MIN_BID_INCREMENT, highestBid.getBidPrice()));
        }
    }

    /**
     * Validasi anti-spam: Maksimal 3 bid aktif per produk per user
     */
    private void validateActiveBidsLimit(Long productId, String buyerId) {
        long activeBids = bidRepository.countByProductIdAndBuyerIdAndStatus(productId, buyerId, BidStatus.PENDING);
        if (activeBids >= MAX_ACTIVE_BIDS_PER_PRODUCT) {
            throw new BidException("Anda sudah memiliki " + activeBids + " penawaran aktif untuk produk ini");
        }
    }

    /**
     * Validasi: Hanya seller produk yang bisa melakukan aksi (accept/counter)
     * Seller extends User, sehingga seller.getId() valid
     */
    private void validateSellerOwnership(Bid bid, String sellerEmail) {
        User seller = getUserByEmail(sellerEmail);
        String sellerUserId = bid.getProduct().getSeller().getId();
        if (!sellerUserId.equals(seller.getId())) {
            throw new BidException("Only the product seller can perform this action");
        }
    }

    /**
     * Validasi: Hanya pembeli yang melakukan bid yang bisa membatalkan
     */
    private void validateBuyerOwnership(Bid bid, String buyerEmail) {
        User buyer = getUserByEmail(buyerEmail);
        if (!bid.getBuyer().getId().equals(buyer.getId())) {
            throw new BidException("Only the bidder can perform this action");
        }
    }

    /**
     * Validasi: Bid harus dalam status PENDING untuk di-accept/cancel
     */
    private void validateBidPending(Bid bid) {
        if (bid.getStatus() != BidStatus.PENDING) {
            throw new BidException("This bid is no longer valid (status: " + bid.getStatus() + ")");
        }
    }

    /**
     * Validasi: Counter offer tidak boleh di bawah minimal dan tidak boleh melebihi
     * harga asli
     */
    private void validateCounterOfferPrice(Product product, Double counterPrice) {
        double minAllowed = product.getPrice() * (1 - MAX_DISCOUNT_PERCENTAGE);
        if (counterPrice < minAllowed) {
            throw new BidException("Counter offer cannot be lower than " + minAllowed);
        }
        if (counterPrice > product.getPrice()) {
            throw new BidException("Counter offer cannot exceed original price");
        }
    }

    // ==================== PRIVATE BUSINESS METHODS ====================

    /**
     * Tolak semua bid yang lebih rendah dari bid baru
     */
    private void rejectLowerBids(Long productId, Double newBidPrice) {
        List<Bid> lowerBids = bidRepository.findByProductIdOrderByBidPriceDesc(productId)
                .stream()
                .filter(bid -> bid.getBidPrice() < newBidPrice && bid.getStatus() == BidStatus.PENDING)
                .toList();

        lowerBids.forEach(bid -> bid.setStatus(BidStatus.REJECTED));
        bidRepository.saveAll(lowerBids);

        if (!lowerBids.isEmpty()) {
            log.info("Rejected {} lower bids for productId: {}", lowerBids.size(), productId);
        }
    }

    /**
     * Tolak semua bid lain untuk produk yang sama (selain yang di-accept)
     */
    private void rejectAllOtherBids(Long productId, Long acceptedBidId) {
        List<Bid> otherBids = bidRepository.findByProductIdOrderByBidPriceDesc(productId)
                .stream()
                .filter(bid -> !bid.getId().equals(acceptedBidId) && bid.getStatus() == BidStatus.PENDING)
                .toList();

        otherBids.forEach(bid -> bid.setStatus(BidStatus.REJECTED));
        bidRepository.saveAll(otherBids);

        if (!otherBids.isEmpty()) {
            log.info("Rejected {} other bids for productId: {}", otherBids.size(), productId);
        }
    }

    /**
     * Ubah status produk menjadi SOLD
     */
    private void markProductAsSold(Product product) {
        product.setStatus("SOLD");
        productRepository.save(product);
        log.info("Product marked as SOLD - productId: {}", product.getId());
    }

    /**
     * Buat dan simpan entity Bid baru
     */
    private Bid createAndSaveBid(Product product, User buyer, Double bidPrice) {
        Bid newBid = Bid.builder()
                .bidPrice(bidPrice)
                .status(BidStatus.PENDING)
                .product(product)
                .buyer(buyer)
                .build();
        return bidRepository.save(newBid);
    }
}