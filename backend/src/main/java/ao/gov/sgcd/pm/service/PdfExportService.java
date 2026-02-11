package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.entity.Sprint;
import ao.gov.sgcd.pm.entity.SprintReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final ObjectMapper objectMapper;

    private static final BaseColor ANGOLA_RED = new BaseColor(204, 9, 47);
    private static final BaseColor ANGOLA_GOLD = new BaseColor(244, 180, 0);
    private static final BaseColor DARK_GRAY = new BaseColor(51, 51, 51);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generatePdf(SprintReport report) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            Sprint sprint = report.getSprint();

            // Header
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, ANGOLA_RED);
            Paragraph header = new Paragraph("SGCD — Relatório de Sprint", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, DARK_GRAY);
            Paragraph subHeader = new Paragraph(
                    "República de Angola — Embaixada em Portugal", subHeaderFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(20);
            document.add(subHeader);

            // Sprint Info
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, DARK_GRAY);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, DARK_GRAY);

            document.add(new Paragraph(
                    String.format("Sprint %d: %s", sprint.getSprintNumber(), sprint.getName()), boldFont));
            document.add(new Paragraph(
                    String.format("Período: %s — %s", sprint.getStartDate(), sprint.getEndDate()), normalFont));
            document.add(new Paragraph(
                    String.format("Tipo: %s | Gerado: %s",
                            report.getReportType(), report.getGeneratedAt().format(DATE_FMT)), normalFont));

            document.add(Chunk.NEWLINE);

            // Summary (PT)
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, ANGOLA_RED);
            document.add(new Paragraph("Resumo", sectionFont));
            document.add(new Paragraph(report.getSummaryPt(), normalFont));
            document.add(Chunk.NEWLINE);

            // Metrics Table
            document.add(new Paragraph("Métricas", sectionFont));
            addMetricsTable(document, report.getMetricsJson());
            document.add(Chunk.NEWLINE);

            // English Summary
            Font enFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
            document.add(new Paragraph("Summary (EN)", enFont));
            document.add(new Paragraph(report.getSummaryEn(), enFont));

            // Footer
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
            Paragraph footer = new Paragraph(
                    String.format("SGCD-PM — Gerado automaticamente em %s",
                            report.getGeneratedAt().format(DATE_FMT)), footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    private void addMetricsTable(Document document, String metricsJson) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setSpacingBefore(8);
        table.setSpacingAfter(8);

        Font headerCellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, DARK_GRAY);

        // Header row
        PdfPCell labelHeader = new PdfPCell(new Phrase("Métrica", headerCellFont));
        labelHeader.setBackgroundColor(ANGOLA_RED);
        labelHeader.setPadding(6);
        table.addCell(labelHeader);

        PdfPCell valueHeader = new PdfPCell(new Phrase("Valor", headerCellFont));
        valueHeader.setBackgroundColor(ANGOLA_RED);
        valueHeader.setPadding(6);
        table.addCell(valueHeader);

        try {
            JsonNode metrics = objectMapper.readTree(metricsJson);
            addRow(table, cellFont, "Sessões Totais", getText(metrics, "totalSessions"));
            addRow(table, cellFont, "Sessões Concluídas", getText(metrics, "completedSessions"));
            addRow(table, cellFont, "Tarefas Bloqueadas", getText(metrics, "blockedTasks"));
            addRow(table, cellFont, "Tarefas Ignoradas", getText(metrics, "skippedTasks"));
            addRow(table, cellFont, "Horas Planeadas", getText(metrics, "totalHours"));
            addRow(table, cellFont, "Horas Reais", getText(metrics, "actualHours"));
            addRow(table, cellFont, "Progresso (%)", String.format("%.1f%%",
                    metrics.has("progressPercent") ? metrics.get("progressPercent").asDouble() : 0));
        } catch (Exception e) {
            addRow(table, cellFont, "Erro", "Não foi possível processar métricas");
        }

        document.add(table);
    }

    private void addRow(PdfPTable table, Font font, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(new BaseColor(245, 245, 245));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : "—";
    }
}
