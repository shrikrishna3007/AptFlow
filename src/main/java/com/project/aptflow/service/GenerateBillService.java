package com.project.aptflow.service;

import com.project.aptflow.dto.billing.GenerateBillDTO;

import java.math.BigDecimal;
import java.util.List;

public interface GenerateBillService {
    List<GenerateBillDTO> getAllBills();

    GenerateBillDTO getBillById(Long id);

    List<GenerateBillDTO> getBillsByAdhaarNumber(String adhaarNumber);

    GenerateBillDTO updateBill(Long id, GenerateBillDTO updateBillDTO);

    void deleteBill(Long id);

    void generateBill(GenerateBillDTO generateBillDTO);

    BigDecimal checkOutBill(GenerateBillDTO generateBillDTO);
}
