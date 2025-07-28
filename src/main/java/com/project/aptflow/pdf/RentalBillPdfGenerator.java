package com.project.aptflow.pdf;

import com.project.aptflow.entity.GenerateBillEntity;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class RentalBillPdfGenerator {

    private final ResourceLoader resourceLoader;

    public RentalBillPdfGenerator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public byte[] generateBillPdf(GenerateBillEntity generateBill) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try(PDDocument document = new PDDocument()){
            PDPage page = new PDPage();
            document.addPage(page);

            try(PDPageContentStream contentStream = new PDPageContentStream(document,page)) {
                float paddingTop = 750;
                // add logo
                Resource resource = resourceLoader.getResource("classpath:logo3.png");
                try(InputStream inputStream = resource.getInputStream()) {
                    PDImageXObject imageXObject = PDImageXObject.createFromByteArray(document,inputStream.readAllBytes(),"logo3.png");
                    contentStream.drawImage(imageXObject, 10, paddingTop-150, 200, 200);
                }
                // add header
                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_BOLD, 30);
                contentStream.newLineAtOffset(200,paddingTop-25);
                contentStream.showText("SaiPrabha Apartment");
                contentStream.endText();

                //add address and other details
                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_ROMAN,14);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(200,paddingTop-50);
                contentStream.showText("Green Valley , Kuntaliguli,PO Mangalagangothri ,Konaje Village");
                contentStream.newLine();
                contentStream.showText("Konaje Proper, Karnataka 574199");
                contentStream.newLine();
                contentStream.showText("Mobile: +91 8197479894");
                contentStream.newLine();
                contentStream.showText("Email: saiprabharooms@gmail.com");
                contentStream.endText();

                //draw separator line
                contentStream.moveTo(55,paddingTop-120);
                contentStream.lineTo(550,paddingTop-120);
                contentStream.stroke();

                //pdf content
                contentStream.beginText();
                contentStream.setFont(PDType1Font.TIMES_BOLD,18);
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(70,paddingTop-150);
                contentStream.showText("Rental Bill Details ");
                contentStream.newLine();
                contentStream.newLine();
                contentStream.endText();

                // draw table
                float margin=75;
                float yStart=575;
                float tableWidth=page.getMediaBox().getWidth()-2*margin;
                float yPosition=yStart;
                float rowHeight=20f;
                float cellMargin=5f;
                float textX=margin+cellMargin;
                float textY=yPosition-15;

                String[] headers={"Bill Number","Bill ID","Adhaar Number","Room Number","Booking ID","Total Amount"};
                String[] data={
                        String.valueOf(generateBill.getBillEntity().getId()),
                        String.valueOf(generateBill.getBillEntity().getId()),
                        generateBill.getUserEntity().getAdhaarNumber(),
                        generateBill.getRoomEntity().getRoomNumber(),
                        String.valueOf(generateBill.getBookingEntity().getId()),
                        String.valueOf(generateBill.getTotal())
                };

                contentStream.setFont(PDType1Font.TIMES_ROMAN,12);

                for (int i =0;i<headers.length;i++){
                    // draw headers row
                    drawRow(contentStream,headers[i],data[i],margin,yPosition,cellMargin,tableWidth,rowHeight);
                    yPosition-=rowHeight;
                }
            }
            document.save(byteArrayOutputStream);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void drawRow(PDPageContentStream contentStream, String header, String data, float x, float y, float cellMargin, float tableWidth, float rowHeight) throws IOException {
        float textX=x+cellMargin;
        float textY=y-15;
        //draw header cell
        contentStream.beginText();
        contentStream.newLineAtOffset(textX,textY);
        contentStream.showText(header);
        contentStream.endText();

        //draw header cell box
        contentStream.setLineWidth(1f);
        contentStream.addRect(x,y-rowHeight,tableWidth/2,rowHeight);
        contentStream.stroke();

        //draw data cell
        contentStream.beginText();
        contentStream.newLineAtOffset(textX+tableWidth/2,textY);
        contentStream.showText(data);
        contentStream.endText();

        //draw data cell box
        contentStream.addRect(x+tableWidth/2,y-rowHeight,tableWidth/2,rowHeight);
        contentStream.stroke();
    }
}
