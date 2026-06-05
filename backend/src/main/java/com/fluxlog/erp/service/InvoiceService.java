package com.fluxlog.erp.service;

import com.fluxlog.erp.dto.request.InvoiceGenerateRequest;
import com.fluxlog.erp.entity.Project;
import com.fluxlog.erp.repository.ProjectRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final ProjectRepository projectRepository;

    public byte[] generateInvoicePdf(InvoiceGenerateRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Proiectul/Comanda nu a fost gasita!"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell buyerCell = new PdfPCell();
            buyerCell.setBorder(Rectangle.NO_BORDER);
            buyerCell.addElement(new Paragraph("Comanda:", normalFont));
            buyerCell.addElement(new Paragraph(project.getName(), boldFont));
            buyerCell.addElement(new Paragraph("Adresa: Str. Transportatorilor 1", normalFont));
            buyerCell.addElement(new Paragraph("CUI: RO99887766", normalFont));
            headerTable.addCell(buyerCell);

            PdfPCell datesCell = new PdfPCell();
            datesCell.setBorder(Rectangle.NO_BORDER);
            datesCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            datesCell.addElement(new Paragraph("Data facturii: " + request.getInvoiceDate(), normalFont));
            datesCell.addElement(new Paragraph("Data scadenta: " + request.getDueDate(), normalFont));
            headerTable.addCell(datesCell);

            document.add(headerTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Paragraph title = new Paragraph("Factura # " + request.getInvoiceNumber(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1f, 2f, 2f});

            String[] headers = {"Denumire", "Cant.", "Pret unitar", "Total"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, boldFont));
                cell.setBorderWidthBottom(1f);
                cell.setBorderWidthTop(1f);
                cell.setBorderWidthLeft(0);
                cell.setBorderWidthRight(0);
                cell.setPaddingBottom(5f);
                table.addCell(cell);
            }

            double totalNet = 0.0;

            // Iterăm dinamic prin lista de servicii primită din frontend
            if (request.getLines() != null) {
                for (InvoiceGenerateRequest.InvoiceLineDto line : request.getLines()) {
                    if (line.getServiceName() != null && !line.getServiceName().isEmpty() &&
                            line.getQuantity() != null && line.getUnitPrice() != null) {

                        double lineTotal = line.getQuantity() * line.getUnitPrice();
                        totalNet += lineTotal;
                        addTableRow(table, line.getServiceName(), line.getQuantity(), line.getUnitPrice(), lineTotal, normalFont);
                    }
                }
            }

            document.add(table);
            document.add(new Paragraph(" "));

            double totalTVA = totalNet * 0.19;
            double totalGeneral = totalNet + totalTVA;

            Paragraph totalNetP = new Paragraph(String.format("Total: %.2f RON", totalNet), normalFont);
            totalNetP.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalNetP);

            Paragraph tvaP = new Paragraph(String.format("TVA(19.00%%): %.2f RON", totalTVA), normalFont);
            tvaP.setAlignment(Element.ALIGN_RIGHT);
            document.add(tvaP);

            Paragraph totalP = new Paragraph(String.format("Total de plata: %.2f RON", totalGeneral), boldFont);
            totalP.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalP);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Paragraph legal = new Paragraph("Factura valabila fara semnatura si stampila cf. art.V, alin (2) din Ordonanta nr.17/2015 si art. 319 alin (29) din Legea nr. 227/2015 privind Codul fiscal.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9));
            document.add(legal);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            PdfPTable footerTable = new PdfPTable(3);
            footerTable.setWidthPercentage(100);
            footerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell fCol1 = new PdfPCell();
            fCol1.setBorder(Rectangle.NO_BORDER);
            fCol1.addElement(new Paragraph("SC FLUXLOG LOGISTICS SRL", boldFont));
            fCol1.addElement(new Paragraph("Reg. com.: J40/1234/2026", normalFont));
            fCol1.addElement(new Paragraph("CIF: RO12345678", normalFont));
            footerTable.addCell(fCol1);

            PdfPCell fCol2 = new PdfPCell();
            fCol2.setBorder(Rectangle.NO_BORDER);
            fCol2.addElement(new Paragraph("Str. Logisticii Nr. 10, Craiova", normalFont));
            fCol2.addElement(new Paragraph("Telefon: 0700 000 000", normalFont));
            footerTable.addCell(fCol2);

            PdfPCell fCol3 = new PdfPCell();
            fCol3.setBorder(Rectangle.NO_BORDER);
            fCol3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph bankP = new Paragraph("Banca Transilvania", normalFont);
            bankP.setAlignment(Element.ALIGN_RIGHT);
            Paragraph ibanP = new Paragraph("RO00BTRL0000000000000000", normalFont);
            ibanP.setAlignment(Element.ALIGN_RIGHT);
            fCol3.addElement(bankP);
            fCol3.addElement(ibanP);
            footerTable.addCell(fCol3);

            document.add(footerTable);

            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Eroare la generarea Facturii PDF", e);
        }

        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String name, int qty, double price, double total, Font font) {
        PdfPCell cell1 = new PdfPCell(new Paragraph(name, font));
        cell1.setBorderWidthLeft(0); cell1.setBorderWidthRight(0); cell1.setBorderWidthTop(0); cell1.setBorderWidthBottom(0);

        PdfPCell cell2 = new PdfPCell(new Paragraph(String.valueOf(qty), font));
        cell2.setBorderWidthLeft(0); cell2.setBorderWidthRight(0); cell2.setBorderWidthTop(0); cell2.setBorderWidthBottom(0);

        PdfPCell cell3 = new PdfPCell(new Paragraph(String.format("%.2f", price), font));
        cell3.setBorderWidthLeft(0); cell3.setBorderWidthRight(0); cell3.setBorderWidthTop(0); cell3.setBorderWidthBottom(0);

        PdfPCell cell4 = new PdfPCell(new Paragraph(String.format("%.2f", total), font));
        cell4.setBorderWidthLeft(0); cell4.setBorderWidthRight(0); cell4.setBorderWidthTop(0); cell4.setBorderWidthBottom(0);

        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);
        table.addCell(cell4);
    }
}