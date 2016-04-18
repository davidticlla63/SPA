package bo.com.erp360.webapp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainClass {
	public static void main(String[] args) {
		try {
			FileWriter fw = new FileWriter("c:/facturas/Factura-NRO-1-NIT-0LEGALfalse.pdf");
			PrintWriter pw = new PrintWriter(fw);
			String s = "PROBANDO ";
			int i, len = s.length();
			for (i = 0; len > 80; i += 80) {
				pw.print(s.substring(i, i + 80));
				pw.print("\r\n");
				len -= 80;
			}
			if (len > 0) {
				pw.print(s.substring(i));
				pw.print("\r\n");
			}
			pw.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}