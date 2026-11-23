package com.biblioshare.notification;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {
    private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);
    private final JavaMailSender mail;
    private final ObjectMapper json;

    public EmailConsumer(JavaMailSender mail, ObjectMapper json) {
        this.mail = mail;
        this.json = json;
    }

    @Bean
    Queue emailQueue(@Value("${biblio.notifications.queue}") String queue) {
        return QueueBuilder.durable(queue).build();
    }

    private record EmailEvent(String type, String email, Long referenceId) {}

    @RabbitListener(queues = "${biblio.notifications.queue}")
    public void receive(Message message) throws IOException {
        EmailEvent event = json.readValue(message.getBody(), EmailEvent.class);
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("bibliotheque@biblio-share.local");
        email.setTo(event.email());
        switch (event.type()) {
            case "RETARD" -> {
                email.setSubject("Emprunt en retard");
                email.setText("Votre emprunt no" + event.referenceId() + " est en retard.");
            }
            case "DISPONIBILITE" -> {
                email.setSubject("Ouvrage reserve disponible");
                email.setText("Un exemplaire de l'ouvrage no" + event.referenceId() + " est disponible.");
            }
            default -> throw new IllegalArgumentException("Type de notification inconnu");
        }
        mail.send(email);
        log.info("Notification {} reference {} remise au serveur SMTP", event.type(), event.referenceId());
    }
}
