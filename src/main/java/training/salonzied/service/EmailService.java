package training.salonzied.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import training.salonzied.messaging.ReservationCreatedEvent;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendReservationConfirmation(ReservationCreatedEvent  event) {
        String emailClient= event.getUserEmail();
        String clientName= event.getUserName();
        if (emailClient == null || emailClient.isBlank()) {
            log.warn("Cannot send reservation confirmation email: email address is null or empty");
            return;
        }

        if (clientName == null) {
            clientName = "Client";
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailClient);
        message.setSubject("Confirmare rezervare");
        message.setText(
                "Bună " + clientName + ",\n\n" +
                        "Rezervarea ta a fost înregistrată pentru data:\n" +
                        event.getReservationDate().toString() + "\n\n" +
                        "Mulțumim că ai ales salonul nostru!"
        );

        mailSender.send(message);
        log.info("Reservation confirmation email sent to: {}", emailClient);
    }
}
