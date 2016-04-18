package bo.com.erp360.webapp.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bo.com.erp360.webapp.data.FacturaRepository;
import bo.com.erp360.webapp.model.Factura;

@WebServlet("/ReporteLibroVentasTXT")
public class ReporteLibroVentasTXT extends HttpServlet {

	private static final long serialVersionUID = -3280103162817601529L;

	@Inject
	private EntityManager em;

	@Inject
	FacturaRepository facturaRepository;

	public String formatearFecha(Date date) {
		try {
			String DATE_FORMAT = "dd/MM/yyyy";
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.format(date);

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error en formatearFecha: " + e.getMessage());
			return null;
		}
	}

	public static String round(double value) {
		try {
			DecimalFormat df = new DecimalFormat("####0.00");
			// System.out.println("Value: " + df.format(value));
			return df.format(value);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error en round: " + e.getMessage());
			return "0.00";
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ServletOutputStream servletOutputStream = response.getOutputStream();

		// OutputStream out = response.getOutputStream();

		try {
			// CAPTURANDO PARAMETROS
			String pGestion = request.getParameter("pGestion");
			String pMes = request.getParameter("pMes");
			String pIdSucursal = request.getParameter("pIdSucursal");

			String pEstado = request.getParameter("pEstado");

			System.out
					.println("LIBRO VENTAS TXT GENERANDO =============>  pGestion: "
							+ pGestion + ", pMes: " + pMes);
			StringWriter stringWriter = new StringWriter();
			List<Factura> listaFacturas = new ArrayList<Factura>();

			if (!pGestion.isEmpty() && !pMes.isEmpty()) {
				if (pEstado.equals("1")) {
					listaFacturas = facturaRepository
							.traerComprasPeriodoFiscal(pGestion, pMes,
									pIdSucursal);
				}
				if (pEstado.equals("2")) {
					listaFacturas = facturaRepository
							.traerComprasPeriodoFiscalValidas(pGestion, pMes,
									pIdSucursal);
				}
				if (pEstado.equals("3")) {
					listaFacturas = facturaRepository
							.traerComprasPeriodoFiscalAnuladas(pGestion, pMes,
									pIdSucursal);
				}

				

				if (!listaFacturas.isEmpty()) {
					int index=0;
					int especificacion=3;
					for (Factura factura : listaFacturas) {
						index++;
						double importeExcentos = factura
								.getImporteExportaciones()
								+ factura.getImporteVentasGrabadasTasaCero();
						switch (factura.getEstado()) {
						case "V"://VALIDAS 
							//3|1|03/03/2016|58|395408600015844|V|5342721013|REYNALDO SUBIRANA|159|0|0|159|0|0|0|0.00|CC-AB-72-91-34
							//3|4|07/03/2016|58|395408600015844|V|5342721013|REYNALDO SUBIRANA|159|0|0|0|159|0|159|20.67|CC-AB-72-91-34
							stringWriter.append(especificacion + "|"
									+index + "|"
									+ Time.convertSimpleDateToString(factura.getFechaFactura())  + "|"
									+ factura.getNumeroFactura() + "|"
									+ factura.getNumeroAutorizacion() + "|"
									+ factura.getEstado() + "|"
									+factura.getNitCi() + "|"
									+ factura.getNombreFactura() + "|"
									+ round(factura.getTotalFacturado()) + "|"
									+ round(factura.getImporteICE()) + "|"
									+ round(importeExcentos) + "|"
									+ round(factura.getImporteVentasGrabadasTasaCero()) + "|"
									+ round(factura.getImporteSubTotal()) + "|"
									+ round(factura.getImporteDescuentosBonificaciones()) + "|"
									+ round(factura.getImporteBaseDebitoFiscal())+ "|"
									+ round(factura.getDebitoFiscal()) + "|"
									+ factura.getCodigoControl());
									stringWriter.append("\n");
							
							break;
						case "A"://ANULADA
							stringWriter.append(especificacion + "|"
									+index + "|"
									+ Time.convertSimpleDateToString(factura.getFechaFactura())  + "|"
									+ factura.getNumeroFactura() + "|"
									+ factura.getNumeroAutorizacion() + "|"
									+ factura.getEstado() + "|"
									+0 + "|"
									+ "ANULADA" + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|" 
									+ round(0) + "|"
									+ round(0) + "|"
									+ 0);
									stringWriter.append("\n");
							break;
						case "E"://EXTRAVIADA
							stringWriter.append(especificacion + "|"
									+index + "|"
									+ Time.convertSimpleDateToString(factura.getFechaFactura())  + "|"
									+ factura.getNumeroFactura() + "|"
									+ factura.getNumeroAutorizacion() + "|"
									+ factura.getEstado() + "|"
									+0 + "|"
									+ "SIN NOMBRE" + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0)
									+ "|" + round(0) + "|"
									+ 0);
									stringWriter.append("\n");
							break;
						case "N"://NO UTILIZADA
							stringWriter.append(especificacion + "|"
									+index + "|"
									+ Time.convertSimpleDateToString(factura.getFechaFactura())  + "|"
									+ factura.getNumeroFactura() + "|"
									+ factura.getNumeroAutorizacion() + "|"
									+ factura.getEstado() + "|"
									+0 + "|"
									+ "SIN NOMBRE" + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0) + "|"
									+ round(0)
									+ "|" + round(0) + "|"
									+ 0);
									stringWriter.append("\n");
							break;

						default:
							break;
						}
					
					
						
						
					}
				} else {
					stringWriter.append("No existen Datos.");
				}

			} else {
				stringWriter.append("No existen Parametros.");
			}

			System.out.println("Conexion em: " + em.isOpen());

			// save report to path
			response.setContentType("text/plain");
			response.getOutputStream().print(stringWriter.toString().replaceAll(",", "."));

			servletOutputStream.flush();
			servletOutputStream.close();

		} catch (Exception e) {
			// display stack trace in the browser
			e.printStackTrace();
			System.out.println("Error en ReporteLibroVentasTXT: "
					+ e.getMessage());
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);
			response.setContentType("text/plain");
			response.getOutputStream().print(stringWriter.toString());
		}

	}

}
