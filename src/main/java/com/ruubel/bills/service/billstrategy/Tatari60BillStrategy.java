package com.ruubel.bills.service.billstrategy;

import java.util.Map;

public class Tatari60BillStrategy extends AbstractBillStrategy {

    public final static String FIRST_LINE = "first";
    public final static String SECOND_LINE = "second";

    public Double extractToPay(Map<String, String> extractedLines) {
        if (extractedLines.size() < 2) {
            return null;
        }
        String rawFixCosts = extractedLines.get(FIRST_LINE);
        String rawSum = extractedLines.get(SECOND_LINE);

        String[] fixCostsPieces = rawFixCosts.split(" ");
        Double fixCosts = Double.parseDouble(fixCostsPieces[1]);

        rawSum = rawSum.replaceAll("[^\\d.]", "");
        Double sum = Double.parseDouble(rawSum);

        return sum - fixCosts;
    }

}
