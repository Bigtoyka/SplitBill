package com.app.splitbill.service;

import com.app.splitbill.dto.DebtDto;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.repository.GroupMemberRepository;
import com.app.splitbill.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final UserRepository userRepository;
    private final BillService billService;
    private final EmailSender emailSender;
    private final GroupMemberRepository groupMemberRepository;

    @Scheduled(cron = "0 0 9 * * 1")
    public void sendWeeklyDebtNotifications() {
        log.info("Starting the process of sending weekly debt notifications.");

        try {
            List<AppUser> users = userRepository.findAll();
            log.debug("Found {} users in the database.", users.size());

            users.forEach(user -> {
                log.info("Processing debts for user: {}", user.getUsername());

                List<String> groupNames = groupMemberRepository.findGroupsByUserId(user.getId()).stream()
                        .map(AppGroup::getName)
                        .collect(Collectors.toList());
                log.debug("User {} is a member of the following groups: {}", user.getUsername(), groupNames);

                StringBuilder emailContent = new StringBuilder("Your debts summary:\n");

                groupNames.forEach(groupName -> {
                    log.debug("Calculating debts for group: {}", groupName);
                    List<DebtDto> debts = billService.calculateGroupDebts(groupName);
                    debts.stream()
                            .filter(debt -> debt.getDebtorName().equals(user.getUsername()))
                            .forEach(debt -> {
                                log.debug("Adding debt: {} owes {} an amount of {}", debt.getDebtorName(), debt.getCreditorName(), debt.getAmount());
                                emailContent.append(String.format("Group: %s | You owe %s an amount of %s%n",
                                        groupName, debt.getCreditorName(), debt.getAmount()));
                            });
                });

                if (emailContent.length() > "Your debts summary:\n".length()) {
                    log.info("Sending email to user: {}", user.getEmail());
                    emailSender.sendEmail(user.getEmail(), "Weekly Debt Summary", emailContent.toString());
                    log.info("Email sent successfully to user: {}", user.getEmail());
                } else {
                    log.info("No debts found for user: {}. No email will be sent.", user.getUsername());
                }
            });

            log.info("Finished sending weekly debt notifications.");
        } catch (Exception e) {
            log.error("An error occurred during the execution of sendWeeklyDebtNotifications job.", e);
        }
        log.info("Finished sending weekly debt notifications.");
    }
}
