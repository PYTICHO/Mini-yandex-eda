package dev.sorokin.deliveryservice.kafka;


import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

import dev.sorokin.api.kafka.OrderPaidEvent;

import dev.sorokin.deliveryservice.domain.DeliveryProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableKafka
@Configuration
@AllArgsConstructor
public class OrderPaidKafkaConsumer {

    private final DeliveryProcessor deliveryProcessor;

    @KafkaListener(
        topics = "${order-paid-topic}",
        containerFactory = "orderPaidEventListenerFactory"
    )
    public void listen(OrderPaidEvent event) {
        log.info("Recieved order paid event: {}", event);

        deliveryProcessor.processOrderPaid(event);
    }


}
