package bo.com.erp360.webapp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import bo.com.erp360.webapp.data.FormatoEmpresaRepository;
import bo.com.erp360.webapp.model.FormatoEmpresa;

/**
 * Servlet implementation class ServletLogoEmpresa
 */
@WebServlet("/ServletLogoEmpresa")
public class ServletLogoEmpresa extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private @Inject FormatoEmpresaRepository FormatoEmpresaRepository;
	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ServletLogoEmpresa() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		byte[] imagenData = null;
		try{

			int idFormatoEmpresa =  Integer.parseInt(request.getParameter("idFormatoEmpresa"));
			FormatoEmpresa formatoEmpresa = FormatoEmpresaRepository.findById(idFormatoEmpresa);
			log.info("formatoEmpresa = "+formatoEmpresa+" - idFormatoEmpresa="+idFormatoEmpresa);
			if(formatoEmpresa == null){
				imagenData = toByteArrayUsingJava(getImageDefaul().getStream());
			}else{
				if(formatoEmpresa.getPesoFoto() == 0){
					imagenData = toByteArrayUsingJava(getImageDefaul().getStream());
				}else{
					imagenData = formatoEmpresa.getLogo();
				}
			}
			try{
				response.setContentType("image/jpeg");
				response.setHeader("Content-Disposition", "inline; filename=imagen.jpg");
				response.setHeader("Cache-control", "public");
				ServletOutputStream sout = response.getOutputStream();
				sout.write(imagenData);
				sout.flush();
				sout.close();
			} catch (Exception e) {
				log.error("Error imagen: "+e.getMessage());
			}
		}catch(Exception e){
			log.error("Error doGet: "+e.getMessage());
		}
	}

	private StreamedContent getImageDefaul() {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		InputStream stream = classLoader
				.getResourceAsStream("logo.png");
		return new DefaultStreamedContent(stream, "image/jpeg");
	}

	public static byte[] toByteArrayUsingJava(InputStream is) throws IOException{ 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int reads = is.read();
		while(reads != -1){
			baos.write(reads); reads = is.read(); 
		}
		return baos.toByteArray();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}