package com.fluxlog.erp.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceGenerateRequest {

    private Long projectId;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String invoiceNumber;

    private List<InvoiceLineDto> lines;

    @Data
    public static class InvoiceLineDto {
        private String serviceName;
        private Integer quantity;
        private Double unitPrice;
    }
}