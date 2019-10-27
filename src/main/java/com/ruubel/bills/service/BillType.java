package com.ruubel.bills.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.ruubel.bills.model.Bill;
import com.ruubel.bills.service.billstrategy.BillStrategy;
import com.ruubel.bills.service.billstrategy.Tatari60BillStrategy;

public enum BillType {

    KU_TATARI_60(new Tatari60BillStrategy());

    private BillStrategy strategy;

    BillType(BillStrategy strategy) {
        this.strategy = strategy;
    }

    public Double getToPay(Bill bill, Message message, Gmail gmail) {
        try {
            return strategy.getToPay(bill, message, gmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
