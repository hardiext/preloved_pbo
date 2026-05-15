package com.preloved_id.preloved.service;

import com.preloved_id.preloved.model.Product;
import com.preloved_id.preloved.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnMissingBean(name = "transactionService")
public class TransactionService {

    public void createPendingTransaction(User buyer, Product product, Double finalPrice) {
        log.info("Creating pending transaction: buyer={}, product={}, price={}",
                buyer.getEmail(), product.getId(), finalPrice);
    }
}