package com.project.aptflow.service;

import com.project.aptflow.dto.BillDTO;

import java.util.List;

public interface BillService {
    List<BillDTO> getAllBills();

    BillDTO getBillById(Long id);

    BillDTO updateBill(Long id, BillDTO updateBill);
}
