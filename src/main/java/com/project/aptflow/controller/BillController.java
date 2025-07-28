package com.project.aptflow.controller;

import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.service.BillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bills")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class BillController {
    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/details")
    public ResponseEntity<List<BillDTO>> getAllBills(){
        List<BillDTO> bills=billService.getAllBills();
        return ResponseEntity.status(HttpStatus.OK).body(bills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getBillById(@PathVariable Long id){
        BillDTO bill=billService.getBillById(id);
        return ResponseEntity.status(HttpStatus.OK).body(bill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<BillDTO>> updateBill(@PathVariable Long id, @RequestBody BillDTO updateBill){
        BillDTO updatedBill=billService.updateBill(id, updateBill);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Bill updated successfully",HttpStatus.OK,updatedBill));
    }
}
