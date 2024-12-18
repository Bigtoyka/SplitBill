package com.app.splitbill.service;

import com.app.splitbill.config.RabbitMQConfig;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)

public class EmailServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailService emailService;

    @Test
    void scheduleEmailNotifications_ShouldSendTasksToQueue() {
        AppUser user1 = new AppUser(1L, "user1", "user1@example.com", "password");
        AppUser user2 = new AppUser(2L, "user2", "user2@example.com", "password");
        List<AppUser> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        emailService.scheduleEmailNotifications();

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE),
                eq("{\"email\": \"user1@example.com\", \"username\": \"user1\"}")
        );
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE),
                eq("{\"email\": \"user2@example.com\", \"username\": \"user2\"}")
        );
    }
}

