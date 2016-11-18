package bo.com.erp360.webapp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Sucursal;

/**
 * Servlet implementation class ServletLogoEmpresa
 */
@WebServlet("/ServletBackgroundFactura")
public class ServletBackgroundFactura extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private @Inject SucursalRepository sucursalRepository;
	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ServletBackgroundFactura() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		byte[] imagenData = null;
		try {

			int idFormatoEmpresa = Integer.parseInt(request
					.getParameter("idSucursal"));
			Sucursal formatoEmpresa = sucursalRepository
					.findById(idFormatoEmpresa);
			log.info("LogoEmpresa = " + formatoEmpresa + " - idFormatoEmpresa="
					+ idFormatoEmpresa);
			if (formatoEmpresa == null) {
				imagenData = toByteArrayUsingJava(getImageDefaul().getStream());
				
				imagenData = toByteArrayUsingJava(getImageDefaul(
						"logo.png", "image/jpeg").getStream());
			} else {
				if (formatoEmpresa.getPesoLogo() == 0) {
//					imagenData = toByteArrayUsingJava(getImageDefaul(
//							"logo.png", "image/jpeg").getStream());
					imagenData = toByteArrayUsingJava(getImageDefaul()
							.getStream());
				} else {
					imagenData = formatoEmpresa.getLogo();
				}
			}
			try {
				response.setContentType("image/jpeg");
				response.setHeader("Content-Disposition",
						"inline; filename=imagen.jpg");
				response.setHeader("Cache-control", "public");
				ServletOutputStream sout = response.getOutputStream();
				sout.write(imagenData);
				sout.flush();
				sout.close();
			} catch (Exception e) {
				log.error("Error imagen: " + e.getMessage());
			}
		} catch (Exception e) {
			log.error("Error doGet: " + e.getMessage());
		}
	}

	private StreamedContent getImageDefaul() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		InputStream stream = classLoader.getResourceAsStream("logo.png");
		return new DefaultStreamedContent(stream, "image/jpeg");
	}

	private StreamedContent getImageDefaul(String namePhoto, String type) {// logo-siga.png
		// ,
		// image/png
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		InputStream stream = classLoader.getResourceAsStream(namePhoto);
		return new DefaultStreamedContent(stream, type);
	}

	public static byte[] toByteArrayUsingJava(InputStream is)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int reads = is.read();
		while (reads != -1) {
			baos.write(reads);
			reads = is.read();
		}
		return baos.toByteArray();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

}