package bo.com.erp360.webapp.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.log4j.Logger;
import org.hibernate.validator.constraints.br.CNPJ;
import org.jfree.util.Log;
import org.richfaces.application.push.impl.SessionFactoryImpl;

import bo.com.erp360.webapp.model.Factura;

@WebServlet("/ReportFactura5Col")
public class ReportFactura5ColServlet extends HttpServlet {

	@Inject
	private EntityManager em;

	Logger log = Logger.getLogger(ReportFactura5ColServlet.class);

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ServletOutputStream servletOutputStream = response.getOutputStream();
		JasperReport jasperReport;
		JasperPrint jasperPrint;

		Connection conn = null;
		DataSource ds = null;
		Context ctx = null;
		try {
			try {
				ctx = new InitialContext();
				ds = (DataSource) ctx.lookup(Conexion.datasourse);

				conn = ds.getConnection();

				if (conn != null) {
					System.out
							.println("Conexion Exitosa JDBC com.edb.Driver...");
				} else {
					System.out.println("Error Conexion JDBC com.edb.Driver...");
				}

			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error al conectar JDBC: " + e.getMessage());
			}

			try {

				String idFactura = request.getParameter("pIdFactura");
				String empresa = request.getParameter("pEmpresa");
				String ciudad = request.getParameter("pCiudad");
				String pais = request.getParameter("pPais");
				String logo = request.getParameter("pLogo");
				String nit = request.getParameter("pNit");
				String qr = request.getParameter("pQr");
				String pLeyenda = request.getParameter("pLeyenda");
				String pTamano = request.getParameter("pTamano");

				Factura factura = em
						.find(Factura.class, new Integer(idFactura));

				if (factura != null) {

					System.out.println("Conexion em: " + em.isOpen());

					String realPath = request.getRealPath("/");
					System.out.println("Real Path: " + realPath);

					// load JasperDesign from XML and compile it into
					// JasperReport
					System.out.println("Context getServletContext: "
							+ request.getServletContext().getContextPath());
					System.out.println("Context getServletPath: "
							+ request.getServletPath());
					System.out
							.println("Context getSession().getServletContext(): "
									+ request.getSession().getServletContext()
											.getRealPath("/"));

					String urlPath = request.getRequestURL().toString();
					urlPath = urlPath.substring(0, urlPath.length()
							- request.getRequestURI().length())
							+ request.getContextPath() + "/";
					System.out.println("URL ::::: " + urlPath);

					String urlsubReporte = urlPath
							+ "resources/report/Facturacion/";

					String URL_SERVLET_IMAGE_ANULADA = "";
					if (factura.getEstado().equals("A")) {

						URL_SERVLET_IMAGE_ANULADA = urlPath
								+ "resources/gfx/anulada.png";
					}

					String urlQR = urlPath + "codeQR?qrtext=" + qr;

					System.out.println("qrURL: " + urlQR);
					System.out.println("SUBREPORT_DIR: " + urlsubReporte);
					// create a map of parameters to pass to the report.
					Map parameters = new HashMap();
					parameters.put("id", new Integer(idFactura));
					parameters.put("empresa", empresa);
					parameters.put("ciudad", ciudad);
					parameters.put("pais", pais);
					parameters.put("logo", logo);
					parameters.put("nroNit", nit);
					parameters.put("SUBREPORT_DIR", urlsubReporte);
					parameters.put("qr", urlQR);
					parameters.put("leyenda", pLeyenda);
					parameters.put("pImageAnulada", URL_SERVLET_IMAGE_ANULADA);

					parameters.put("REPORT_LOCALE", new Locale("en", "US"));

					String rutaReporte = "";
					String tamano = pTamano;
					log.info("tamano : " + tamano);
					switch (tamano) {
					case "LEGAL":
						rutaReporte = urlPath
								+ "resources/report/Facturacion/reportFactura5Col.jasper";
						break;
					case "LETTER":
						rutaReporte = urlPath
								+ "resources/report/Facturacion/reportFacturaLetter.jasper";
						break;
					case "MINI":
						rutaReporte = urlPath
								+ "resources/report/Facturacion/factura.jasper";
						break;

					}

					log.info("Parametros : " + parameters.toString());

					log.info("rutaReporte: " + rutaReporte);

					jasperReport = (JasperReport) JRLoader.loadObject(new URL(
							rutaReporte));

					jasperPrint = JasperFillManager.fillReport(jasperReport,
							parameters, conn);

					// save report to path
					JasperExportManager.exportReportToPdfFile(
							jasperPrint,
							"/facturas/NotaVenta-NRO-"
									+ factura.getNumeroFactura() + "-NIT-"
									+ factura.getNitCi() + tamano + ".pdf");
					response.setContentType("application/pdf");
					response.setCharacterEncoding("UTF-8");
					
					JasperExportManager.exportReportToPdfStream(jasperPrint,
							servletOutputStream);

					servletOutputStream.flush();
					servletOutputStream.close();
				} else {
					log.info("Factura no encontrada x ID: " + idFactura);
				}

			} catch (Exception e) {
				// display stack trace in the browser
				e.printStackTrace();
				System.out.println("Error al ingresar RerpoteVentas: "
						+ e.getMessage());
				StringWriter stringWriter = new StringWriter();
				PrintWriter printWriter = new PrintWriter(stringWriter);
				e.printStackTrace(printWriter);
				response.setContentType("text/plain");
				response.getOutputStream().print(stringWriter.toString());

			}
		} catch (Exception e) {
			e.printStackTrace();

		}finally{
			try{
				if(!conn.isClosed()){
					System.out.println("cerrando conexion...");
					conn.close();
				}
			}catch(Exception e){
				System.out.println("No se pudo cerrar la conexion, Error: "+e.getMessage());
			}
		}

	}

}
