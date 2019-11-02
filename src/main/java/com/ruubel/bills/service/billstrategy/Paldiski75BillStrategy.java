package com.ruubel.bills.service.billstrategy;

import java.util.Map;

public class Paldiski75BillStrategy extends AbstractBillStrategy {
    @Override
    public Double extractToPay(Map<String, String> extractedLines) {
        System.out.println(extractedLines);
        return null;
    }
}
