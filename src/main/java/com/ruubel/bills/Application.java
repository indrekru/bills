package com.ruubel.bills;

import com.ruubel.bills.job.ReadGmailJob;
import com.ruubel.bills.service.GmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Application implements CommandLineRunner {


	@Autowired
	private GmailService gmailService;

	@Autowired
	private ReadGmailJob job;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Override
	public void run(String... args) {
//		try {
//			job.run();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

//		List<Double> imatraPrices = pdfExtractorService.extractAmounts(BillType.IMATRA, "elekter1.pdf");
//		List<Double> kuTatariPrices = pdfExtractorService.extractAmounts(BillType.KU_TATARI_60, "ku_arve1.pdf");
//
//		Double imatraBill = BillType.IMATRA.getBillMath().doTheMath(imatraPrices);
//		Double kuTatariBill = BillType.KU_TATARI_60.getBillMath().doTheMath(kuTatariPrices);
//		Double rent = 500.0;
//
//		System.out.println("Total: " + (imatraBill + kuTatariBill + rent));

//		try {
//			gmailService.readGmail();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
