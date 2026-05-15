package com.preloved_id.preloved.model.enums;

public enum BidStatus {
    PENDING, // Menunggu respon seller
    ACCEPTED, // Diterima, produk terjual
    REJECTED, // Ditolak seller
    EXPIRED, // Kadaluarsa (48 jam)
    COUNTERED, // Seller memberi harga balik
    CANCELLED // Dibatalkan pembeli
}