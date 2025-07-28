package com.project.aptflow.controller;

import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.dto.apiresponse.ResponseDTO;
import com.project.aptflow.dto.billing.GenerateBillDTO;
import com.project.aptflow.service.GenerateBillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/generated-bills")
public class GenerateBillController {
    private final GenerateBillService generateBillService;

    public GenerateBillController(GenerateBillService generateBillService) {
        this.generateBillService = generateBillService;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/bills")
    public ResponseEntity<List<GenerateBillDTO>> getAllBills(){
        List<GenerateBillDTO> bills=generateBillService.getAllBills();
        return ResponseEntity.status(HttpStatus.OK).body(bills);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/bill/{id}")
    public ResponseEntity<GenerateBillDTO> getBillById(@PathVariable Long id){
        GenerateBillDTO bill=generateBillService.getBillById(id);
        return ResponseEntity.status(HttpStatus.OK).body(bill);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/{adhaarNumber}")
    public ResponseEntity<List<GenerateBillDTO>> getBillsByAdhaarNumber(@PathVariable String adhaarNumber){
        List<GenerateBillDTO> bills=generateBillService.getBillsByAdhaarNumber(adhaarNumber);
        return ResponseEntity.status(HttpStatus.OK).body(bills);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<GenerateBillDTO>> updateBill(@PathVariable Long id, @RequestBody GenerateBillDTO updateBillDTO){
        GenerateBillDTO updatedBill = generateBillService.updateBill(id, updateBillDTO);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Data updated successfully...", HttpStatus.OK,updatedBill));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(@PathVariable Long id){
        generateBillService.deleteBill(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/generate")
    public ResponseEntity<MessageResponseDTO> generateBill(@RequestBody GenerateBillDTO generateBillDTO){
        generateBillService.generateBill(generateBillDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponseDTO("Bill generated successfully",HttpStatus.CREATED));
    }
}
