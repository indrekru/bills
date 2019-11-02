package com.ruubel.bills.service.billstrategy;

import java.util.Map;

public class EestiEnergiaBillStrategy extends AbstractBillStrategy {

    public final static String REF_NUMBER = "ref";
    public final static String TO_PAY = "to_pay";

    public Double extractToPay(Map<String, String> extractedLines) {
        if (extractedLines.containsKey(REF_NUMBER)) {
            String rawToPayLine = extractedLines.get(TO_PAY);
            rawToPayLine = rawToPayLine.replace(",", ".");
            rawToPayLine = rawToPayLine.replaceAll("[^\\d.]", "");
            return Double.parseDouble(rawToPayLine);
        }
        return null;
    }

}
