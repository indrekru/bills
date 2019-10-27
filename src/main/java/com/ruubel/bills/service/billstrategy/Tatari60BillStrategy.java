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

import java.util.*;

import static com.ruubel.bills.service.GmailService.USER;

public class Tatari60BillStrategy implements BillStrategy {

    @Override
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
            String targetSenderEmail = bill.getSenderEmail();
            if (senderEmail.contains(targetSenderEmail)) {
                Double toPay = getToPay(message.getId(), payload, gmail);
                return toPay;
            }
        }
        return null;
    }

    private Double getToPay(String messageId, MessagePart payload, Gmail service) throws Exception {

        Map<String, Integer> rows = new HashMap<String, Integer>() {{
            put("Remondikulud", 1);
            put("Kokku:", 1);
        }};


        List<MessagePart> parts = payload.getParts();
        Double out = null;
        for (MessagePart part : parts) {
            String filename = part.getFilename().trim();
            if (!filename.isEmpty()) {
                MessagePartBody body = part.getBody();
                String attachmentId = body.getAttachmentId();
                body = service.users().messages().attachments().get(USER, messageId, attachmentId).execute();
                byte[] bytes = body.decodeData();
                List<Double> prices = extractAmounts(rows, bytes);
                Double toPay = prices.get(1) - prices.get(0);
                return toPay;
            }
        }
        return out;
    }

    private List<Double> extractAmounts(Map<String, Integer> config, byte[] bytes) {
        List<Double> out = new ArrayList<>();
        try {
            PDDocument document = PDDocument.load(bytes);
            document.getClass();
            if (!document.isEncrypted()) {

                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);

                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);

                // split by whitespace
                String lines[] = pdfFileInText.split("\\r?\\n");
                for (String line : lines) {
                    line = line.trim();
                    for (String key : config.keySet()) {
                        if (line.contains(key)) {
                            String[] parts = line.split(" ");
                            String target = parts[config.get(key)];
                            target = target.replaceAll("[^\\d.]", "");
                            Double extractedPrice = Double.parseDouble(target);
                            out.add(extractedPrice);
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
