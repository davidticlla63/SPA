package bo.com.erp360.webapp.util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;

@SuppressWarnings("deprecation")
@WebServlet("/ReporteLibroVentasDaVinci")
public class ReporteLibroVentasDaVinci extends HttpServlet {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6345314447300286913L;
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
        	//CAPTURANDO PARAMETROS
            String pGestion = request.getParameter("pGestion");
    		String pMes = request.getParameter("pMes");
    		String pMesNun = request.getParameter("pMesNun");
    		String pRazonSocial= request.getParameter("pRazonSocial");
    		String pIdSucursal= request.getParameter("pIdSucursal");
    		String pNit= request.getParameter("pNit");
    		String pEstado=request.getParameter("pEstado");
    			
    			
    		System.out.println("Conexion em: "+em.isOpen());
    			
    		@SuppressWarnings("deprecation")
			String realPath = request.getRealPath("/");
    		System.out.println("Real Path: "+realPath);

    		// load JasperDesign from XML and compile it into JasperReport
    		System.out.println("Context getServletContext: "+request.getServletContext().getContextPath());
    		System.out.println("Context getServletPath: "+request.getServletPath());
    		System.out.println("Context getSession().getServletContext(): "+request.getSession().getServletContext().getRealPath("/"));
    			
    			
    		String urlPath = request.getRequestURL().toString();
    		urlPath = urlPath.substring(0, urlPath.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
    		System.out.println("URL ::::: "+urlPath);
    		
    		String rutaReporte="";
			if (pEstado.equals("1")) {//TODOS LOS ESTADOS
				rutaReporte = urlPath
						+ "resources/report/Facturacion/reportLibroVentas.jasper";
			}
			if (pEstado.equals("2")) {//VALIDAS
				rutaReporte = urlPath
						+ "resources/report/Facturacion/reportLibroVentasValidas.jasper";
			}
			if (pEstado.equals("3")) {// ANULADAS
				rutaReporte = urlPath
						+ "resources/report/Facturacion/reportLibroVentasAnuladas.jasper";
			}
    		System.out.println("rutaReporte: "+rutaReporte);
    		
    		// create a map of parameters to pass to the report.       		
    		Map parameters = new HashMap();
    		parameters.put("ANIO", pGestion);
    		parameters.put("MES", pMes);
    		parameters.put("RAZON_SOCIAL", pRazonSocial);
    		parameters.put("MES_NUM", pMesNun);
    		parameters.put("NIT", pNit);
    		parameters.put("ID_SUCURSAL", new Integer(pIdSucursal));
    		parameters.put("REPORT_LOCALE", new Locale("en", "US"));
    		
    		System.out.println("parametos : "+parameters.toString());
    		//find file .jasper
			jasperReport = (JasperReport)JRLoader.loadObject (new URL(rutaReporte));

			// fill JasperPrint using fillReport() method
			jasperPrint = JasperFillManager.fillReport(jasperReport,parameters, conn);

			// -------------------------------------------------------
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			JRXlsExporter exporterXLS = new JRXlsExporter();

			exporterXLS.setParameter(JRXlsExporterParameter.IS_FONT_SIZE_FIX_ENABLED,Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
			exporterXLS.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_IGNORE_CELL_BORDER, Boolean.TRUE);
			//	exporterXLS.setParameter(JRXlsExporterParameter.IS_AUTO_DETECT_CELL_TYPE, Boolean.TRUE); 
			//	exporterXLS.setParameter(JRXlsExporterParameter.IS_AUTO_DETECT_CELL_TYPE, Boolean.TRUE);

			try {
				exporterXLS.exportReport();
			} catch (JRException ex) {
				System.out.println("No se pudo crear el excel..."+ex.getMessage());
			}

			response.setContentType("application/vnd.ms-excel");
			response.setContentLength(byteArrayOutputStream.toByteArray().length);
			response.setHeader("Content-disposition","attachment; filename=\"LibroVentasDaVinci.xls\"");


			servletOutputStream = response.getOutputStream();
			servletOutputStream.write( byteArrayOutputStream.toByteArray());
			servletOutputStream.flush();
			servletOutputStream.close();

			conn.close();
    	
		} catch (Exception e) {
			// display stack trace in the browser
			e.printStackTrace();
			System.out.println("Error en ReporteLibroVentasDaVinci: " + e.getMessage());
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
