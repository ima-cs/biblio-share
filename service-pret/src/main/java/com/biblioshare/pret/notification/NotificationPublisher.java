package com.biblioshare.pret.notification;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.biblioshare.pret.service.LoanService.LateNotice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.event.TransactionalEventListener;

@Configuration(proxyBeanMethods = false)
public class NotificationPublisher {
    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);
    private final RabbitTemplate rabbit;
    private final ObjectMapper json;
    private final String queue;

    public NotificationPublisher(RabbitTemplate rabbit, ObjectMapper json,
                                 @Value("${biblio.notifications.queue:notifications.email}") String queue) {
        this.rabbit = rabbit;
        this.json = json;
        this.queue = queue;
    }

    @Bean
    Queue emailQueue() {
        return QueueBuilder.durable(queue).build();
    }

    private record EmailEvent(String type, String email, Long referenceId) {}

    @TransactionalEventListener
    public void publish(LateNotice notice) {
        EmailEvent payload = new EmailEvent("RETARD", notice.email(), notice.loanId());
        try {
            Message message = MessageBuilder.withBody(json.writeValueAsBytes(payload))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbit.send("", queue, message);
            log.info("Notification {} reference {} envoyee a RabbitMQ", payload.type(), payload.referenceId());
        } catch (JsonProcessingException | org.springframework.amqp.AmqpException failure) {
            log.error("Notification {} pour la reference {} non publiee apres commit",
                    payload.type(), payload.referenceId(), failure);
        }
    }
}
