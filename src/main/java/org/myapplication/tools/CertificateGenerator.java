package org.myapplication.tools;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.myapplication.enumerate.Slot;
import org.myapplication.enumerate.Status;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.AppointmentModel;
import org.myapplication.models.UserModel;
import org.myapplication.models.VaccineModel;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class CertificateGenerator {

    private static final PDType1Font TITLE_FONT = PDType1Font.HELVETICA_BOLD;
    private static final PDType1Font SUBTITLE_FONT = PDType1Font.HELVETICA;
    private static final PDType1Font BODY_FONT = PDType1Font.HELVETICA;
    private static final int TITLE_FONT_SIZE = 30;
    private static final int SUBTITLE_FONT_SIZE = 10;
    private static final int HEADER_FONT_SIZE = 20;
    private static final int LABEL_FONT_SIZE = 14;
    private static final int BODY_FONT_SIZE = 12;
    private static final Color PRIMARY_COLOR = new Color(31, 73, 125);
    private static final Color SECONDARY_COLOR = Color.BLACK;
    private static final Color TABLE_HEADER_COLOR = Color.LIGHT_GRAY;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");

    public static PDDocument generatePDF(UserModel user, VaccineModel vaccine, String logoPath) {

        try {
            PDDocument doc = new PDDocument();

            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight()));
            doc.addPage(page);

            PDImageXObject logo = PDImageXObject.createFromFile(logoPath, doc);
            float x = 187;

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.drawImage(logo, x, 742, 50f, 50f);

                contentStream.setFont(TITLE_FONT, TITLE_FONT_SIZE);
                contentStream.beginText();
                contentStream.newLineAtOffset(238, 765);
                contentStream.showText("SafeDose");
                contentStream.endText();

                contentStream.setFont(SUBTITLE_FONT, SUBTITLE_FONT_SIZE);
                contentStream.beginText();
                contentStream.newLineAtOffset(238, 746);
                contentStream.showText("Empowering Wellness with Every Shot");
                contentStream.endText();

                contentStream.setNonStrokingColor(PRIMARY_COLOR);

                contentStream.setFont(TITLE_FONT, HEADER_FONT_SIZE);
                contentStream.beginText();
                contentStream.newLineAtOffset(175, 704);
                contentStream.showText("Certificate for Vaccination");
                contentStream.endText();

                contentStream.setFont(SUBTITLE_FONT, LABEL_FONT_SIZE);
                contentStream.beginText();
                contentStream.newLineAtOffset(170, 686);
                contentStream.showText("Issued in India by Care & Wellness Team");
                contentStream.endText();

                float labelX = 50;
                float valueX = 300;
                float currentY = 628; // Start y-position for the first line

                contentStream.setFont(TITLE_FONT, LABEL_FONT_SIZE);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, currentY); // Position for "Beneficiary Details"
                contentStream.showText("Beneficiary Details");
                contentStream.endText();

                contentStream.setNonStrokingColor(SECONDARY_COLOR);
                contentStream.setFont(BODY_FONT, BODY_FONT_SIZE);

                currentY -= 30;

                contentStream.beginText();
                contentStream.newLineAtOffset(labelX, currentY);
                contentStream.showText("Name:");
                contentStream.newLineAtOffset(valueX - labelX, 0); // Move to value column
                contentStream.showText(user.getFullName());
                currentY -= 20; // Move to next line

                contentStream.newLineAtOffset(-(valueX - labelX), -20); // Reset back to label column
                contentStream.showText("Aadhar Number:");
                contentStream.newLineAtOffset(valueX - labelX, 0);
                contentStream.showText("xxxx xxxx " + user.getAadharNumber().substring(8));
                currentY -= 20;

                String status = "Not Vaccinated";

                if (vaccine.getAppointments().length == vaccine.getDosages()) {
                    status = String.format("Fully Vaccinated (%d Dosages)", vaccine.getDosages());
                } else if (vaccine.getAppointments().length != 0) {
                    status = String.format("Partially Vaccinated (%d Dosage)", vaccine.getAppointments().length);
                }

                contentStream.newLineAtOffset(-(valueX - labelX), -20);
                contentStream.showText("Vaccinated Status:");
                contentStream.newLineAtOffset(valueX - labelX, 0);
                contentStream.showText(status);
                contentStream.endText();
                currentY -= 45;

                contentStream.setNonStrokingColor(PRIMARY_COLOR);

                contentStream.setFont(TITLE_FONT, HEADER_FONT_SIZE);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, currentY); // Position for "Beneficiary Details"
                contentStream.showText("Vaccine Details");
                contentStream.endText();

                contentStream.setNonStrokingColor(SECONDARY_COLOR);
                contentStream.setFont(BODY_FONT, BODY_FONT_SIZE);

                currentY -= 30;

                contentStream.beginText();
                contentStream.newLineAtOffset(labelX, currentY);
                contentStream.showText("Name:");
                contentStream.newLineAtOffset(valueX - labelX, 0); // Move to value column
                contentStream.showText(vaccine.getVaccineName());
                currentY -= 20; // Move to next line

                contentStream.newLineAtOffset(-(valueX - labelX), -20); // Reset back to label column
                contentStream.showText("Total Dosages:");
                contentStream.newLineAtOffset(valueX - labelX, 0);
                contentStream.showText(vaccine.getDosages() + " Dosages");
                contentStream.endText();
                currentY -= 45;

                // Assuming initial variables are defined: labelX, valueX, currentY
                int dosage = 1;
                float margin = 50;
                float tableWidth = 500;
                float tableHeight = 100;
                float cellHeight = 20;
                float cellMargin = 5;

                float[] columnWidths = {50, 100, 150, 100, 100}; // Adjust column widths as needed
                float[] columnPositions = new float[columnWidths.length];
                columnPositions[0] = labelX; // Start position of the first column
                for (int i = 1; i < columnWidths.length; i++) {
                    columnPositions[i] = columnPositions[i - 1] + columnWidths[i - 1];
                }

                contentStream.setNonStrokingColor(TABLE_HEADER_COLOR);
                contentStream.addRect(labelX, currentY - cellHeight, tableWidth, cellHeight);
                contentStream.fill();

                contentStream.setNonStrokingColor(SECONDARY_COLOR);
                contentStream.setFont(TITLE_FONT, BODY_FONT_SIZE);
                contentStream.beginText();
                contentStream.newLineAtOffset(columnPositions[0] + cellMargin, currentY - cellHeight + cellMargin);
                contentStream.showText("Dosage");
                contentStream.newLineAtOffset(columnWidths[0], 0);
                contentStream.showText("Appointment ID");
                contentStream.newLineAtOffset(columnWidths[1], 0);
                contentStream.showText("Location");
                contentStream.newLineAtOffset(columnWidths[2], 0);
                contentStream.showText("Slot");
                contentStream.newLineAtOffset(columnWidths[3], 0);
                contentStream.showText("Date");
                contentStream.endText();

                currentY -= cellHeight;

                contentStream.setFont(BODY_FONT, BODY_FONT_SIZE);
                for (AppointmentModel appointment : vaccine.getAppointments()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(columnPositions[0] + cellMargin, currentY - cellHeight + cellMargin);
                    contentStream.showText(dosage + "/" + vaccine.getDosages());

                    contentStream.newLineAtOffset(columnWidths[0], 0);
                    contentStream.showText(String.valueOf(appointment.getAppointmentId()));

                    contentStream.newLineAtOffset(columnWidths[1], 0);
                    contentStream.showText(appointment.getCampName());

                    contentStream.newLineAtOffset(columnWidths[2], 0);
                    contentStream.showText(appointment.getSlot());

                    contentStream.newLineAtOffset(columnWidths[3], 0);
                    contentStream.showText(DATE_FORMAT.format(appointment.getDate()));

                    contentStream.endText();

                    // Draw row border
                    contentStream.moveTo(labelX, currentY);
                    contentStream.lineTo(labelX + tableWidth, currentY);
                    contentStream.stroke();

                    currentY -= cellHeight;
                    dosage++;
                }

                contentStream.moveTo(labelX, currentY);
                contentStream.lineTo(labelX + tableWidth, currentY);
                contentStream.stroke();

            }

            return doc;
        } catch (IOException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static void main(String[] args) {

        UserModel user = new UserModel();

        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAadharNumber("123456789012");

        AppointmentModel first = new AppointmentModel();

        first.setAppointmentId(12);
        first.setSlot(Slot.MORNING);
        first.setStatus(Status.SUCCESS);
        first.setDate("2024-05-21");
        first.setCampName("chennai");

        AppointmentModel second = new AppointmentModel();

        second.setAppointmentId(14);
        second.setSlot(Slot.AFTERNOON);
        second.setStatus(Status.SUCCESS);
        second.setDate("2024-09-21");
        second.setCampName("chennai");

        VaccineModel vaccine = new VaccineModel();

        vaccine.setVaccineId(1);
        vaccine.setVaccineName("CoviShield");
        vaccine.setDosages(2);
        vaccine.setAppointments(new AppointmentModel[]{first, second});


        String dest = "utput.pdf"; // Specify the output PDF file path

        try (PDDocument document = generatePDF(user, vaccine, "src/main/webapp/WEB-INF/assets/SafeDoseLogo.png")) {
            document.save(dest);
            ColoredOutput.print("green","PDF created successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
