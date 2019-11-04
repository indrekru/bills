package com.ruubel.bills.service.billstrategy;

import java.util.Map;

public class Tatari60BillStrategy extends AbstractBillStrategy {

    public final static String FIRST_LINE = "first";
    public final static String SECOND_LINE = "second";

    public Double extractToPay(Map<String, String> extractedLines) {
        if (!extractedLines.containsKey(SECOND_LINE)) {
            return null;
        }
        String rawSum = extractedLines.get(SECOND_LINE);
        rawSum = rawSum.replaceAll("[^\\d.]", "");
        Double sum = Double.parseDouble(rawSum);

        return sum;
    }

}
