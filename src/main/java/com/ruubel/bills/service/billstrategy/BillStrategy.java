package com.ruubel.bills.service.billstrategy;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.ruubel.bills.model.Bill;

import java.util.Map;

public interface BillStrategy {

    Double getToPay(Bill bill, Message message, Gmail gmail) throws Exception;
    Map<String, String> extractLines(Map<String, String> neededLines, byte[] bytes);
    Double extractToPay(Map<String, String> extractedLines);

}
