package com.fluxlog.erp.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.fluxlog.erp.dto.response.EmployeeResponse;
import com.fluxlog.erp.entity.Task;
import com.fluxlog.erp.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TaskRepository taskRepository;
    private final EmployeeService employeeService;

    public byte[] generateExcelReport(String email) throws IOException {
        Long deptId = employeeService.findByEmail(email).getDepartment().getId();

        List<Task> tasks = taskRepository.findByAssignedTo_Employee_Department_Id(deptId)
                .stream()
                .filter(t -> t.getAssignedTo() != null && !t.getAssignedTo().getEmployee().isDeleted())
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            createManagerSummarySheet(workbook, tasks);

            Sheet sheet = workbook.createSheet("Registru Comenzi Marfa");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"Cod Lot", "Denumire Marfa", "Status Livrare", "Termen Receptie", "Contract/Comanda", "Gestionar Responsabil"};
            createHeaderRow(sheet, columns, headerStyle);

            int rowNum = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue("LOT-" + task.getId());
                row.createCell(1).setCellValue(task.getTitle());
                row.createCell(2).setCellValue(task.getStatus() != null ? task.getStatus().toString() : "");
                row.createCell(3).setCellValue(task.getDeadline() != null ? task.getDeadline().toString() : "");
                row.createCell(4).setCellValue(task.getProject() != null ? task.getProject().getName() : "Stoc General");
                row.createCell(5).setCellValue(task.getAssignedTo().getEmployee().getFirstName() + " " + task.getAssignedTo().getEmployee().getLastName());
            }

            applyAutoFilter(sheet, rowNum - 1, columns.length - 1);
            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] generateAdminMasterReport() throws IOException {
        List<EmployeeResponse> allEmployees = employeeService.getAllEmployeesIncludeDeleted()
                .stream()
                .filter(e -> !e.isDeleted())
                .collect(Collectors.toList());

        List<Task> allTasks = taskRepository.findAll()
                .stream()
                .filter(t -> t.getAssignedTo() != null && !t.getAssignedTo().getEmployee().isDeleted())
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            createAdminSummarySheet(workbook, allTasks, allEmployees);
            CellStyle headerStyle = createHeaderStyle(workbook);

            Sheet taskSheet = workbook.createSheet("Inventar Global Operatiuni");
            String[] taskCols = {"Referinta", "Descriere Bunuri", "Stadiu", "Data Scadenta", "Client/Contract", "Operator"};
            createHeaderRow(taskSheet, taskCols, headerStyle);

            int tRow = 1;
            for (Task task : allTasks) {
                Row row = taskSheet.createRow(tRow++);
                row.createCell(0).setCellValue("ID-FLUX-" + task.getId());
                row.createCell(1).setCellValue(task.getTitle());
                row.createCell(2).setCellValue(task.getStatus() != null ? task.getStatus().name() : "");
                row.createCell(3).setCellValue(task.getDeadline() != null ? task.getDeadline().toString() : "");
                row.createCell(4).setCellValue(task.getProject() != null ? task.getProject().getName() : "Nespecificat");
                row.createCell(5).setCellValue(task.getAssignedTo().getEmployee().getFirstName() + " " + task.getAssignedTo().getEmployee().getLastName());
            }
            for (int i = 0; i < taskCols.length; i++) taskSheet.autoSizeColumn(i);

            Sheet hrSheet = workbook.createSheet("State de Plata si Taxe");
            String[] hrCols = {"Nume Angajat", "Salariu Brut", "CAS (25%)", "CASS (10%)", "Impozit (10%)", "Salariu Net"};
            createHeaderRow(hrSheet, hrCols, headerStyle);

            int rowNum = 1;
            for (EmployeeResponse emp : allEmployees) {
                Row row = hrSheet.createRow(rowNum++);
                double brut = (emp.getSalary() != null) ? emp.getSalary().doubleValue() : 0.0;
                double cas = brut * 0.25;
                double cass = brut * 0.10;
                double impozit = (brut - cas - cass) * 0.10;
                double net = brut - cas - cass - impozit;

                row.createCell(0).setCellValue(emp.getLastName() + " " + emp.getFirstName());
                row.createCell(1).setCellValue(brut);
                row.createCell(2).setCellValue(cas);
                row.createCell(3).setCellValue(cass);
                row.createCell(4).setCellValue(impozit);
                row.createCell(5).setCellValue(net);
            }
            for (int i = 0; i < hrCols.length; i++) hrSheet.autoSizeColumn(i);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] generateManagerPdf(String email) {
        Long deptId = employeeService.findByEmail(email).getDepartment().getId();
        List<Task> tasks = taskRepository.findByAssignedTo_Employee_Department_Id(deptId)
                .stream()
                .filter(t -> t.getAssignedTo() != null && !t.getAssignedTo().getEmployee().isDeleted())
                .collect(Collectors.toList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("SC FLUXLOG LOGISTICS SRL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph("CUI: RO12345678 | Nr. Reg. Com: J40/1234/2026"));
            document.add(new Paragraph("Adresa: Str. Logisticii Nr. 10, Craiova"));
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));
            document.add(new Paragraph(" "));

            Paragraph title = new Paragraph("RAPORT OPERATIONAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("Data emiterii: " + LocalDate.now()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            String[] headers = {"Produs/Serviciu", "Stadiu", "Termen", "Contract", "Responsabil"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                cell.setBackgroundColor(new Color(230, 230, 230));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (Task task : tasks) {
                table.addCell(new Paragraph(task.getTitle(), FontFactory.getFont(FontFactory.HELVETICA, 9)));
                table.addCell(new Paragraph(task.getStatus() != null ? task.getStatus().toString() : "-", FontFactory.getFont(FontFactory.HELVETICA, 9)));
                table.addCell(new Paragraph(task.getDeadline() != null ? task.getDeadline().toString() : "-", FontFactory.getFont(FontFactory.HELVETICA, 9)));
                table.addCell(new Paragraph(task.getProject() != null ? task.getProject().getName() : "N/A", FontFactory.getFont(FontFactory.HELVETICA, 9)));
                table.addCell(new Paragraph(task.getAssignedTo().getEmployee().getLastName(), FontFactory.getFont(FontFactory.HELVETICA, 9)));
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Semnatura de primire: ____________________"));
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Eroare la generarea Avizului", e);
        }
        return out.toByteArray();
    }

    public byte[] generateAdminPdf() {
        List<EmployeeResponse> activeEmployees = employeeService.getAllEmployeesIncludeDeleted()
                .stream()
                .filter(e -> !e.isDeleted())
                .collect(Collectors.toList());

        List<Task> activeTasks = taskRepository.findAll()
                .stream()
                .filter(t -> t.getAssignedTo() != null && !t.getAssignedTo().getEmployee().isDeleted())
                .collect(Collectors.toList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("FURNIZOR: SC FLUXLOG LOGISTICS SRL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            document.add(new Paragraph("CLIENT: CENTRALIZATOR ADMIN ERP"));
            document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------"));

            Paragraph title = new Paragraph("RAPORT FISCAL SI SITUATIE GLOBALA GESTIUNE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("1. SITUATIE OPERATIONALA (MARFA/FLUXURI)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            document.add(new Paragraph(" "));

            PdfPTable taskTable = new PdfPTable(5);
            taskTable.setWidthPercentage(100);
            String[] taskHeaders = {"Referinta", "Status", "Scadenta", "Comanda", "Operator"};

            for (String h : taskHeaders) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                cell.setBackgroundColor(new Color(200, 200, 200));
                taskTable.addCell(cell);
            }

            for (Task task : activeTasks) {
                taskTable.addCell(new Paragraph(task.getTitle(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                taskTable.addCell(new Paragraph(task.getStatus() != null ? task.getStatus().toString() : "-", FontFactory.getFont(FontFactory.HELVETICA, 8)));
                taskTable.addCell(new Paragraph(task.getDeadline() != null ? task.getDeadline().toString() : "-", FontFactory.getFont(FontFactory.HELVETICA, 8)));
                taskTable.addCell(new Paragraph(task.getProject() != null ? task.getProject().getName() : "N/A", FontFactory.getFont(FontFactory.HELVETICA, 8)));
                taskTable.addCell(new Paragraph(task.getAssignedTo().getEmployee().getLastName(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
            }
            document.add(taskTable);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("2. STAT DE PLATA DETALIAT", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            document.add(new Paragraph(" "));

            PdfPTable hrTable = new PdfPTable(6);
            hrTable.setWidthPercentage(100);
            String[] hrHeaders = {"Nume", "BRUT", "CAS(25%)", "CASS(10%)", "Impozit", "NET FINAL"};

            for (String h : hrHeaders) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
                cell.setBackgroundColor(new Color(200, 200, 200));
                hrTable.addCell(cell);
            }

            double totalNetGlobal = 0;
            for (EmployeeResponse emp : activeEmployees) {
                double brut = (emp.getSalary() != null) ? emp.getSalary().doubleValue() : 0.0;
                double cas = brut * 0.25;
                double cass = brut * 0.10;
                double impozit = (brut - cas - cass) * 0.10;
                double net = brut - cas - cass - impozit;
                totalNetGlobal += net;

                hrTable.addCell(new Paragraph(emp.getLastName(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                hrTable.addCell(new Paragraph(String.format("%.2f", brut), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                hrTable.addCell(new Paragraph(String.format("%.2f", cas), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                hrTable.addCell(new Paragraph(String.format("%.2f", cass), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                hrTable.addCell(new Paragraph(String.format("%.2f", impozit), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                hrTable.addCell(new Paragraph(String.format("%.2f", net), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
            }
            document.add(hrTable);

            document.add(new Paragraph(" "));
            Paragraph totalFinal = new Paragraph("TOTAL NET DE PLATA: " + String.format("%.2f RON", totalNetGlobal), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            totalFinal.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalFinal);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Eroare la generarea Raportului Admin", e);
        }
        return out.toByteArray();
    }

    private void createManagerSummarySheet(Workbook workbook, List<Task> tasks) {
        Sheet sheet = workbook.createSheet("Indicatori Performanta");
        long completed = tasks.stream().filter(t -> t.getStatus() != null && "DONE".equals(t.getStatus().toString())).count();
        addSummaryRow(sheet, 0, "Total Sarcini Gestionate", (int) tasks.size());
        addSummaryRow(sheet, 1, "Sarcini Finalizate", (int) completed);
        addSummaryRow(sheet, 2, "Rata de Eficienta (%)", tasks.size() > 0 ? (int) (completed * 100 / tasks.size()) : 0);
        sheet.autoSizeColumn(0);
    }

    private void createAdminSummarySheet(Workbook workbook, List<Task> tasks, List<EmployeeResponse> employees) {
        Sheet sheet = workbook.createSheet("Sumar Contabil");
        double totalSalary = employees.stream().mapToDouble(e -> e.getSalary() != null ? e.getSalary().doubleValue() : 0.0).sum();
        addSummaryRow(sheet, 0, "Volum Total Operatiuni", (int) tasks.size());
        addSummaryRow(sheet, 1, "Efectiv Personal Activ", (int) employees.size());
        addSummaryRow(sheet, 2, "Fond Salarii Brut (RON)", (int) totalSalary);
        sheet.autoSizeColumn(0);
    }

    private void addSummaryRow(Sheet sheet, int rowIdx, String label, int value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void applyAutoFilter(Sheet sheet, int lastRow, int lastCol) {
        if (lastRow > 0) sheet.setAutoFilter(new CellRangeAddress(0, lastRow, 0, lastCol));
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.MEDIUM);
        return style;
    }

    private void createHeaderRow(Sheet sheet, String[] columns, CellStyle style) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(style);
        }
    }
}