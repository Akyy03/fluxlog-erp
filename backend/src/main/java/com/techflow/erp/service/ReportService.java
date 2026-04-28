package com.techflow.erp.service;

import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.techflow.erp.dto.response.EmployeeResponse;
import com.techflow.erp.entity.Task;
import com.techflow.erp.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TaskRepository taskRepository;
    private final EmployeeService employeeService;

    // --- GENERARE RAPORT MANAGER (Echipa proprie) ---
    public byte[] generateExcelReport(String email) throws IOException {
        Long deptId = employeeService.findByEmail(email).getDepartment().getId();
        List<Task> tasks = taskRepository.findByAssignedTo_Employee_Department_Id(deptId);

        try (Workbook workbook = new XSSFWorkbook()) {
            createManagerSummarySheet(workbook, tasks);

            Sheet sheet = workbook.createSheet("Tasks Report");
            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] columns = {"ID", "Titlu", "Status", "Deadline", "Proiect", "Asignat Către"};
            createHeaderRow(sheet, columns, headerStyle);

            int rowNum = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(task.getId());
                row.createCell(1).setCellValue(task.getTitle());
                row.createCell(2).setCellValue(task.getStatus() != null ? task.getStatus().toString() : "");
                row.createCell(3).setCellValue(task.getDeadline() != null ? task.getDeadline().toString() : "");
                row.createCell(4).setCellValue(task.getProject() != null ? task.getProject().getName() : "Fără Proiect");
                row.createCell(5).setCellValue(task.getAssignedTo() != null ?
                        task.getAssignedTo().getEmployee().getFirstName() + " " + task.getAssignedTo().getEmployee().getLastName() : "Neasignat");
            }

            applyAutoFilter(sheet, rowNum - 1, columns.length - 1);
            applyDeadlineFormatting(sheet, rowNum - 1);
            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // --- GENERARE RAPORT ADMIN (Master) ---
    public byte[] generateAdminMasterReport() throws IOException {
        List<Task> allTasks = taskRepository.findAll();
        List<EmployeeResponse> allEmployees = employeeService.getAllEmployeesIncludeDeleted();

        try (Workbook workbook = new XSSFWorkbook()) {
            createAdminSummarySheet(workbook, allTasks, allEmployees);
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Tab: Toate Taskurile
            Sheet taskSheet = workbook.createSheet("Toate Taskurile");
            String[] taskCols = {"ID", "Titlu", "Status", "Deadline", "Proiect", "Asignat Către"};
            createHeaderRow(taskSheet, taskCols, headerStyle);

            int tRow = 1;
            for (Task task : allTasks) {
                Row row = taskSheet.createRow(tRow++);
                row.createCell(0).setCellValue(task.getId());
                row.createCell(1).setCellValue(task.getTitle());
                row.createCell(2).setCellValue(task.getStatus() != null ? task.getStatus().name() : "");
                row.createCell(3).setCellValue(task.getDeadline() != null ? task.getDeadline().toString() : "");
                row.createCell(4).setCellValue(task.getProject() != null ? task.getProject().getName() : "Fără Proiect");
                row.createCell(5).setCellValue(task.getAssignedTo() != null ?
                        task.getAssignedTo().getEmployee().getFirstName() + " " + task.getAssignedTo().getEmployee().getLastName() : "Neasignat");
            }
            applyAutoFilter(taskSheet, tRow - 1, taskCols.length - 1);
            applyDeadlineFormatting(taskSheet, tRow - 1);
            for (int i = 0; i < taskCols.length; i++) taskSheet.autoSizeColumn(i);

            // Tab: HR
            Sheet hrSheet = workbook.createSheet("Angajați & Buget");
            String[] hrCols = {"Nume", "Prenume", "Salariu Bază", "Echipament", "Cost Total"};
            createHeaderRow(hrSheet, hrCols, headerStyle);

            int rowNum = 1;
            for (EmployeeResponse emp : allEmployees) {
                Row row = hrSheet.createRow(rowNum++);
                double salary = (emp.getSalary() != null) ? emp.getSalary().doubleValue() : 0.0;
                row.createCell(0).setCellValue(emp.getLastName());
                row.createCell(1).setCellValue(emp.getFirstName());
                row.createCell(2).setCellValue(salary);
                row.createCell(3).setCellValue(1000.0);
                row.createCell(4).setCellValue(salary + 1000.0);
            }
            applyAutoFilter(hrSheet, rowNum - 1, hrCols.length - 1);
            for (int i = 0; i < hrCols.length; i++) hrSheet.autoSizeColumn(i);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // Metoda pentru Manager (Team Report)
    public byte[] generateManagerPdf(String email) {
        Long deptId = employeeService.findByEmail(email).getDepartment().getId();
        List<Task> tasks = taskRepository.findByAssignedTo_Employee_Department_Id(deptId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Raport Activitate Echipa", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            String[] headers = {"Titlu", "Status", "Deadline", "Proiect", "Asignat"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                cell.setBackgroundColor(new Color(211, 211, 211));
                table.addCell(cell);
            }

            for (Task task : tasks) {
                table.addCell(task.getTitle() != null ? task.getTitle() : "");
                table.addCell(task.getStatus() != null ? task.getStatus().toString() : "-");
                table.addCell(task.getDeadline() != null ? task.getDeadline().toString() : "-");
                table.addCell(task.getProject() != null ? task.getProject().getName() : "N/A");
                table.addCell(task.getAssignedTo() != null ?
                        task.getAssignedTo().getEmployee().getFirstName() + " " + task.getAssignedTo().getEmployee().getLastName() : "Neasignat");
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Eroare la generarea PDF-ului", e);
        }
        return out.toByteArray();
    }

    // Metoda pentru Admin (Master Report)
    public byte[] generateAdminPdf() {
        List<Task> allTasks = taskRepository.findAll();
        List<EmployeeResponse> allEmployees = employeeService.getAllEmployeesIncludeDeleted();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // --- Titlu ---
            document.add(new Paragraph("Raport Master ERP - Global", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20)));
            document.add(new Paragraph(" "));

            // --- Tabel Task-uri ---
            document.add(new Paragraph("Toate Task-urile din Sistem", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            PdfPTable taskTable = new PdfPTable(5);
            taskTable.setWidthPercentage(100);
            String[] taskHeaders = {"Titlu", "Status", "Deadline", "Proiect", "Asignat"};

            for (String h : taskHeaders) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                cell.setBackgroundColor(new Color(211, 211, 211));
                taskTable.addCell(cell);
            }

            for (Task task : allTasks) {
                taskTable.addCell(task.getTitle() != null ? task.getTitle() : "");
                taskTable.addCell(task.getStatus() != null ? task.getStatus().toString() : "-");
                taskTable.addCell(task.getDeadline() != null ? task.getDeadline().toString() : "-");
                taskTable.addCell(task.getProject() != null ? task.getProject().getName() : "N/A");
                taskTable.addCell(task.getAssignedTo() != null ?
                        task.getAssignedTo().getEmployee().getFirstName() + " " + task.getAssignedTo().getEmployee().getLastName() : "Neasignat");
            }
            document.add(taskTable);

            // --- Tabel HR & Buget ---
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Situație HR & Buget", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            PdfPTable hrTable = new PdfPTable(5);
            hrTable.setWidthPercentage(100);
            String[] hrHeaders = {"Nume", "Prenume", "Salariu", "Echipament", "Total"};

            for (String h : hrHeaders) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                cell.setBackgroundColor(new Color(211, 211, 211));
                hrTable.addCell(cell);
            }

            for (EmployeeResponse emp : allEmployees) {
                double salary = (emp.getSalary() != null) ? emp.getSalary().doubleValue() : 0.0;
                double equip = 1000.0;

                hrTable.addCell(emp.getLastName() != null ? emp.getLastName() : "");
                hrTable.addCell(emp.getFirstName() != null ? emp.getFirstName() : "");
                hrTable.addCell(String.format("%.2f", salary));
                hrTable.addCell(String.format("%.2f", equip));
                hrTable.addCell(String.format("%.2f", salary + equip));
            }
            document.add(hrTable);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Eroare la generarea PDF-ului master", e);
        }
        return out.toByteArray();
    }

    // --- HELPER METHODS (Logica PRO) ---

    private void createManagerSummarySheet(Workbook workbook, List<Task> tasks) {
        Sheet sheet = workbook.createSheet("Sumar");
        long completed = tasks.stream().filter(t -> t.getStatus() != null && "DONE".equals(t.getStatus().toString())).count();
        addSummaryRow(sheet, 0, "Total Task-uri", tasks.size());
        addSummaryRow(sheet, 1, "Finalizate", (int) completed);
        sheet.autoSizeColumn(0);
    }

    private void createAdminSummarySheet(Workbook workbook, List<Task> tasks, List<EmployeeResponse> employees) {
        Sheet sheet = workbook.createSheet("Sumar Global");
        double totalSalary = employees.stream().mapToDouble(e -> e.getSalary() != null ? e.getSalary().doubleValue() : 0.0).sum();
        addSummaryRow(sheet, 0, "Total Task-uri", tasks.size());
        addSummaryRow(sheet, 1, "Total Angajați", employees.size());
        addSummaryRow(sheet, 2, "Cost Salarii", (int) totalSalary);
        sheet.autoSizeColumn(0);
    }

    private void addSummaryRow(Sheet sheet, int rowIdx, String label, int value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void applyDeadlineFormatting(Sheet sheet, int lastRow) {
        if (lastRow < 1) return;
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule rule = sheetCF.createConditionalFormattingRule("DATEVALUE(D2) < TODAY()");
        PatternFormatting fill = rule.createPatternFormatting();
        fill.setFillForegroundColor(IndexedColors.RED.getIndex());
        fill.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
        CellRangeAddress[] regions = { new CellRangeAddress(1, lastRow, 3, 3) };
        sheetCF.addConditionalFormatting(regions, rule);
    }

    private void applyAutoFilter(Sheet sheet, int lastRow, int lastCol) {
        sheet.setAutoFilter(new CellRangeAddress(0, lastRow, 0, lastCol));
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
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