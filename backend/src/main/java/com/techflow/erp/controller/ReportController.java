package com.techflow.erp.controller;

import com.techflow.erp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // Endpoint pentru Manageri
    @GetMapping("/export/my-team")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<byte[]> exportMyTeam(Principal principal) throws IOException {

        byte[] excelContent = reportService.generateExcelReport(principal.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Echipa_Mea_Report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }

    // Endpoint pentru Admini
    @GetMapping("/export/master")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportMasterReport() throws IOException {
        byte[] excelContent = reportService.generateAdminMasterReport();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Master_Report_ERP.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }

    @GetMapping("/pdf/manager")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<byte[]> exportManagerPdf(Principal principal) {
        // principal.getName() returnează email-ul userului logat
        byte[] pdfContent = reportService.generateManagerPdf(principal.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Raport_Echipa.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @GetMapping("/pdf/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<byte[]> exportAdminPdf() {
        // Adminul vede tot, deci nu avem nevoie de email
        byte[] pdfContent = reportService.generateAdminPdf();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Raport_Master_ERP.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }
}