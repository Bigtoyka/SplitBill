package com.app.splitbill.service;

import com.app.splitbill.config.RabbitMQConfig;
import com.app.splitbill.dto.DebtDto;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.repository.GroupMemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EmailNotificationConsumer {

    private final EmailSender emailSender;
    private final BillService billService;
    private final GroupMemberRepository groupMemberRepository;
    private static final String DEBT_SUMMARY_HEADER = "Your debts summary:\n";

    @Autowired
    public EmailNotificationConsumer(EmailSender emailSender, BillService billService, GroupMemberRepository groupMemberRepository) {
        this.emailSender = emailSender;
        this.billService = billService;
        this.groupMemberRepository = groupMemberRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE)
    public void processEmailTask(String message) {
        try {
            log.info("Received email task: {}", message);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> task = objectMapper.readValue(message, new TypeReference<>() {
            });

            String email = task.get("email");
            String username = task.get("username");

            String emailContent = generateDebtSummary(username);

            emailSender.sendEmail(email, "Weekly Debt Summary", emailContent);
            log.info("Email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to process email task: {}", e.getMessage(), e);
        }
    }

    private String generateDebtSummary(String username) {
        StringBuilder content = new StringBuilder(DEBT_SUMMARY_HEADER);

        List<String> groupNames = groupMemberRepository.findGroupsByUsername(username).stream()
                .map(AppGroup::getName)
                .collect(Collectors.toList());

        groupNames.forEach(groupName -> {
            List<DebtDto> debts = billService.calculateGroupDebts(groupName);
            debts.stream()
                    .filter(debt -> debt.getDebtorName().equals(username))
                    .forEach(debt -> content.append(
                            String.format("Group: %s | You owe %s an amount of %s%n",
                                    groupName, debt.getCreditorName(), debt.getAmount())
                    ));
        });

        return content.toString();
    }
}
