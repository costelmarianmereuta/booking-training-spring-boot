package training.salonzied.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import training.salonzied.config.RabbitConfig;

@Component
public class ReservationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ReservationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
//todo add retry
    public void publishReservationCreated(ReservationCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY_RESERVATION_CREATED,
                event
        );
    }
}
