package com.project.aptflow.service.impl;

import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.entity.BillEntity;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.BillMapper;
import com.project.aptflow.repository.BillRepository;
import com.project.aptflow.service.BillService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BillServiceImpl implements BillService {
    private final BillRepository billRepository;
    private final BillMapper billMapper;

    public BillServiceImpl(BillRepository billRepository, BillMapper billMapper) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
    }


    @Override
    public List<BillDTO> getAllBills() {
        List<BillEntity> billEntities = billRepository.findAll();
        List<BillDTO> billDTOs = new ArrayList<>();
        for (BillEntity bill: billEntities){
            billDTOs.add(billMapper.entityToDTO(bill));
        }
        return billDTOs;
    }

    @Override
    public BillDTO getBillById(Long id) {
        BillEntity bill =billRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Bill not found..."));
        return billMapper.entityToDTO(bill);
    }

    @Override
    public BillDTO updateBill(Long id, BillDTO updateBill) {
        BillEntity bill = billRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Bill not found..."));
        billMapper.updateBillEntity(bill,updateBill);
        return billMapper.entityToDTO(billRepository.save(bill));
    }
}
