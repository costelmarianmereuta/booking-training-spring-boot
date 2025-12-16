package training.salonzied.messaging;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import training.salonzied.config.RabbitConfig;
import training.salonzied.service.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationNotificationListener {
    private final EmailService emailService;

    @RabbitListener(queues = RabbitConfig.QUEUE_RESERVATION_CREATED)
    public void handleReservationCreated(ReservationCreatedEvent event) {
     emailService.sendReservationConfirmation(event);
        log.info("Received reservation created event: {}", event);
    }
}
