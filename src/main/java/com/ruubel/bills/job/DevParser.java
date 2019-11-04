package com.ruubel.bills.job;

import com.ruubel.bills.service.billstrategy.*;
import net.sourceforge.tess4j.Tesseract;
import org.json.JSONObject;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.ruubel.bills.service.billstrategy.AbstractBillStrategy.EXTRACTION;
import static com.ruubel.bills.service.billstrategy.AbstractBillStrategy.SENDER_EMAIL;
import static com.ruubel.bills.service.billstrategy.EestiEnergiaBillStrategy.REF_NUMBER;
import static com.ruubel.bills.service.billstrategy.EestiEnergiaBillStrategy.TO_PAY;
import static com.ruubel.bills.service.billstrategy.Tatari60BillStrategy.FIRST_LINE;
import static com.ruubel.bills.service.billstrategy.Tatari60BillStrategy.SECOND_LINE;

public class DevParser {

    public DevParser() {
    }

    public void run() {
//        System.out.println("Eesti energia:");
//        testEestiEnergia();
//        System.out.println("Tatari 60:");
//        testTatari60();
//        System.out.println("Paldiski 75:");
//        testPaldiski75();
//        System.out.println("Peterburi tee 28:");
//        testPeterburiTee28();
//        System.out.println("Elisa bill:");
//        testElisa();
    }

    private void testElisa() {

        BillStrategy strategy = new ElisaBillStartegy();

        Map<String, Object> params = new HashMap<String, Object>(){{
            put(SENDER_EMAIL, "noreply.arved@elisa.ee");
            put(EXTRACTION, new HashMap<String, String>(){{
                put(FIRST_LINE, "Kokku käesoleva");
            }});
        }};

//        JSONObject jsonObject = new JSONObject(params);
//        System.out.println(jsonObject);

        extractPrice(strategy, (Map<String, String>) params.get(EXTRACTION), "bills/elisa.pdf");
    }

    private void testPaldiski75() {

        Tesseract tesseract = new Tesseract();
        try {
            String content = tesseract.doOCR(ResourceUtils.getFile("bills/tatari60.pdf"));
            System.out.println(content);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        BillStrategy strategy = new Tatari60BillStrategy();
//
//        Map<String, Object> params = new HashMap<String, Object>(){{
//            put(SENDER_EMAIL, "paldiski75@gmail.com");
//            put(EXTRACTION, new HashMap<String, String>(){{
//                put(FIRST_LINE, "Kokku");
//            }});
//        }};

//        JSONObject jsonObject = new JSONObject(params);
//        System.out.println(jsonObject);

//        extractPrice(strategy, (Map<String, String>) params.get(EXTRACTION), "bills/paldiski75.pdf");
    }

    private void testPeterburiTee28() {
        BillStrategy strategy = new PeterburiTee28BillStrategy();

        Map<String, Object> params = new HashMap<String, Object>(){{
            put(SENDER_EMAIL, "rghalduse@gmail.com");
            put(EXTRACTION, new HashMap<String, String>(){{
                put(FIRST_LINE, "KOKKU (EUR)");
            }});
        }};

//        JSONObject jsonObject = new JSONObject(params);
//        System.out.println(jsonObject);

        extractPrice(strategy, (Map<String, String>) params.get(EXTRACTION), "bills/peterburitee28.pdf");
    }

    private void testTatari60() {
        BillStrategy strategy = new Tatari60BillStrategy();

        Map<String, Object> params = new HashMap<String, Object>(){{
            put(SENDER_EMAIL, "imre@heiberg.ee");
            put(EXTRACTION, new HashMap<String, String>(){{
                put(FIRST_LINE, "Remondikulud");
                put(SECOND_LINE, "Kokku:");
            }});
        }};

//        JSONObject jsonObject = new JSONObject(params);
//        System.out.println(jsonObject);

        extractPrice(strategy, (Map<String, String>) params.get(EXTRACTION), "bills/tatari60.pdf");
    }

    private void testEestiEnergia() {
        BillStrategy strategy = new EestiEnergiaBillStrategy();

        Map<String, Object> params = new HashMap<String, Object>(){{
            put(SENDER_EMAIL, "eestienergia@earvekeskus.ee");
            put(EXTRACTION, new HashMap<String, String>(){{
                put(TO_PAY, "Kuulub tasumisele");
                put(REF_NUMBER, "Viitenumber: 71998532127");
            }});
        }};

//        JSONObject jsonObject = new JSONObject(params);
//        System.out.println(jsonObject);

        extractPrice(strategy, (Map<String, String>) params.get(EXTRACTION), "bills/eesti_energia.pdf");
    }

    private void extractPrice(BillStrategy strategy, Map<String, String> neededLines, String fileLocation) {

        byte[] bytes = getFileBytes(fileLocation);

        Map<String, String> extractedLines = strategy.extractLines(neededLines, bytes);
        Double price = strategy.extractToPay(extractedLines);

        System.out.println(price);
    }

    private byte[] getFileBytes(String location) {
        File file = null;
        try {
            file = ResourceUtils.getFile(location);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (file.exists()) {
            return readFileToByteArray(file);
        }

        System.out.println("No file found at: " + location);

        return null;
    }

    private static byte[] readFileToByteArray(File file){
        FileInputStream fis;
        // Creating a byte array using the length of the file
        // file.length returns long which is cast to int
        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();

        }catch(IOException ioExp){
            ioExp.printStackTrace();
        }
        return bArray;
    }

    public static void main(String[] args) {
        DevParser parser = new DevParser();
        parser.run();
    }

}
