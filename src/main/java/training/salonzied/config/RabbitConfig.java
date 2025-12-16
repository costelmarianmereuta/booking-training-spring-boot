package training.salonzied.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "reservation.exchange";
    public static final String QUEUE_RESERVATION_CREATED = "reservation.notifications";
    public static final String ROUTING_KEY_RESERVATION_CREATED = "reservation.created";

    @Bean
    public TopicExchange reservationExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange("reservation.dlx", true, false);
    }

    public Queue reservationCreatedQueue() {
        return QueueBuilder.durable(QUEUE_RESERVATION_CREATED)
                .withArgument("x-dead-letter-exchange", "reservation.dlx")
                .withArgument("x-dead-letter-routing-key", "reservation.created.dlq")
                .build();
    }

    @Bean
    public Binding reservationBinding() {
        return BindingBuilder.bind(reservationCreatedQueue())
                .to(reservationExchange())
                .with(ROUTING_KEY_RESERVATION_CREATED);
    }

    public Queue reservationCreatedDLQ() {
        return QueueBuilder.durable("reservation.notifications.dlq").build();
    }

    @Bean
    public Binding reservationDLQBinding() {
        return BindingBuilder.bind(reservationCreatedDLQ())
                .to(deadLetterExchange())
                .with("reservation.created.dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
