package com.project.aptflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BillDTO {
    private Long id;
    private String roomNumber;
    private String month;
    private BigDecimal electricityBill;
    private BigDecimal unitPrice;
    private BigDecimal rentPerDay;
}
