package com.project.aptflow.dto.billing;

import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.booking.BookingDTO;
import com.project.aptflow.enums.DeliveryStatus;
import com.project.aptflow.enums.GenerateBillStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GenerateBillDTO {
    private Long id;
    private BillDTO billDTO;
    private RoomSummaryDTO roomSummaryDTO;
    private UserSummaryDTO userSummaryDTO;
    private BookingDTO bookingDTO;
    private YearMonth month;
    private BigDecimal total;
    private DeliveryStatus deliveryStatus;
    private GenerateBillStatus generateBillStatus;

}
