package com.app.splitbill.service;

import com.app.splitbill.dto.BillParticipantRequestDto;
import com.app.splitbill.dto.BillRequestDto;
import com.app.splitbill.dto.DebtDto;
import com.app.splitbill.dto.SharedBillItemRequestDto;
import com.app.splitbill.exception.ResourceNotFoundException;
import com.app.splitbill.exception.ValidationException;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.Bill;
import com.app.splitbill.model.BillParticipant;
import com.app.splitbill.model.BillItem;
import com.app.splitbill.model.GroupMember;

import com.app.splitbill.repository.BillRepository;
import com.app.splitbill.repository.BillParticipantRepository;
import com.app.splitbill.repository.BillItemRepository;
import com.app.splitbill.repository.GroupMemberRepository;
import com.app.splitbill.repository.GroupRepository;
import com.app.splitbill.repository.UserRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;


public class BillServiceTest {
    @Mock
    private BillRepository billRepository;
    @Mock
    private BillItemRepository billItemRepository;

    @Mock
    private BillParticipantRepository billParticipantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private BillService billService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBillFromRequest_ShouldCreateBillSuccessfully() {
        BillRequestDto billRequestDto = new BillRequestDto();
        billRequestDto.setCreatedBy("user1");
        billRequestDto.setMainPayer("user2");
        billRequestDto.setAppGroup("group1");
        billRequestDto.setTotalAmount(BigDecimal.valueOf(100));
        billRequestDto.setDescription("Test bill");
        billRequestDto.setDate(LocalDate.now());

        AppUser createdBy = new AppUser();
        createdBy.setUsername("user1");

        AppUser mainPayer = new AppUser();
        mainPayer.setUsername("user2");

        AppGroup appGroup = new AppGroup();
        appGroup.setName("group1");

        Bill savedBill = new Bill();
        savedBill.setId(1L);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(createdBy));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(mainPayer));
        when(groupRepository.findByName("group1")).thenReturn(Optional.of(appGroup));
        when(billRepository.save(any(Bill.class))).thenReturn(savedBill);

        Bill result = billService.createBillFromRequest(billRequestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findByUsername("user1");
        verify(userRepository).findByUsername("user2");
        verify(groupRepository).findByName("group1");
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void createBillFromRequest_ShouldThrowException_WhenUserNotFound() {
        BillRequestDto billRequestDto = new BillRequestDto();
        billRequestDto.setCreatedBy("notexistentuser");

        when(userRepository.findByUsername("notexistentuser")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                billService.createBillFromRequest(billRequestDto));

        assertEquals("User not found: notexistentuser", exception.getMessage());
        verify(userRepository).findByUsername("notexistentuser");
        verifyNoInteractions(billRepository);
    }

    @Test
    void GetBillById_Success() {
        Long billId = 1L;
        Bill mockBill = new Bill();
        mockBill.setId(billId);
        mockBill.setDescription("Test Bill");
        when(billRepository.findById(billId)).thenReturn(Optional.of(mockBill));

        Bill result = billService.getBillById(billId);

        assertNotNull(result);
        assertEquals(billId, result.getId());
        assertEquals("Test Bill", result.getDescription());
        verify(billRepository, Mockito.times(1)).findById(billId);
    }

    @Test
    void GetBillById_BillNotFound() {
        Long billId = 1L;
        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            billService.getBillById(billId);
        });

        assertEquals("Bill not found with id: 1", exception.getMessage());
        verify(billRepository, Mockito.times(1)).findById(billId);
    }

    @Test
    void AddBillItem_Success() {
        Long billId = 1L;
        Bill bill = new Bill();
        bill.setId(billId);

        BillItem billItem = new BillItem();
        billItem.setItemName("Item 1");
        billItem.setItemPrice(new BigDecimal("10.00"));

        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));
        when(billItemRepository.save(any(BillItem.class))).thenAnswer(invocation -> {
            BillItem savedItem = invocation.getArgument(0);
            savedItem.setId(1L);
            return savedItem;
        });

        BillItem result = billService.addBillItem(billId, billItem);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Item 1", result.getItemName());
        verify(billRepository, times(1)).findById(billId);
        verify(billItemRepository, times(1)).save(billItem);
    }

    @Test
    void AddBillItem_BillNotFound() {
        Long billId = 1L;
        BillItem billItem = new BillItem();

        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                billService.addBillItem(billId, billItem));

        assertEquals("Bill not found with id: 1", exception.getMessage());
        verify(billRepository, times(1)).findById(billId);
        verifyNoInteractions(billItemRepository);
    }

    @Test
    void addBillParticipantByDetails_ShouldAddParticipant_WhenDataIsValid() {
        BillParticipantRequestDto participantDto = new BillParticipantRequestDto(
                1L, "item1", "group1", "user1", BigDecimal.valueOf(100), BigDecimal.valueOf(50)
        );

        AppGroup appGroup = new AppGroup();
        appGroup.setName("group1");

        Bill bill = new Bill();
        bill.setId(1L);
        bill.setAppGroup(appGroup);

        BillItem billItem = new BillItem();
        billItem.setId(1L);
        billItem.setItemName("item1");

        AppUser appUser = new AppUser();
        appUser.setUsername("user1");

        BillParticipant savedParticipant = new BillParticipant();
        savedParticipant.setId(1L);
        savedParticipant.setBillItem(billItem);
        savedParticipant.setAppUser(appUser);
        savedParticipant.setAmountOwed(BigDecimal.valueOf(100));
        savedParticipant.setAmountPaid(BigDecimal.valueOf(50));

        when(groupRepository.findByName(participantDto.getGroupName())).thenReturn(Optional.of(appGroup));
        when(billRepository.findById(participantDto.getBillId())).thenReturn(Optional.of(bill));
        when(billItemRepository.findByItemNameAndBill_AppGroup(participantDto.getItemName(), appGroup)).thenReturn(Optional.of(billItem));
        when(userRepository.findByUsername(participantDto.getUsername())).thenReturn(Optional.of(appUser));
        when(billParticipantRepository.save(any(BillParticipant.class))).thenReturn(savedParticipant);

        BillParticipant result = billService.addBillParticipantByDetails(participantDto);

        assertNotNull(result);
        assertEquals(savedParticipant.getId(), result.getId());
        assertEquals(savedParticipant.getAmountOwed(), result.getAmountOwed());
        assertEquals(savedParticipant.getAmountPaid(), result.getAmountPaid());
        assertEquals(savedParticipant.getAppUser(), result.getAppUser());
        assertEquals(savedParticipant.getBillItem(), result.getBillItem());

        verify(groupRepository).findByName(participantDto.getGroupName());
        verify(billRepository).findById(participantDto.getBillId());
        verify(billItemRepository).findByItemNameAndBill_AppGroup(participantDto.getItemName(), appGroup);
        verify(userRepository).findByUsername(participantDto.getUsername());
        verify(billParticipantRepository).save(any(BillParticipant.class));
    }

    @Test
    void addBillParticipantByDetails_ShouldThrowException_WhenGroupNotFound() {
        BillParticipantRequestDto participantDto = new BillParticipantRequestDto(
                1L, "item1", "group1", "user1", BigDecimal.valueOf(100), BigDecimal.valueOf(50)
        );

        when(groupRepository.findByName(participantDto.getGroupName())).thenReturn(Optional.empty());


        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                billService.addBillParticipantByDetails(participantDto));

        assertEquals("Group not found", exception.getMessage());

        verify(groupRepository).findByName(participantDto.getGroupName());
        verifyNoInteractions(billRepository, billItemRepository, userRepository, billParticipantRepository);
    }

    @Test
    void addBillParticipantByDetails_ShouldThrowException_WhenBillNotFound() {
        BillParticipantRequestDto participantDto = new BillParticipantRequestDto(
                1L, "item1", "group1", "user1", BigDecimal.valueOf(100), BigDecimal.valueOf(50)
        );

        AppGroup appGroup = new AppGroup();
        appGroup.setName("group1");

        when(groupRepository.findByName(participantDto.getGroupName())).thenReturn(Optional.of(appGroup));
        when(billRepository.findById(participantDto.getBillId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                billService.addBillParticipantByDetails(participantDto));

        assertEquals("Bill not found", exception.getMessage());

        verify(groupRepository).findByName(participantDto.getGroupName());
        verify(billRepository).findById(participantDto.getBillId());
        verifyNoInteractions(billItemRepository, userRepository, billParticipantRepository);
    }

    @Test
    void calculateDebtBasedOnConsumption_ShouldReturnCorrectDebts_WhenBillHasParticipants() {
        Long billId = 1L;
        Bill bill = new Bill();
        bill.setId(billId);

        AppUser mainPayer = new AppUser();
        mainPayer.setUsername("mainPayer");
        bill.setMainPayer(mainPayer);

        BillItem item1 = new BillItem();
        item1.setId(1L);

        BillItem item2 = new BillItem();
        item2.setId(2L);

        AppUser user1 = new AppUser();
        user1.setUsername("user1");

        AppUser user2 = new AppUser();
        user2.setUsername("user2");

        BillParticipant participant1 = new BillParticipant();
        participant1.setAppUser(user1);
        participant1.setAmountOwed(BigDecimal.valueOf(100));
        participant1.setAmountPaid(BigDecimal.valueOf(50));
        participant1.setBillItem(item1);

        BillParticipant participant2 = new BillParticipant();
        participant2.setAppUser(user2);
        participant2.setAmountOwed(BigDecimal.valueOf(200));
        participant2.setAmountPaid(BigDecimal.valueOf(0));
        participant2.setBillItem(item2);

        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));
        when(billItemRepository.findByBillId(billId)).thenReturn(Arrays.asList(item1, item2));
        when(billParticipantRepository.findByBillItemId(item1.getId())).thenReturn(Arrays.asList(participant1));
        when(billParticipantRepository.findByBillItemId(item2.getId())).thenReturn(Arrays.asList(participant2));
        when(billParticipantRepository.findTotalPaidByUserAndBill(user1.getId(), billId)).thenReturn(BigDecimal.valueOf(50));
        when(billParticipantRepository.findTotalPaidByUserAndBill(user2.getId(), billId)).thenReturn(BigDecimal.ZERO);

        List<DebtDto> debts = billService.calculateDebtBasedOnConsumption(billId);

        assertNotNull(debts);
        assertEquals(2, debts.size());
        Map<String, DebtDto> debtMap = debts.stream().collect(Collectors.toMap(DebtDto::getDebtorName, debt -> debt));

        DebtDto debt1 = debtMap.get("user1");
        assertNotNull(debt1);
        assertEquals("mainPayer", debt1.getCreditorName());
        assertEquals(BigDecimal.valueOf(100), debt1.getAmount());

        DebtDto debt2 = debtMap.get("user2");
        assertNotNull(debt2);
        assertEquals("mainPayer", debt2.getCreditorName());
        assertEquals(BigDecimal.valueOf(200), debt2.getAmount());

        verify(billRepository).findById(billId);
        verify(billItemRepository).findByBillId(billId);
        verify(billParticipantRepository).findByBillItemId(item1.getId());
        verify(billParticipantRepository).findByBillItemId(item2.getId());
    }

    @Test
    void calculateGroupDebts_ShouldReturnCorrectDebts_WhenGroupHasMultipleBills() {
        String groupName = "TestGroup";
        AppGroup group = new AppGroup();
        group.setName(groupName);

        AppUser user1 = new AppUser();
        user1.setUsername("User1");

        AppUser user2 = new AppUser();
        user2.setUsername("User2");

        AppUser user3 = new AppUser();
        user3.setUsername("User3");

        Bill bill1 = new Bill();
        bill1.setId(1L);
        bill1.setMainPayer(user1);

        Bill bill2 = new Bill();
        bill2.setId(2L);
        bill2.setMainPayer(user2);

        BillItem item1 = new BillItem();
        item1.setId(1L);

        BillItem item2 = new BillItem();
        item2.setId(2L);

        BillParticipant participant1 = new BillParticipant();
        participant1.setAppUser(user2);
        participant1.setAmountOwed(BigDecimal.valueOf(100));
        participant1.setAmountPaid(BigDecimal.valueOf(50));
        participant1.setBillItem(item1);

        BillParticipant participant2 = new BillParticipant();
        participant2.setAppUser(user3);
        participant2.setAmountOwed(BigDecimal.valueOf(200));
        participant2.setAmountPaid(BigDecimal.ZERO);
        participant2.setBillItem(item2);

        when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));
        when(billRepository.findByAppGroup(group)).thenReturn(Arrays.asList(bill1, bill2));
        when(billItemRepository.findByBillId(1L)).thenReturn(Collections.singletonList(item1));
        when(billItemRepository.findByBillId(2L)).thenReturn(Collections.singletonList(item2));
        when(billParticipantRepository.findByBillItemId(1L)).thenReturn(Collections.singletonList(participant1));
        when(billParticipantRepository.findByBillItemId(2L)).thenReturn(Collections.singletonList(participant2));

        List<DebtDto> debts = billService.calculateGroupDebts(groupName);

        assertNotNull(debts);
        assertEquals(2, debts.size());

        DebtDto debt1 = debts.get(0);
        assertEquals("User2", debt1.getDebtorName());
        assertEquals("User1", debt1.getCreditorName());
        assertEquals(BigDecimal.valueOf(50), debt1.getAmount());

        DebtDto debt2 = debts.get(1);
        assertEquals("User3", debt2.getDebtorName());
        assertEquals("User2", debt2.getCreditorName());
        assertEquals(BigDecimal.valueOf(200), debt2.getAmount());

        verify(groupRepository).findByName(groupName);
        verify(billRepository).findByAppGroup(group);
        verify(billItemRepository).findByBillId(1L);
        verify(billItemRepository).findByBillId(2L);
        verify(billParticipantRepository).findByBillItemId(1L);
        verify(billParticipantRepository).findByBillItemId(2L);
    }

    @Test
    void addSharedBillItem_ShouldAddItemAndDistributeCosts_WhenDataIsValid() {
        SharedBillItemRequestDto requestDto = new SharedBillItemRequestDto(
                1L, "Shared Item", BigDecimal.valueOf(300)
        );

        AppGroup group = new AppGroup();
        group.setId(1L);

        Bill bill = new Bill();
        bill.setId(1L);
        bill.setAppGroup(group);

        AppUser user1 = new AppUser();
        user1.setId(1L);
        user1.setUsername("user1");

        AppUser user2 = new AppUser();
        user2.setId(2L);
        user2.setUsername("user2");

        BillItem sharedItem = new BillItem();
        sharedItem.setId(1L);
        sharedItem.setItemName("Shared Item");
        sharedItem.setItemPrice(BigDecimal.valueOf(300));

        GroupMember member1 = new GroupMember();
        member1.setAppUser(user1);

        GroupMember member2 = new GroupMember();
        member2.setAppUser(user2);

        when(billRepository.findById(requestDto.getBillId())).thenReturn(Optional.of(bill));
        when(groupMemberRepository.findByAppGroup(group)).thenReturn(Arrays.asList(member1, member2));
        when(billItemRepository.save(any(BillItem.class))).thenReturn(sharedItem);
        billService.addSharedBillItem(1L, "Shared Item", BigDecimal.valueOf(300));


        verify(billRepository).findById(requestDto.getBillId());
        verify(groupMemberRepository).findByAppGroup(group);
        verify(billItemRepository).save(any(BillItem.class));

        verify(billParticipantRepository, times(2)).save(any(BillParticipant.class));
    }

    @Test
    void shouldThrowResourceNotFoundException_whenBillNotFound() {
        Long billId = 1L;
        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            billService.addSharedBillItem(billId, "Item", new BigDecimal("100.00"));
        });
        assertEquals("Bill not found", exception.getMessage());
    }

    @Test
    void shouldThrowValidationException_whenGroupNotAssociatedWithBill() {
        AppGroup appGroup = new AppGroup();
        appGroup.setName("Test Group");
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setAppGroup(appGroup);

        bill.setAppGroup(null);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            billService.addSharedBillItem(1L, "Item", new BigDecimal("100.00"));
        });
        assertEquals("Group not associated with this bill", exception.getMessage());
    }

    @Test
    void shouldThrowValidationException_whenGroupHasNoMembers() {
        AppGroup appGroup = new AppGroup();
        appGroup.setName("Test Group");
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setAppGroup(appGroup);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(groupMemberRepository.findByAppGroup(appGroup)).thenReturn(Collections.emptyList());

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            billService.addSharedBillItem(1L, "Item", new BigDecimal("100.00"));
        });
        assertEquals("Group has no members", exception.getMessage());
    }
}
