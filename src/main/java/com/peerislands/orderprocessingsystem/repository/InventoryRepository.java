package com.peerislands.orderprocessingsystem.repository;

import com.peerislands.orderprocessingsystem.domain.model.InventoryItem;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryItem i where i.productCode = :productCode")
    Optional<InventoryItem> findByProductCodeForUpdate(@Param("productCode") String productCode);

    Optional<InventoryItem> findByProductCode(String productCode);
}

