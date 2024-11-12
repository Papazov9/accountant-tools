package com.wildrep.accountantapp.util;

import com.wildrep.accountantapp.model.dto.PaymentSessionRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class PDFWriterUtil {

    public static File generateProFormaInvoicePDF(PaymentSessionRequest paymentSessionRequest, String iban) {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        String uuid = UUID.randomUUID().toString();

        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            contentStream.newLineAtOffset(220, 750);
            contentStream.showText("PROFORMA INVOICE");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(40, 700);

            contentStream.showText("From: Unparalleled LTD");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Kichuka 032");

            contentStream.newLineAtOffset(400, 15);
            contentStream.showText("Bill To: " + paymentSessionRequest.companyName());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText(paymentSessionRequest.addressLine1());
            contentStream.endText();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            contentStream.beginText();
            contentStream.newLineAtOffset(40, 650);
            contentStream.showText("Invoice #: " + uuid);
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Date: " + dateFormat.format(new Date()));
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Due Date: " + dateFormat.format(new Date(System.currentTimeMillis() + 2628000000L)));
            contentStream.endText();

            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(40, 600);
            contentStream.showText("DESCRIPTION");
            contentStream.newLineAtOffset(250, 0);
            contentStream.showText("UNIT PRICE");
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText("QTY");
            contentStream.newLineAtOffset(50, 0);
            contentStream.showText("AMOUNT");
            contentStream.endText();

            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
            int yPosition = 580;

            contentStream.beginText();
            contentStream.newLineAtOffset(40, yPosition);
            contentStream.showText(paymentSessionRequest.subscriptionType());
            contentStream.newLineAtOffset(250, 0);
            contentStream.showText(String.format("%.2f", Double.parseDouble(String.valueOf(paymentSessionRequest.price())) / 100.0));
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText(String.valueOf(1L));
            contentStream.newLineAtOffset(50, 0);
            contentStream.showText(String.format("%.2f", Double.parseDouble(String.valueOf(paymentSessionRequest.price())) / 100.0));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(40, yPosition - 30);
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
            contentStream.showText("Total: " + String.format("%.2f", Double.parseDouble(String.valueOf(paymentSessionRequest.price())) / 100.0));
            contentStream.endText();


            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(40, yPosition - 80);
            contentStream.showText("Bank Transfer Instructions:");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("IBAN: " + iban);
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Please transfer the total amount to the above IBAN.");
            contentStream.endText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        File file = new File("ProFormaInvoice_" + uuid + ".pdf");
        try {
            document.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                document.close();
            } catch (IOException e) {
                log.error("FAiled to close pdf document!");
            }
        }

        return file;
    }
}
