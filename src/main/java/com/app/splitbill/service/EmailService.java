package com.app.splitbill.service;

import com.app.splitbill.config.RabbitMQConfig;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RabbitTemplate rabbitTemplate;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 33 21 * * 1")
    public void scheduleEmailNotifications() {
        log.info("Preparing to send email notifications.");
        List<AppUser> users = userRepository.findAll();
        log.debug("Found {} users.", users.size());

        users.forEach(user -> {
            try {
                String emailTask = prepareEmailTask(user);
                rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE, emailTask);
                log.info("Task for user {} added to queue.", user.getUsername());
            } catch (Exception e) {
                log.error("Failed to prepare email task for user {}: {}", user.getUsername(), e.getMessage());
            }
        });
    }

    private String prepareEmailTask(AppUser user) {
        return String.format("{\"email\": \"%s\", \"username\": \"%s\"}", user.getEmail(), user.getUsername());
    }
}
