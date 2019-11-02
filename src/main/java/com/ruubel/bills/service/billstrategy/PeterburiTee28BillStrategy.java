package com.ruubel.bills.service.billstrategy;

import java.util.Map;

import static com.ruubel.bills.service.billstrategy.Tatari60BillStrategy.FIRST_LINE;

public class PeterburiTee28BillStrategy extends AbstractBillStrategy {
    @Override
    public Double extractToPay(Map<String, String> extractedLines) {
        if (extractedLines.containsKey(FIRST_LINE)) {
            String rawLine = extractedLines.get(FIRST_LINE);
            String[] pieces = rawLine.split(" ");
            String rawPrice = pieces[3];
            rawPrice = rawPrice.replaceAll("[^\\d.]", "");
            return Double.parseDouble(rawPrice);
        }
        return null;
    }
}
