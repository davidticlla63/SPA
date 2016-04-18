package bo.com.erp360.webapp.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;

@WebServlet("/ReporteTicket")
public class ReporteTicket extends HttpServlet {



	/**
	 * 
	 */
	private static final long serialVersionUID = 3694503356536921388L;
	@Inject
	private EntityManager em;

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
			// CAPTURANDO PARAMETROS
			String pIdOrden = request.getParameter("pIdOrden");
			
			String idFactura = request.getParameter("pIdFactura");
			String empresa = request.getParameter("pEmpresa");
			String ciudad = request.getParameter("pCiudad");
			String pais = request.getParameter("pPais");
			String logo = request.getParameter("pLogo");

			System.out.println("Conexion em: " + em.isOpen());

			@SuppressWarnings("deprecation")
			String realPath = request.getRealPath("/");
			System.out.println("Real Path: " + realPath);

			// load JasperDesign from XML and compile it into JasperReport
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

			String rutaReporte = urlPath
					+ "resources/report/Venta/reportTicket.jasper";
			System.out.println("rutaReporte: " + rutaReporte);

			// create a map of parameters to pass to the report.
			Map parameters = new HashMap();
			parameters.put("ID_ORDEN", new Integer(pIdOrden));
			parameters.put("empresa", empresa);
			parameters.put("ciudad", ciudad);
			parameters.put("pais", pais);
			parameters.put("logo", logo);
			parameters.put("REPORT_LOCALE", new Locale("en", "US"));
			// find file .jasper
			jasperReport = (JasperReport) JRLoader.loadObject(new URL(
					rutaReporte));

			jasperPrint = JasperFillManager.fillReport(jasperReport,
					parameters, conn);

			// save report to path
			// JasperExportManager.exportReportToPdfFile(jasperPrint,"C:/etiquetas/Etiqueta+"+pCodigoPre+"-"+pNombreElaborado+".pdf");
			response.setContentType("application/pdf");
			JasperExportManager.exportReportToPdfStream(jasperPrint,
					servletOutputStream);

			servletOutputStream.flush();
			servletOutputStream.close();

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
