package com.ruubel.bills.service.billstrategy;

import java.util.Map;

import static com.ruubel.bills.service.billstrategy.Tatari60BillStrategy.FIRST_LINE;

public class ElisaBillStartegy extends AbstractBillStrategy {
    @Override
    public Double extractToPay(Map<String, String> extractedLines) {
        if (extractedLines.containsKey(FIRST_LINE)) {
            String line = extractedLines.get(FIRST_LINE);
            line = line.replaceAll(",", ".");
            line = line.replaceAll("[^\\d.]", "");
            return Double.parseDouble(line);
        }
        return null;
    }
}
