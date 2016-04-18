package bo.com.erp360.webapp.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.FacturaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;

@Named(value = "exportacionController")
@ConversationScoped
public class ExportacionController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5976317835956498815L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	private EntityManager em;

	@Inject
	Conversation conversation;

	@Inject
	FacturaRepository facturaRepository;

	@Inject
	SucursalRepository sucursalRepository;

	@Inject
	UsuarioRepository usuarioRepository;

	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventExportaciones;

	Logger log = Logger.getLogger(ExportacionController.class);

	// parametros filtros
	private int sucursalID;
	private String mes = obtenerMes(new Date());
	private String gestion = obtenerGestion(new Date());
	private Integer numeroFolio;

	private List<String> listaGestiones = new ArrayList<String>();
	private List<Sucursal> listaSucursales = new ArrayList<Sucursal>();

	// reporte LIBRO VENTAS
	private StreamedContent streamedLibroVentas; // OK PDF
	private StreamedContent streamedLibroVentasSFV; // OK PDF

	private StreamedContent streamedLibroVentasDaVinci; // OK EXCEL
	private StreamedContent streamedLibroVentasSFVExcel; // OK EXCEL

	private StreamedContent streamedLibroVentasTXT; // TXT PENDIENTE

	private StreamedContent streamedLibroVentasValidasTXT; // TXT VALIDAS

	private StreamedContent streamedLibroVentasAnuladasTXT; // TXT ANULADAS

	// vista previa Ventas
	private String urlVentasNSF;
	private String urlVentasSFV;

	// vista previa compras
	private String urlComprasNSF;
	private String urlComprasSFV;
	private Gestion gestionLogin;
	private EstadoUsuarioLogin estadoUsuarioLogin;

	@Inject
	private GestionRepository gestionRepository;

	@Inject
	private EmpresaRepository empresaRepository;
	private Empresa empresaLogin;

	private String nombreUsuario;

	private Sucursal sucursalLogin;

	@PostConstruct
	public void initNewExportacion() {

		// initConversation();
		beginConversation();

		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);

		nombreUsuario = estadoUsuarioLogin.getNombreUsuarioSession();
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);
		gestionLogin = estadoUsuarioLogin.getGestionSession(empresaRepository,
				gestionRepository);
		sucursalLogin = estadoUsuarioLogin.getSucursalSession(
				empresaRepository, sucursalRepository);

		log.info("Sucursal Usuario: " + sucursalLogin.getNombre());
		// indicar sucursal x defecto
		sucursalID = sucursalLogin.getId();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		setEmpresaLogin(estadoUsuarioLogin.getEmpresaSession(empresaRepository));

		gestion = gestionLogin.getGestion() + "";

		log.info("Gestion : " + gestion);
		log.info("Sucursal Usuario: " + sucursalLogin.getNombre());
		// actualizar lista de sucursales
		listaSucursales.clear();
		listaSucursales = sucursalRepository
				.findAllActivasByEmpresa(empresaLogin);
		setGestionLogin(estadoUsuarioLogin.getGestionSession(empresaRepository,
				gestionRepository));
		// actualizar gestiones disponibles facturadas
		listaGestiones = facturaRepository.traerGestionesFacturadas();
	}

	public static Date getPrimerDiaDelMes() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.getActualMinimum(Calendar.DAY_OF_MONTH),
				cal.getMinimum(Calendar.HOUR_OF_DAY),
				cal.getMinimum(Calendar.MINUTE),
				cal.getMinimum(Calendar.SECOND));
		return cal.getTime();
	}

	public static Date getUltimoDiaDelMes() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.getActualMaximum(Calendar.DAY_OF_MONTH),
				cal.getMaximum(Calendar.HOUR_OF_DAY),
				cal.getMaximum(Calendar.MINUTE),
				cal.getMaximum(Calendar.SECOND));
		return cal.getTime();
	}

	public void beginConversation() {

		if (conversation.isTransient()) {
			log.info("beginning conversation : " + this.conversation);
			conversation.begin();
			log.info("---> Init Conversation");
		}
	}

	public void endConversation() {
		if (!conversation.isTransient()) {
			conversation.end();
		}
	}

	public Sucursal getSucursal(int sucursalId) {
		try {
			return em.find(Sucursal.class, sucursalId);
		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en getSucursal: " + e.getMessage());
			return null;
		}
	}

	private static File stream2file(InputStream in) throws IOException {

		final File tempFile = File.createTempFile("Reporte", ".pdf");
		tempFile.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(tempFile)) {
			IOUtils.copy(in, out);
		}

		return tempFile;
	}

	private static File stream2fileTXT(InputStream in) throws IOException {

		final File tempFile = File.createTempFile("Reporte", ".txt");
		tempFile.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(tempFile)) {
			IOUtils.copy(in, out);
		}

		return tempFile;
	}

	private static File stream2fileExcel(InputStream in) throws IOException {

		final File tempFile = File.createTempFile("Reporte", ".xls");
		tempFile.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(tempFile)) {
			IOUtils.copy(in, out);
		}

		return tempFile;
	}

	public StreamedContent getStreamedLibroVentasAnuladas() {
		try {
			log.info("Ingreso a descargar LibroVentasNotariado...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3
			String urlPDFreporte = urlPath
					+ "ReporteLibroVentasSFV?pGestion="
					+ URLEncoder.encode(this.getGestion(), "UTF-8")
					+ "&pMes="
					+ URLEncoder.encode(obtenerMesLiteral(this.getMes()),
							"UTF-8")
					+ "&pMesNun="
					+ URLEncoder.encode(this.getMes(), "UTF-8")
					+ "&pRazonSocial="
					+ URLEncoder.encode(
							this.getEmpresaLogin().getRazonSocial(),
							"ISO-8859-1")
					+ "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pNit="
					+ URLEncoder.encode(this.getEmpresaLogin().getNit(),
							"UTF-8")
					+ "&pDireccion="
					+ URLEncoder.encode("" + sucursal.getDireccion(),
							"ISO-8859-1")
					+ "&pSucursal="
					+ URLEncoder
							.encode("" + sucursal.getNombre(), "ISO-8859-1")
					+ "&pEstado=" + URLEncoder.encode("3", "UTF-8");
			log.info("URL Reporte PDF: " + urlPDFreporte);

			// URLEncoder.encode(
			// dosificacion.getLeyendaInferior2(),"ISO-8859-1")

			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2file(is1);
			log.info("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			streamedLibroVentas = new DefaultStreamedContent(stream,
					"application/pdf", "LibroVentasNotariado.pdf");
			return streamedLibroVentas;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en getStreamedLibroVentas: " + e.getMessage());
			return null;
		}
	}

	public StreamedContent getStreamedLibroVentasValidas() {
		try {
			log.info("Ingreso a descargar LibroVentasNotariado...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			request = (HttpServletRequest) facesContext.getExternalContext()
					.getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3
			String urlPDFreporte = urlPath
					+ "ReporteLibroVentasSFV?pGestion="

					+ URLEncoder.encode(this.getGestion(), "UTF-8")
					+ "&pMes="
					+ URLEncoder.encode(obtenerMesLiteral(this.getMes()),
							"UTF-8")
					+ "&pMesNun="
					+ URLEncoder.encode(this.getMes(), "UTF-8")
					+ "&pRazonSocial="
					+ URLEncoder.encode(
							this.getEmpresaLogin().getRazonSocial(),
							"ISO-8859-1")
					+ "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pNit="
					+ URLEncoder.encode(this.getEmpresaLogin().getNit(),
							"UTF-8")
					+ "&pDireccion="
					+ URLEncoder.encode("" + sucursal.getDireccion(),
							"ISO-8859-1")
					+ "&pSucursal="
					+ URLEncoder
							.encode("" + sucursal.getNombre(), "ISO-8859-1")
					+ "&pEstado=" + URLEncoder.encode("2", "UTF-8");
			log.info("URL Reporte PDF: " + urlPDFreporte);

			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2file(is1);
			log.info("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			streamedLibroVentas = new DefaultStreamedContent(stream,
					"application/pdf", "LibroVentasNotariado.pdf");
			return streamedLibroVentas;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en getStreamedLibroVentas: " + e.getMessage());
			return null;
		}
	}

	public StreamedContent getStreamedLibroVentas() {
		try {
			log.info("Ingreso a descargar LibroVentasNotariado...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			request = (HttpServletRequest) facesContext.getExternalContext()
					.getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3
			String urlPDFreporte = urlPath
					+ "ReporteLibroVentasSFV?pGestion="
					+ URLEncoder.encode(this.getGestion(), "UTF-8")
					+ "&pMes="
					+ URLEncoder.encode(obtenerMesLiteral(this.getMes()),
							"UTF-8")
					+ "&pMesNun="
					+ URLEncoder.encode(this.getMes(), "UTF-8")
					+ "&pRazonSocial="
					+ URLEncoder.encode(
							this.getEmpresaLogin().getRazonSocial(),
							"ISO-8859-1")
					+ "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pNit="
					+ URLEncoder.encode(this.getEmpresaLogin().getNit(),
							"UTF-8")
					+ "&pDireccion="
					+ URLEncoder.encode("" + sucursal.getDireccion(),
							"ISO-8859-1")
					+ "&pSucursal="
					+ URLEncoder
							.encode("" + sucursal.getNombre(), "ISO-8859-1")
					+ "&pEstado=" + URLEncoder.encode("1", "UTF-8");
			log.info("URL Reporte PDF: " + urlPDFreporte);

			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2file(is1);
			log.info("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			streamedLibroVentas = new DefaultStreamedContent(stream,
					"application/pdf", "LibroVentasNotariado.pdf");
			return streamedLibroVentas;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en getStreamedLibroVentas: " + e.getMessage());
			return null;
		}
	}

	public void descargarPDF() {
		log.info("Ingreso a descargar LibroVentasNotariado...");
		HttpServletRequest request = (HttpServletRequest) facesContext
				.getExternalContext().getRequest();
		request = (HttpServletRequest) facesContext.getExternalContext()
				.getRequest();
		String urlPath = request.getRequestURL().toString();
		urlPath = urlPath.substring(0, urlPath.length()
				- request.getRequestURI().length())
				+ request.getContextPath() + "/";
		log.info("urlPath >> " + urlPath);

		// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3
		String urlPDFreporte = urlPath + "ReporteLibroVentasSFV?pGestion="
				+ this.getGestion() + "&pMes="
				+ obtenerMesLiteral(this.getMes()) + "&pMesNun="
				+ this.getMes() + "&pRazonSocial="
				+ this.getEmpresaLogin().getRazonSocial() + "&pIdSucursal="
				+ this.getSucursalID() + "&pNit="
				+ this.getEmpresaLogin().getNit();
		log.info("URL Reporte PDF: " + urlPDFreporte);

		try {
			facesContext.getExternalContext().redirect(urlPDFreporte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String obtenerMes(Date fecha) {
		try {
			String DATE_FORMAT = "MM";
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.format(fecha);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en obtenerMes: " + e.getMessage());
			return null;
		}
	}

	public String obtenerGestion(Date fecha) {
		try {
			String DATE_FORMAT = "yyyy";
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.format(fecha);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en obtenerGestion: " + e.getMessage());
			return null;
		}
	}

	public void dialogClose1() {
		System.out.println("Ingreso a dialogClose1");
		RequestContext context1 = RequestContext.getCurrentInstance();
		context1.execute("PF('dlgVistaPrevia').hide();");
	}

	public void dialogClose() {
		System.out.println("Ingreso a dialogClose");
		RequestContext context1 = RequestContext.getCurrentInstance();
		context1.execute("PF('dlgVistaPreviaSFV').hide();");
	}

	public void setStreamedLibroVentas(StreamedContent streamedLibroVentas) {
		this.streamedLibroVentas = streamedLibroVentas;
	}

	public int getSucursalID() {
		return sucursalID;
	}

	public void setSucursalID(int sucursalID) {
		this.sucursalID = sucursalID;
	}

	public String getMes() {
		return mes;
	}

	public void setMes(String mes) {
		this.mes = mes;
	}

	public String getGestion() {
		return gestion;
	}

	public void setGestion(String gestion) {
		this.gestion = gestion;
	}

	public List<Sucursal> getListaSucursales() {
		return listaSucursales;
	}

	public void setListaSucursales(List<Sucursal> listaSucursales) {
		this.listaSucursales = listaSucursales;
	}

	public List<String> getListaGestiones() {
		return listaGestiones;
	}

	public void setListaGestiones(List<String> listaGestiones) {
		this.listaGestiones = listaGestiones;
	}

	public Integer getNumeroFolio() {
		return numeroFolio;
	}

	public void setNumeroFolio(Integer numeroFolio) {
		this.numeroFolio = numeroFolio;
	}

	public StreamedContent getStreamedLibroVentasDaVinci() {

		try {
			System.out
					.println("Ingreso a descargar getStreamedLibroVentasDaVinci...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			request = (HttpServletRequest) facesContext.getExternalContext()
					.getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			// http://localhost:8080/buffalo/ReporteLibroVentasDaVinci?pGestion=2015&pMes=01

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			String urlPDFreporte = urlPath
					+ "ReporteLibroVentasDaVinci?pGestion="

					+ URLEncoder.encode(this.getGestion(), "UTF-8")
					+ "&pMes="
					+ URLEncoder.encode(obtenerMesLiteral(this.getMes()),
							"UTF-8")
					+ "&pMesNun="
					+ URLEncoder.encode(this.getMes(), "UTF-8")
					+ "&pRazonSocial="
					+ URLEncoder.encode(
							this.getEmpresaLogin().getRazonSocial(),
							"ISO-8859-1")
					+ "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pNit="
					+ URLEncoder.encode(this.getEmpresaLogin().getNit(),
							"UTF-8")
					+ "&pDireccion="
					+ URLEncoder.encode("" + sucursal.getDireccion(),
							"ISO-8859-1")
					+ "&pSucursal="
					+ URLEncoder
							.encode("" + sucursal.getNombre(), "ISO-8859-1")
					+ "&pEstado=" + URLEncoder.encode("1", "UTF-8");

			log.info("URL : " + urlPDFreporte);
			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2fileExcel(is1);
			log.info("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			streamedLibroVentasDaVinci = new DefaultStreamedContent(stream,
					"application/vnd.ms-excel", "LibroVentasDaVinci.xls");
			return streamedLibroVentasDaVinci;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en getStreamedLibroVentasDaVinci: "
					+ e.getMessage());
			return null;
		}

	}

	public StreamedContent getStreamedLibroVentasDaVinciValidas() {

		try {
			System.out
					.println("Ingreso a descargar getStreamedLibroVentasDaVinci...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			request = (HttpServletRequest) facesContext.getExternalContext()
					.getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			// http://localhost:8080/buffalo/ReporteLibroVentasDaVinci?pGestion=2015&pMes=01
			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			String urlPDFreporte = urlPath
					+ "ReporteLibroVentasDaVinci?pGestion="

					+ URLEncoder.encode(this.getGestion(), "UTF-8")
					+ "&pMes="
					+ URLEncoder.encode(obtenerMesLiteral(this.getMes()),
							"UTF-8")
					+ "&pMesNun="
					+ URLEncoder.encode(this.getMes(), "UTF-8")
					+ "&pRazonSocial="
					+ URLEncoder.encode(
							this.getEmpresaLogin().getRazonSocial(), "UTF-8")
					+ "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pNit="
					+ URLEncoder.encode(this.getEmpresaLogin().getNit(),
							"UTF-8") + "&pDireccion="
					+ URLEncoder.encode("" + sucursal.getDireccion(), "UTF-8")
					+ "&pSucursal="
					+ URLEncoder.encode("" + sucursal.getNombre(), "UTF-8")
					+ "&pEstado=" + URLEncoder.encode("2", "UTF-8");

			/*
			 * String urlPDFreporte = urlPath +
			 * "ReporteLibroVentasDaVinci?pGestion="
			 * 
			 * + this.getGestion() + "&pMes="+obtenerMesLiteral(this.getMes()) +
			 * "&pMesNun=" + this.getMes() + "&pRazonSocial=" +
			 * this.getEmpresaLogin().getRazonSocial() + "&pIdSucursal=" +
			 * this.getSucursalID() + "&pNit=" +
			 * this.getEmpresaLogin().getNit()+ "&pDireccion=" +
			 * sucursal.getDireccion() + "&pSucursal=" + "" +
			 * sucursal.getNombre() + "&pEstado="+2;
			 */
			log.info("URL : " + urlPDFreporte);
			URL url = new URL(urlPDFreporte);
			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2fileExcel(is1);
			System.out.println("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			streamedLibroVentasDaVinci = new DefaultStreamedContent(stream,
					"application/vnd.ms-excel", "LibroVentasDaVinci.xls");
			return streamedLibroVentasDaVinci;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error en getStreamedLibroVentasDaVinci: "
					+ e.getMessage());
			return null;
		}

	}

	public StreamedContent getStreamedLibroVentasDaVinciAnuladas() {

		try {
			System.out
					.println("Ingreso a descargar getStreamedLibroVentasDaVinci...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			request = (HttpServletRequest) facesContext.getExternalContext()
					.getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			// http://localhost:8080/buffalo/ReporteLibroVentasDaVinci?pGestion=2015&pMes=01
			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			String urlPDFreporte = urlPath
					+ "ReporteLibroVentasDaVinci?pGestion="

					+ URLEncoder.encode(this.getGestion(), "UTF-8")
					+ "&pMes="
					+ URLEncoder.encode(obtenerMesLiteral(this.getMes()),
							"UTF-8")
					+ "&pMesNun="
					+ URLEncoder.encode(this.getMes(), "UTF-8")
					+ "&pRazonSocial="
					+ URLEncoder.encode(
							this.getEmpresaLogin().getRazonSocial(),
							"ISO-8859-1")
					+ "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pNit="
					+ URLEncoder.encode(this.getEmpresaLogin().getNit(),
							"UTF-8")
					+ "&pDireccion="
					+ URLEncoder.encode("" + sucursal.getDireccion(),
							"ISO-8859-1")
					+ "&pSucursal="
					+ URLEncoder
							.encode("" + sucursal.getNombre(), "ISO-8859-1")
					+ "&pEstado=" + URLEncoder.encode("3", "UTF-8");

			log.info("URL : " + urlPDFreporte);
			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2fileExcel(is1);
			log.info("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			streamedLibroVentasDaVinci = new DefaultStreamedContent(stream,
					"application/vnd.ms-excel", "LibroVentasDaVinci.xls");
			return streamedLibroVentasDaVinci;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en getStreamedLibroVentasDaVinci: "
					+ e.getMessage());
			return null;
		}

	}

	public void generarExcel() {
		List<Factura> listaFacturas = new ArrayList<Factura>();
		listaFacturas = facturaRepository.traerComprasPeriodoFiscal("2015",
				"11", "1");
		log.info("Size : " + listaFacturas.size());
		/* ExcelGenerateReport report = new ExcelGenerateReport(); */
		// report.createExcel();
	}

	public void setStreamedLibroVentasDaVinci(
			StreamedContent streamedLibroVentasDaVinci) {
		this.streamedLibroVentasDaVinci = streamedLibroVentasDaVinci;
	}

	public StreamedContent getStreamedLibroVentasTXT() {

		try {
			System.out
					.println("Ingreso a descargar getStreamedLibroVentasTXT...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			request = (HttpServletRequest) facesContext.getExternalContext()
					.getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			// http://localhost:8080/buffalo/ReporteLibroVentasTXT?pGestion=2015&pMes=01
			String urlPDFreporte = urlPath + "ReporteLibroVentasTXT?pGestion="
					+ this.getGestion() + "&pMes=" + this.getMes()
					+ "&pIdSucursal=" + this.sucursalID + "&pEstado="
					+ URLEncoder.encode("1", "UTF-8");
			log.info("URL Reporte PDF: " + urlPDFreporte);

			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2fileTXT(is1);
			log.info("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			// ventas_mmaaaa_9999999999.TXT
			streamedLibroVentasTXT = new DefaultStreamedContent(stream,
					"text/plain", "ventas_" + this.getMes() + this.getGestion()
							+ "_" + empresaLogin.getNit() + ".txt");
			return streamedLibroVentasTXT;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en getStreamedLibroVentasTXT: " + e.getMessage());
			return null;
		}

	}

	public void setStreamedLibroVentasTXT(StreamedContent streamedLibroVentasTXT) {
		this.streamedLibroVentasTXT = streamedLibroVentasTXT;
	}

	public StreamedContent getStreamedLibroVentasValidasTXT() {
		try {
			System.out
					.println("Ingreso a descargar getStreamedLibroVentasValidasTXT...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			request = (HttpServletRequest) facesContext.getExternalContext()
					.getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			// http://localhost:8080/buffalo/ReporteLibroVentasTXT?pGestion=2015&pMes=01
			String urlPDFreporte = urlPath + "ReporteLibroVentasTXT?pGestion="
					+ this.getGestion() + "&pMes=" + this.getMes()
					+ "&pIdSucursal=" + this.sucursalID + "&pEstado="
					+ URLEncoder.encode("2", "UTF-8");
			log.info("URL Reporte PDF: " + urlPDFreporte);

			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2fileTXT(is1);
			log.info("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			// ventas_mmaaaa_9999999999.TXT
			streamedLibroVentasValidasTXT = new DefaultStreamedContent(stream,
					"text/plain", "ventas_" + this.getMes() + this.getGestion()
							+ "_" + empresaLogin.getNit() + ".txt");
			return streamedLibroVentasValidasTXT;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en streamedLibroVentasValidasTXT: "
					+ e.getMessage());
			return null;
		}
	}

	public void setStreamedLibroVentasValidasTXT(
			StreamedContent streamedLibroVentasValidasTXT) {
		this.streamedLibroVentasValidasTXT = streamedLibroVentasValidasTXT;
	}

	public StreamedContent getStreamedLibroVentasAnuladasTXT() {
		try {
			System.out
					.println("Ingreso a descargar getStreamedLibroVentasAnuladasTXT...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			request = (HttpServletRequest) facesContext.getExternalContext()
					.getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			// http://localhost:8080/buffalo/ReporteLibroVentasTXT?pGestion=2015&pMes=01
			String urlPDFreporte = urlPath + "ReporteLibroVentasTXT?pGestion="
					+ this.getGestion() + "&pMes=" + this.getMes()
					+ "&pIdSucursal=" + this.sucursalID + "&pEstado="
					+ URLEncoder.encode("3", "UTF-8");
			log.info("URL Reporte PDF: " + urlPDFreporte);

			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2fileTXT(is1);
			log.info("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			// ventas_mmaaaa_9999999999.TXT
			streamedLibroVentasAnuladasTXT = new DefaultStreamedContent(stream,
					"text/plain", "ventas_" + this.getMes() + this.getGestion()
							+ "_" + empresaLogin.getNit() + ".txt");
			return streamedLibroVentasAnuladasTXT;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en getStreamedLibroVentasAnuladasTXT: "
					+ e.getMessage());
			return null;
		}

	}

	public void setStreamedLibroVentasAnuladasTXT(
			StreamedContent streamedLibroVentasAnuladasTXT) {
		this.streamedLibroVentasAnuladasTXT = streamedLibroVentasAnuladasTXT;
	}

	public StreamedContent getStreamedLibroVentasSFV() {

		try {
			System.out
					.println("Ingreso a descargar getStreamedLibroVentasSFV...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			request = (HttpServletRequest) facesContext.getExternalContext()
					.getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

			String urlPDFreporte = urlPath + "ReporteLibroVentasSFV?pGestion="
					+ this.getGestion() + "&pMes="
					+ obtenerMesLiteral(this.getMes()) + "&pMesNun="
					+ this.getMes() + "&pRazonSocial="
					+ this.getEmpresaLogin().getRazonSocial() + "&pIdSucursal="
					+ this.getSucursalID() + "&pNit="
					+ this.getEmpresaLogin().getNit();
			log.info("URL Reporte PDF: " + urlPDFreporte);

			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2file(is1);
			log.info("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			streamedLibroVentasSFV = new DefaultStreamedContent(stream,
					"application/pdf", "LibroVentasSFV.pdf");
			return streamedLibroVentasSFV;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en getStreamedLibroVentasSFV: " + e.getMessage());
			return null;
		}

	}

	public void setStreamedLibroVentasSFV(StreamedContent streamedLibroVentasSFV) {
		this.streamedLibroVentasSFV = streamedLibroVentasSFV;
	}

	public StreamedContent getStreamedLibroVentasSFVExcel() {

		try {
			System.out
					.println("Ingreso a descargar getStreamedLibroVentasSFVExcel...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			// http://localhost:8080/buffalo/ReporteLibroVentasSFVExcel?pGestion=2015&pMes=01
			String urlPDFreporte = urlPath
					+ "ReporteLibroVentasSFVExcel?pGestion="
					+ this.getGestion() + "&pMes=" + this.getMes();
			log.info("URL Reporte PDF: " + urlPDFreporte);

			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2fileExcel(is1);
			log.info("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			streamedLibroVentasSFVExcel = new DefaultStreamedContent(stream,
					"application/vnd.ms-excel", "LibroVentasSFV.xls");
			return streamedLibroVentasSFVExcel;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			log.info("Error en getStreamedLibroVentasSFVExcel: "
					+ e.getMessage());
			return null;
		}

	}

	public void setStreamedLibroVentasSFVExcel(
			StreamedContent streamedLibroVentasSFVExcel) {
		this.streamedLibroVentasSFVExcel = streamedLibroVentasSFVExcel;
	}

	public void armarURLVentasNSF() {
		try {
			log.info("Ingreso a armarURLVentasNSF...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3
			urlVentasNSF = urlPath
					+ "ReporteLibroVentasSFV?pGestion="

					+ URLEncoder.encode(this.getGestion(), "UTF-8")
					+ "&pMes="
					+ URLEncoder.encode(obtenerMesLiteral(this.getMes()),
							"UTF-8")
					+ "&pMesNun="
					+ URLEncoder.encode(this.getMes(), "UTF-8")
					+ "&pRazonSocial="
					+ URLEncoder.encode(
							this.getEmpresaLogin().getRazonSocial(),
							"ISO-8859-1")
					+ "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pNit="
					+ URLEncoder.encode(this.getEmpresaLogin().getNit(),
							"UTF-8")
					+ "&pDireccion="
					+ URLEncoder.encode("" + sucursal.getDireccion(),
							"ISO-8859-1")
					+ "&pSucursal="
					+ URLEncoder
							.encode("" + sucursal.getNombre(), "ISO-8859-1")
					+ "&pEstado=" + URLEncoder.encode("1", "UTF-8");

			log.info("URL Reporte urlVentasNSF: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
		}
	}

	public void armarURLVentasNSFValidas() {
		try {
			log.info("Ingreso a armarURLVentasNSF...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3
			urlVentasNSF = urlPath
					+ "ReporteLibroVentasSFV?pGestion="

					+ URLEncoder.encode(this.getGestion(), "UTF-8")
					+ "&pMes="
					+ URLEncoder.encode(obtenerMesLiteral(this.getMes()),
							"UTF-8")
					+ "&pMesNun="
					+ URLEncoder.encode(this.getMes(), "UTF-8")
					+ "&pRazonSocial="
					+ URLEncoder.encode(
							this.getEmpresaLogin().getRazonSocial(),
							"ISO-8859-1")
					+ "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pNit="
					+ URLEncoder.encode(this.getEmpresaLogin().getNit(),
							"UTF-8")
					+ "&pDireccion="
					+ URLEncoder.encode("" + sucursal.getDireccion(),
							"ISO-8859-1")
					+ "&pSucursal="
					+ URLEncoder
							.encode("" + sucursal.getNombre(), "ISO-8859-1")
					+ "&pEstado=" + URLEncoder.encode("2", "UTF-8");
			log.info("URL Reporte urlVentasNSF: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
		}
	}

	public void armarURLVentasNSFAnuladas() {
		try {
			log.info("Ingreso a armarURLVentasNSF...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3
			urlVentasNSF = urlPath
					+ "ReporteLibroVentasSFV?pGestion="

					+ URLEncoder.encode(this.getGestion(), "UTF-8")
					+ "&pMes="
					+ URLEncoder.encode(obtenerMesLiteral(this.getMes()),
							"UTF-8")
					+ "&pMesNun="
					+ URLEncoder.encode(this.getMes(), "UTF-8")
					+ "&pRazonSocial="
					+ URLEncoder.encode(
							this.getEmpresaLogin().getRazonSocial(),
							"ISO-8859-1")
					+ "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pNit="
					+ URLEncoder.encode(this.getEmpresaLogin().getNit(),
							"UTF-8")
					+ "&pDireccion="
					+ URLEncoder.encode("" + sucursal.getDireccion(),
							"ISO-8859-1")
					+ "&pSucursal="
					+ URLEncoder
							.encode("" + sucursal.getNombre(), "ISO-8859-1")
					+ "&pEstado=" + URLEncoder.encode("3", "UTF-8");
			log.info("URL Reporte urlVentasNSF: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
		}
	}

	public void armarURLVentasSFV() {
		try {
			log.info("Ingreso a armarURLVentasSFV...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			request = (HttpServletRequest) facesContext.getExternalContext()
					.getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);
			// CAPTURANDO PARAMETROS

			urlVentasSFV = urlPath + "ReporteLibroVentasSFV?pGestion="
					+ this.getGestion() + "&pMes="
					+ obtenerMesLiteral(this.getMes()) + "&pMesNun="
					+ this.getMes() + "&pRazonSocial="
					+ this.getEmpresaLogin().getRazonSocial() + "&pIdSucursal="
					+ this.getSucursalID() + "&pNit="
					+ this.getEmpresaLogin().getNit();
			log.info("URL Reporte urlVentasSFV: " + urlVentasNSF);

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasSFV: " + e.getMessage());
		}
	}

	public String obtenerMesLiteral(String mesnum) {
		switch (mesnum) {
		case "01":
			return "ENERO";
		case "02":
			return "FEBRERO";
		case "03":
			return "MARZO";
		case "04":
			return "ABRIL";
		case "05":
			return "MAYO";
		case "06":
			return "JUNIO";
		case "07":
			return "JULIO";
		case "08":
			return "AGOSTO";
		case "09":
			return "SEPTIEMBRE";
		case "10":
			return "OCTUBRE";
		case "11":
			return "NOVIEMBRE";
		case "12":
			return "DICIEMBRE";

		}
		return "";

	}

	public String getUrlVentasNSF() {
		return urlVentasNSF;
	}

	public void setUrlVentasNSF(String urlVentasNSF) {
		this.urlVentasNSF = urlVentasNSF;
	}

	public String getUrlVentasSFV() {
		return urlVentasSFV;
	}

	public void setUrlVentasSFV(String urlVentasSFV) {
		this.urlVentasSFV = urlVentasSFV;
	}

	public String getUrlComprasNSF() {
		return urlComprasNSF;
	}

	public void setUrlComprasNSF(String urlComprasNSF) {
		this.urlComprasNSF = urlComprasNSF;
	}

	public String getUrlComprasSFV() {
		return urlComprasSFV;
	}

	public void setUrlComprasSFV(String urlComprasSFV) {
		this.urlComprasSFV = urlComprasSFV;
	}

	public Gestion getGestionLogin() {
		return gestionLogin;
	}

	public void setGestionLogin(Gestion gestionLogin) {
		this.gestionLogin = gestionLogin;
	}

	public Empresa getEmpresaLogin() {
		return empresaLogin;
	}

	public void setEmpresaLogin(Empresa empresaLogin) {
		this.empresaLogin = empresaLogin;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public Sucursal getSucursalLogin() {
		return sucursalLogin;
	}

	public void setSucursalLogin(Sucursal sucursalLogin) {
		this.sucursalLogin = sucursalLogin;
	}

}
