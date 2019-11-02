package com.ruubel.bills.service.billstrategy;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.ruubel.bills.model.Bill;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ruubel.bills.service.GmailService.USER;

abstract public class AbstractBillStrategy implements BillStrategy {

    public final static String EXTRACTION = "extraction";
    public final static String SENDER_EMAIL = "sender-email";

    public Double getToPay(Bill bill, Message message, Gmail gmail) throws Exception {

        MessagePart payload = message.getPayload();
        List<MessagePartHeader> headers = payload.getHeaders();
        Optional<MessagePartHeader> optionalFromHeader = headers
                .stream()
                .filter(header -> header.getName().equals("From"))
                .findFirst();

        if (optionalFromHeader.isPresent()) {
            MessagePartHeader fromHeader = optionalFromHeader.get();
            String senderEmail = fromHeader.getValue();
            String targetSenderEmail = (String) bill.getParameter(SENDER_EMAIL);
            if (senderEmail.contains(targetSenderEmail)) {
                Double toPay = getToPay(message.getId(), payload, gmail, (Map<String, String>) bill.getParameter(EXTRACTION));
                return toPay;
            }
        }
        return null;
    }

    public Double getToPay(String messageId, MessagePart payload, Gmail service, Map<String, String> neededLines) throws Exception {
        List<MessagePart> parts = payload.getParts();
        Double out = null;
        for (MessagePart part : parts) {
            String filename = part.getFilename().trim();
            if (!filename.isEmpty()) {
                MessagePartBody body = part.getBody();
                String attachmentId = body.getAttachmentId();
                body = service.users().messages().attachments().get(USER, messageId, attachmentId).execute();
                byte[] bytes = body.decodeData();
                Map<String, String> extractedLines = extractLines(neededLines, bytes);
                return extractToPay(extractedLines);
            }
        }
        return out;
    }

    public Map<String, String> extractLines(Map<String, String> neededLines, byte[] bytes) {
        Map<String, String> out = new HashMap<>();
        if (bytes == null) {
            return out;
        }
        try {
            PDDocument document = PDDocument.load(bytes);
            document.getClass();
            if (!document.isEncrypted()) {

                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);

                // split by whitespace
                String foundLines[] = pdfFileInText.split("\\r?\\n");
                for (String foundLine : foundLines) {
                    foundLine = foundLine.trim();
                    for (Map.Entry<String, String> entry : neededLines.entrySet()) {
                        if (foundLine.contains(entry.getValue())) {
                            out.put(entry.getKey(), foundLine);
                        }
                    }
                }
            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

}
