package com.ruubel.bills.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PDFExtractorService {


    public List<Double> extractAmounts(BillType billType, byte[] bytes) {
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
                Map<String, Integer> config = billType.getConfig();
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
