package com.app.splitbill.service;

import com.app.splitbill.dto.DebtDto;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.repository.GroupMemberRepository;
import com.app.splitbill.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.never;

public class EmailServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BillService billService;
    @Mock
    private EmailSender emailSender;
    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private EmailService emailService;

    private AppUser user;
    private AppGroup group;
    private List<DebtDto> debts;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new AppUser();
        user.setId(1L);
        user.setUsername("max");
        user.setEmail("max@example.com");

        group = new AppGroup();
        group.setName("Group 1");

        debts = Arrays.asList(
                new DebtDto("max", "misha", BigDecimal.valueOf(2000.00)),
                new DebtDto("max", "dasha", BigDecimal.valueOf(1500.00))
        );
    }

    @Test
    void testSendWeeklyDebtNotifications_WithDebts() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(groupMemberRepository.findGroupsByUserId(user.getId())).thenReturn(List.of(group));
        when(billService.calculateGroupDebts(group.getName())).thenReturn(debts);

        emailService.sendWeeklyDebtNotifications();

        verify(emailSender, times(1)).sendEmail(
                eq(user.getEmail()),
                eq("Weekly Debt Summary"),
                contains("You owe misha an amount of 2000.0")
        );
    }

    @Test
    void testSendWeeklyDebtNotifications_NoDebts() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(groupMemberRepository.findGroupsByUserId(user.getId())).thenReturn(List.of(group));
        when(billService.calculateGroupDebts(group.getName())).thenReturn(List.of());

        emailService.sendWeeklyDebtNotifications();

        verify(emailSender, never()).sendEmail(anyString(), anyString(), anyString());
    }
    @Test
    void testSendWeeklyDebtNotifications_UserHasMultipleGroups() {
        AppGroup group1 = new AppGroup();
        group1.setName("Group 1");

        AppGroup group2 = new AppGroup();
        group2.setName("Group 2");

        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("max");
        user.setEmail("max@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user));

        when(groupMemberRepository.findGroupsByUserId(user.getId())).thenReturn(List.of(group1, group2));

        List<DebtDto> debtsForGroup1 = List.of(new DebtDto("max", "misha", BigDecimal.valueOf(2000.0)));
        List<DebtDto> debtsForGroup2 = List.of(new DebtDto("max", "dasha", BigDecimal.valueOf(3000.0)));

        when(billService.calculateGroupDebts(group1.getName())).thenReturn(debtsForGroup1);
        when(billService.calculateGroupDebts(group2.getName())).thenReturn(debtsForGroup2);

        emailService.sendWeeklyDebtNotifications();

        verify(emailSender, times(1)).sendEmail(
                eq("max@example.com"),
                eq("Weekly Debt Summary"),
                contains("You owe misha an amount of 2000.0")
        );

        verify(emailSender, times(1)).sendEmail(
                eq("max@example.com"),
                eq("Weekly Debt Summary"),
                contains("You owe dasha an amount of 3000.0")
        );
    }

    @Test
    void testSendWeeklyDebtNotifications_EmptyUserList() {
        when(userRepository.findAll()).thenReturn(List.of());

        emailService.sendWeeklyDebtNotifications();

        verify(emailSender, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
