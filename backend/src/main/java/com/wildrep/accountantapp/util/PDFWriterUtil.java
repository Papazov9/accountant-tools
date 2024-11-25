package com.wildrep.accountantapp.util;

import com.wildrep.accountantapp.model.dto.PaymentSessionRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

@Slf4j
public class PDFWriterUtil {

    public static File generateProFormaInvoicePDF(PaymentSessionRequest paymentSessionRequest, String invoiceNumber) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        InputStream fontFile = PDFWriterUtil.class.getClassLoader().getResourceAsStream("arial/ARIAL.TTF");
        PDType0Font font = PDType0Font.load(document, fontFile);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
        contentStream.addRect(0, 750, page.getMediaBox().getWidth(), 50);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setFont(font, 18);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.newLineAtOffset(50, 765);
        contentStream.showText(String.format("Ф А К Т У Р А - %s", invoiceNumber));
        contentStream.endText();

        // Client information
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(50, 700);
        contentStream.setLeading(20f);

        contentStream.showText("Клиент: " + paymentSessionRequest.companyName());
        contentStream.newLine();
        contentStream.showText("БУЛСТАТ: " + (paymentSessionRequest.isVatRegistration() ? "BG" + paymentSessionRequest.bulstat() : paymentSessionRequest.bulstat()));
        contentStream.newLine();
        contentStream.newLine();
        contentStream.showText("Адрес: " + paymentSessionRequest.addressLine1() + ", " + paymentSessionRequest.addressLine2() + ", " + paymentSessionRequest.city() + ", " + paymentSessionRequest.postalCode() + ", " + paymentSessionRequest.country());
        contentStream.newLine();
        contentStream.newLine();
        contentStream.endText();

        // Таблица за услуги
        contentStream.setNonStrokingColor(Color.LIGHT_GRAY);
        contentStream.addRect(50, 600, page.getMediaBox().getWidth() - 100, 25);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.newLineAtOffset(55, 615);
        contentStream.showText("Наименование на услугата");
        contentStream.newLineAtOffset(250, 0);
        contentStream.showText("Цена (без ДДС)");
        contentStream.newLine();
        contentStream.endText();

        // Данни за услуги
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(55, 585);
        contentStream.showText(paymentSessionRequest.subscriptionType());
        contentStream.newLineAtOffset(250, 0);
        contentStream.showText(String.format("%.2f BGN", (paymentSessionRequest.price() / 100.0 * 1.9558) / 1.20));
        contentStream.newLine();
        contentStream.endText();

        // Обобщение
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(55, 555);
        contentStream.showText("ДДС (20%): ");
        contentStream.newLineAtOffset(250, 0);
        contentStream.showText(String.format("%.2f BGN",(((paymentSessionRequest.price() / 100.0 * 1.9558) / 1.20) * 0.2)));
        contentStream.newLine();
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(55, 525);
        contentStream.showText("Обща дължима сума: ");
        contentStream.newLineAtOffset(250, 0);
        contentStream.showText(String.format("%.2f BGN",(paymentSessionRequest.price() / 100.0 * 1.9558)));
        contentStream.endText();

        // Футър с пояснения
        contentStream.setFont(font, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 100);
        contentStream.setLeading(14f);
        contentStream.showText("Моля, преведете сумата по следната сметка:");
        contentStream.newLine();
        contentStream.showText("IBAN: BGXXXXXXXXXXXX");
        contentStream.newLine();
        contentStream.showText("Краен срок за плащане: " + LocalDate.now().plusMonths(1));
        contentStream.newLine();
        contentStream.showText("Основание за плащане: Вашето име и номер на фактура.");
        contentStream.endText();

        contentStream.close();

        File invoiceFile = new File("EnhancedProformaInvoice.pdf");
        document.save(invoiceFile);
        document.close();

        return invoiceFile;
    }
}
