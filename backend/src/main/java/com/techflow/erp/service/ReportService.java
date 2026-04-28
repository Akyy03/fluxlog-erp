package com.techflow.erp.service;

import com.techflow.erp.dto.response.EmployeeResponse;
import com.techflow.erp.entity.Task;
import com.techflow.erp.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TaskRepository taskRepository;
    private final EmployeeService employeeService;

    public byte[] generateExcelReport(String email) throws IOException {
        Long deptId = employeeService.findByEmail(email).getDepartment().getId();
        List<Task> tasks = taskRepository.findByAssignedTo_Employee_Department_Id(deptId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tasks Report");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"ID", "Titlu", "Status", "Deadline", "Proiect", "Asignat Către"};
            createHeaderRow(sheet, columns, headerStyle);

            // Populăm datele
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

            // Auto-size coloane
            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] generateAdminMasterReport() throws IOException {
        List<Task> allTasks = taskRepository.findAll();
        List<EmployeeResponse> allEmployees = employeeService.getAllEmployeesIncludeDeleted();

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);

            // --- TAB 1: Toate Taskurile ---
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
            for (int i = 0; i < taskCols.length; i++) taskSheet.autoSizeColumn(i);

            // --- TAB 2: Angajați & Buget ---
            Sheet hrSheet = workbook.createSheet("Angajați & Buget");
            String[] hrCols = {"Nume", "Prenume", "Salariu Bază", "Echipament", "Cost Total"};
            createHeaderRow(hrSheet, hrCols, headerStyle);

            int rowNum = 1;
            double equipmentCost = 1000.0;

            for (EmployeeResponse emp : allEmployees) {
                Row row = hrSheet.createRow(rowNum++);
                double salaryValue = (emp.getSalary() != null) ? emp.getSalary().doubleValue() : 0.0;
                row.createCell(0).setCellValue(emp.getLastName());
                row.createCell(1).setCellValue(emp.getFirstName());
                row.createCell(2).setCellValue(salaryValue);
                row.createCell(3).setCellValue(equipmentCost);
                row.createCell(4).setCellValue(salaryValue + equipmentCost);
            }
            for (int i = 0; i < hrCols.length; i++) hrSheet.autoSizeColumn(i);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // --- Metode Utilitare pentru formatare și DRY ---

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