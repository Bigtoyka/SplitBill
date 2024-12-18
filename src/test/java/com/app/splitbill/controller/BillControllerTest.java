package com.app.splitbill.controller;

import com.app.splitbill.dto.BillParticipantRequestDto;
import com.app.splitbill.dto.BillRequestDto;
import com.app.splitbill.dto.DebtDto;
import com.app.splitbill.dto.SharedBillItemRequestDto;
import com.app.splitbill.model.AppGroup;
import com.app.splitbill.model.AppUser;
import com.app.splitbill.model.Bill;
import com.app.splitbill.model.BillItem;
import com.app.splitbill.service.BillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(BillController.class)
public class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillService billService;

    @Test
    public void CreateBill_SuccessfulyCreate() throws Exception {
        BillRequestDto billRequestDto = new BillRequestDto();

        mockMvc.perform(post("/bills/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(billRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bill added successfully"));
    }
    @Test
    public void GetBillById_SuccessfulyGet() throws Exception {
        Long billId = 1L;
        LocalDate localDate = LocalDate.now();
        Bill bill = new Bill(billId,new AppGroup(), BigDecimal.valueOf(1),"test", localDate,new AppUser(),new AppUser()); // Заполните объект bill необходимыми данными
        when(billService.getBillById(billId)).thenReturn(bill);

        mockMvc.perform(get("/bills/{id}", billId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(billId));
    }
    @Test
    public void AddBillItem_SuccessfulyAdd() throws Exception {
        Long billId = 1L;
        BillItem billItem = new BillItem();

        mockMvc.perform(post("/bills/{billId}/items", billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(billItem)))
                .andExpect(status().isOk())
                .andExpect(content().string("Item added successfully in bill " + billId));
    }
    @Test
    public void AddBillParticipant_SuccessfulyAdd() throws Exception {
        BillParticipantRequestDto participantDto = new BillParticipantRequestDto();

        mockMvc.perform(post("/bills/items/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(participantDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(participantDto.getItemName() + " add in bill " + participantDto.getBillId() + " in group " + participantDto.getGroupName()));
    }
    @Test
    public void CalculateDebtsByConsumption_SuccessfulyCalculate() throws Exception {
        Long billId = 1L;
        List<DebtDto> debts = Arrays.asList(new DebtDto());

        when(billService.calculateDebtBasedOnConsumption(billId)).thenReturn(debts);

        mockMvc.perform(get("/bills/{billId}/debts", billId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(debts.size())));
    }
    @Test
    public void AddSharedBillItem_SuccessfulyAdd() throws Exception {
        SharedBillItemRequestDto requestDto = new SharedBillItemRequestDto();

        mockMvc.perform(post("/bills/shared-item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Shared bill item added successfully"));
    }
    @Test
    public void GetGroupDebts_SuccessfulyGet() throws Exception {
        String groupName = "TestGroup";
        List<DebtDto> debts = Arrays.asList(new DebtDto());

        when(billService.calculateGroupDebts(groupName)).thenReturn(debts);

        mockMvc.perform(get("/bills/group/{groupName}/debts", groupName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(debts.size())));
    }

}
