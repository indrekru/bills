package com.ruubel.bills.service;

import java.util.HashMap;
import java.util.Map;

public enum BillType {
    KU_TATARI_60(new HashMap<String, Integer>() {{
        put("Remondikulud", 1);
        put("Kokku:", 1);
    }}, prices -> prices.get(1) - prices.get(0)),
    IMATRA(new HashMap<String, Integer>() {{
        put("ARVE KOKKU €", 2);
    }}, prices -> {
        return prices.get(0);
    });

    private Map<String, Integer> config;
    private BillMath billMath;

    BillType(Map<String, Integer> config, BillMath billMath) {
        this.config = config;
        this.billMath = billMath;
    }

    public Map<String, Integer> getConfig() {
        return config;
    }

    public BillMath getBillMath() {
        return billMath;
    }
}
