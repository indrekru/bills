package com.ruubel.bills.service;

import org.springframework.stereotype.Service;

@Service
public class PDFExtractorService {


//    public List<Double> extractAmounts(BillType billType, byte[] bytes) {
//        List<Double> out = new ArrayList<>();
//        try {
//            PDDocument document = PDDocument.load(bytes);
//            document.getClass();
//            if (!document.isEncrypted()) {
//
//                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
//                stripper.setSortByPosition(true);
//
//                PDFTextStripper tStripper = new PDFTextStripper();
//
//                String pdfFileInText = tStripper.getText(document);
//
//                // split by whitespace
//                String lines[] = pdfFileInText.split("\\r?\\n");
//                Map<String, Integer> config = billType.getConfig();
//                for (String line : lines) {
//                    line = line.trim();
//                    for (String key : config.keySet()) {
//                        if (line.contains(key)) {
//                            String[] parts = line.split(" ");
//                            String target = parts[config.get(key)];
//                            target = target.replaceAll("[^\\d.]", "");
//                            Double extractedPrice = Double.parseDouble(target);
//                            out.add(extractedPrice);
//                        }
//                    }
//                }
//            }
//            document.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return out;
//    }

}
