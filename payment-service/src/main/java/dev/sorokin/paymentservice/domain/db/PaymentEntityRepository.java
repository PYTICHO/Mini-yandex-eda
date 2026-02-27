package dev.sorokin.paymentservice.domain.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEntityRepository extends JpaRepository<PaymentEntity, Long>{

    Optional<PaymentEntity> findByOrderId(Long orderId);

}
