package dev.sorokin.deliveryservice.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryEntityRepository extends JpaRepository<DeliveryEntity, Long> {

    Optional<DeliveryEntity> findByOrderId(Long orderId);

}
