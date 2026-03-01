package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.model.WarehouseProduct;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, UUID> {

    @Transactional(readOnly = true)
    @Query("SELECT wp.quantity FROM WarehouseProduct wp WHERE wp.productId = :productId")
    Optional<Long> findQuantityByProductId(@Param("productId") UUID productId);

    @Modifying
    @Transactional
    @Query("UPDATE WarehouseProduct wp SET wp.quantity = wp.quantity + :delta WHERE wp.productId = :productId")
    int updateQuantity(@Param("productId") UUID productId, @Param("delta") Long delta);
}
