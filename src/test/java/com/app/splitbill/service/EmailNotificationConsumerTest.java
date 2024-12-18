package com.app.splitbill.service;

import com.app.splitbill.dto.DebtDto;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.repository.GroupMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class EmailNotificationConsumerTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private BillService billService;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private EmailNotificationConsumer consumer;

    @Test
    void processEmailTask_ShouldSendEmailSuccessfully() throws Exception {
        String message = "{\"email\": \"user1@example.com\", \"username\": \"user1\"}";
        String email = "user1@example.com";
        String username = "user1";

        AppGroup group1 = new AppGroup(1L, "Group1");
        AppGroup group2 = new AppGroup(2L, "Group2");

        List<AppGroup> groups = List.of(group1, group2);

        when(groupMemberRepository.findGroupsByUsername(username)).thenReturn(groups);
        when(billService.calculateGroupDebts("Group1")).thenReturn(List.of(
                new DebtDto(username, "user2", BigDecimal.valueOf(50.0))
        ));
        when(billService.calculateGroupDebts("Group2")).thenReturn(List.of());

        consumer.processEmailTask(message);

        String expectedContent = """
                Your debts summary:
                Group: Group1 | You owe user2 an amount of 50.0
                """;

        verify(emailSender, times(1)).sendEmail(eq(email), eq("Weekly Debt Summary"), eq(expectedContent));
    }

    @Test
    void processEmailTask_ShouldHandleInvalidMessage() {
        String invalidMessage = "Invalid JSON";
        consumer.processEmailTask(invalidMessage);
        verifyNoInteractions(emailSender);
    }
}
