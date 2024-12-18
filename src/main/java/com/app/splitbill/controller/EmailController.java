package com.app.splitbill.controller;

import com.app.splitbill.config.RabbitMQConfig;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final RabbitTemplate rabbitTemplate;
    private final GroupMemberRepository groupMemberRepository;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmailForGroup(@RequestParam String groupName) {
        log.info("Received request to send email notifications for group: {}", groupName);

        List<AppUser> groupMembers = groupMemberRepository.findUsersByGroupName(groupName);

        if (groupMembers.isEmpty()) {
            log.warn("No members found for group: {}", groupName);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No members found for the specified group.");
        }

        groupMembers.forEach(user -> {
            String emailTask = prepareEmailTask(user);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE, emailTask);
            log.info("Email task for user {} added to queue.", user.getUsername());
        });

        return ResponseEntity.ok("Email notifications have been successfully queued for group: " + groupName);
    }

    private String prepareEmailTask(AppUser user) {
        return String.format("{\"email\": \"%s\", \"username\": \"%s\"}", user.getEmail(), user.getUsername());
    }
}
