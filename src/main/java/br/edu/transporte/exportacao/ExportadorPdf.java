package br.edu.transporte.exportacao;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ExportadorPdf implements Exportador {

    @Override
    public String formato() {
        return "pdf";
    }

    @Override
    public MediaType tipoMidia() {
        return MediaType.APPLICATION_PDF;
    }

    @Override
    public byte[] gerar(String titulo, List<String> cabecalho, List<List<String>> linhas) {
        try {
            var doc = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            var out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15)));
            var geradoEm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            doc.add(new Paragraph("Gerado em " + geradoEm + " - " + linhas.size() + " registro(s)",
                    FontFactory.getFont(FontFactory.HELVETICA, 9)));
            doc.add(new Paragraph(" "));

            Font fonteCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font fonteTexto = FontFactory.getFont(FontFactory.HELVETICA, 8);
            var tabela = new PdfPTable(cabecalho.size());
            tabela.setWidthPercentage(100);
            tabela.setHeaderRows(1);
            for (var nomeColuna : cabecalho) {
                var celula = new PdfPCell(new Phrase(nomeColuna, fonteCabecalho));
                celula.setBackgroundColor(new Color(33, 37, 41));
                celula.setHorizontalAlignment(Element.ALIGN_LEFT);
                celula.setPadding(4);
                tabela.addCell(celula);
            }
            for (var valores : linhas) {
                for (var valor : valores) {
                    var celula = new PdfPCell(new Phrase(valor == null ? "" : valor, fonteTexto));
                    celula.setPadding(3);
                    tabela.addCell(celula);
                }
            }
            doc.add(tabela);
            doc.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Falha ao gerar o PDF", e);
        }
    }
}
