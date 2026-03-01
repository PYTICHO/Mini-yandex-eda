package dev.sorokin.deliveryservice.domain;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import dev.sorokin.api.kafka.DeliveryAssignedEvent;
import dev.sorokin.api.kafka.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class DeliveryProcessor {

    private final DeliveryEntityRepository repository;
    private final KafkaTemplate<Long, DeliveryAssignedEvent> kafkaTemplate;

    @Value("${delivery-assigned-topic}")
    private String deliveryAssignedTopic;

    public void processOrderPaid(OrderPaidEvent event) {
        var orderId = event.orderId();
        var found = repository.findByOrderId(orderId);

        if (found.isPresent()) {
            log.info("Found order delivery was already assigned: delivery={}", found.get());
            return;
        }

        var assignedDelivery = assignDelivery(orderId);
        sendDeliveryAssignedEvent(assignedDelivery);
    }

    private void sendDeliveryAssignedEvent(DeliveryEntity assignDelivery) {
        kafkaTemplate.send(
            deliveryAssignedTopic,
            assignDelivery.getOrderId(),
            DeliveryAssignedEvent.builder()
                    .courierName(assignDelivery.getCourierName())
                    .orderId(assignDelivery.getOrderId())
                    .etaMinutes(assignDelivery.getEtaMinutes())
                    .build()
        ).thenAccept(result -> {
            log.info("delivery assigned event sent: delivery={}", assignDelivery.getId());
        });
    }

    private DeliveryEntity assignDelivery(Long orderId) {
        var entity = new DeliveryEntity();
        entity.setOrderId(orderId);
        entity.setCourierName("courier-" + ThreadLocalRandom.current().nextInt(100));
        entity.setEtaMinutes(ThreadLocalRandom.current().nextInt(10, 45));
        log.info("saved order delivery was assigned: delivery={}", entity);

        return repository.save(entity);
    }

}
