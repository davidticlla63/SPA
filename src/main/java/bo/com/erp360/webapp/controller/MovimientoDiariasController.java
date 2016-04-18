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
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.EmpleadoRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.FacturaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.UsuarioEmpresaRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empleado;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.util.Time;

@Named(value = "movimientoDiariasController")
@ConversationScoped
public class MovimientoDiariasController implements Serializable {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 5044859939046012205L;

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
	Event<String> pushEventComisionesDiarias;

	Logger log = Logger.getLogger(MovimientoDiariasController.class);

	// parametros filtros
	private int sucursalID;
	private Date fechaInicio = getPrimerDiaDelMes();
	private Date fechaFin = getUltimoDiaDelMes();
	private String gestion = obtenerGestion(new Date());

	private List<String> listaGestiones = new ArrayList<String>();
	private List<Sucursal> listaSucursales = new ArrayList<Sucursal>();

	// reporte LIBRO VENTAS
	private StreamedContent streamedLibroVentas; // OK PDF

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

	private Integer empleadoID;

	private List<Empleado> listaEmpleados = new ArrayList<Empleado>();
	private @Inject EmpleadoRepository empleadoEmpresaRepository;

	@PostConstruct
	public void initNewExportacion() {

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

		listaEmpleados = empleadoEmpresaRepository.findAllActive(empresaLogin);
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

	public void setStreamedLibroVentas(StreamedContent streamedLibroVentas) {
		this.streamedLibroVentas = streamedLibroVentas;
	}

	public int getSucursalID() {
		return sucursalID;
	}

	public void setSucursalID(int sucursalID) {
		this.sucursalID = sucursalID;
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

	public void generarExcel() {
		List<Factura> listaFacturas = new ArrayList<Factura>();
		listaFacturas = facturaRepository.traerComprasPeriodoFiscal("2015",
				"11", "1");
		log.info("Size : " + listaFacturas.size());
		/*ExcelGenerateReport report = new ExcelGenerateReport();*/
		// report.createExcel();
	}

	public void armarURLVentasNSF(Integer estado) {
		try {
			log.info("Ingreso a armarURLVentasNSF...");

			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			String urlLogo = urlPath + "resources/gfx/"
					+ sucursalLogin.getPathLogo();

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			urlVentasNSF = urlPath
					+ "ReporteComisionesDiarias?pFechaIni="
					+ URLEncoder.encode(
							Time.convertSimpleDateToString(fechaInicio),
							"UTF-8")
					+ "&pFechaFin="
					+ URLEncoder.encode(
							Time.convertSimpleDateToString(fechaFin), "UTF-8")
					+ "&pFechaI="
					+ URLEncoder.encode(
							Time.obtenerFormatoYYYYMMDD(fechaInicio), "UTF-8")
					+ "&pFechaF="
					+ URLEncoder.encode(Time.obtenerFormatoYYYYMMDD(fechaFin),
							"UTF-8") + "&pIdEmpleado="
					+ URLEncoder.encode("" + this.getEmpleadoID(), "UTF-8")
					+ "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pPais=" + URLEncoder.encode("BOLIVIA", "UTF-8")
					+ "&pLogo=" + URLEncoder.encode(urlLogo, "UTF-8")
					+ "&pEstado=" + URLEncoder.encode("" + estado, "UTF-8");
			log.info("URL Reporte urlVentasNSF: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
		}
	}

	public void armarURLVentasDiarias() {
		try {
			log.info("Ingreso a armarURLVentasDiarias...");

			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			String urlLogo = urlPath + "resources/gfx/"
					+ sucursalLogin.getPathLogo();

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			urlVentasNSF = urlPath
					+ "ReporteMovimientoDiario?pFechaIni="
					+ URLEncoder.encode(
							Time.convertSimpleDateToString(fechaInicio),
							"UTF-8")
					+ "&pFechaFin="
					+ URLEncoder.encode(
							Time.convertSimpleDateToString(fechaFin), "UTF-8")
					+ "&pFechaI="
					+ URLEncoder.encode(
							Time.obtenerFormatoYYYYMMDD(fechaInicio), "UTF-8")
					+ "&pFechaF="
					+ URLEncoder.encode(Time.obtenerFormatoYYYYMMDD(fechaFin),
							"UTF-8") + "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pPais=" + URLEncoder.encode("BOLIVIA", "UTF-8")
					+ "&pLogo=" + URLEncoder.encode(urlLogo, "UTF-8")
					+ "&pEstado=" + URLEncoder.encode("1", "UTF-8");
			log.info("URL Reporte armarURLVentasDiarias: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
		}
	}
	
	public void armarURLVentasDiariasProducto() {
		try {
			log.info("Ingreso a armarURLVentasDiarias...");

			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			String urlLogo = urlPath + "resources/gfx/"
					+ sucursalLogin.getPathLogo();

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			urlVentasNSF = urlPath
					+ "ReporteMovimientoDiario?pFechaIni="
					+ URLEncoder.encode(
							Time.convertSimpleDateToString(fechaInicio),
							"UTF-8")
					+ "&pFechaFin="
					+ URLEncoder.encode(
							Time.convertSimpleDateToString(fechaFin), "UTF-8")
					+ "&pFechaI="
					+ URLEncoder.encode(
							Time.obtenerFormatoYYYYMMDD(fechaInicio), "UTF-8")
					+ "&pFechaF="
					+ URLEncoder.encode(Time.obtenerFormatoYYYYMMDD(fechaFin),
							"UTF-8") + "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pPais=" + URLEncoder.encode("BOLIVIA", "UTF-8")
					+ "&pLogo=" + URLEncoder.encode(urlLogo, "UTF-8")
					+ "&pEstado=" + URLEncoder.encode("2", "UTF-8");
			log.info("URL Reporte armarURLVentasDiarias: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
		}
	}

	public void armarURLVentasDiariasServicios() {
		try {
			log.info("Ingreso a armarURLVentasDiarias...");

			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			String urlLogo = urlPath + "resources/gfx/"
					+ sucursalLogin.getPathLogo();

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			urlVentasNSF = urlPath
					+ "ReporteMovimientoDiario?pFechaIni="
					+ URLEncoder.encode(
							Time.convertSimpleDateToString(fechaInicio),
							"UTF-8")
					+ "&pFechaFin="
					+ URLEncoder.encode(
							Time.convertSimpleDateToString(fechaFin), "UTF-8")
					+ "&pFechaI="
					+ URLEncoder.encode(
							Time.obtenerFormatoYYYYMMDD(fechaInicio), "UTF-8")
					+ "&pFechaF="
					+ URLEncoder.encode(Time.obtenerFormatoYYYYMMDD(fechaFin),
							"UTF-8") + "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pPais=" + URLEncoder.encode("BOLIVIA", "UTF-8")
					+ "&pLogo=" + URLEncoder.encode(urlLogo, "UTF-8")
					+ "&pEstado=" + URLEncoder.encode("3", "UTF-8");
			log.info("URL Reporte armarURLVentasDiarias: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
		}
	}
	
	public void armarURLVentasDiariasNotaVenta() {
		try {
			log.info("Ingreso a armarURLVentasDiarias...");

			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);

			String urlLogo = urlPath + "resources/gfx/"
					+ sucursalLogin.getPathLogo();

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			urlVentasNSF = urlPath
					+ "ReporteMovimientoDiario?pFechaIni="
					+ URLEncoder.encode(
							Time.convertSimpleDateToString(fechaInicio),
							"UTF-8")
					+ "&pFechaFin="
					+ URLEncoder.encode(
							Time.convertSimpleDateToString(fechaFin), "UTF-8")
					+ "&pFechaI="
					+ URLEncoder.encode(
							Time.obtenerFormatoYYYYMMDD(fechaInicio), "UTF-8")
					+ "&pFechaF="
					+ URLEncoder.encode(Time.obtenerFormatoYYYYMMDD(fechaFin),
							"UTF-8") + "&pIdSucursal="
					+ URLEncoder.encode("" + this.getSucursalID(), "UTF-8")
					+ "&pPais=" + URLEncoder.encode("BOLIVIA", "UTF-8")
					+ "&pLogo=" + URLEncoder.encode(urlLogo, "UTF-8")
					+ "&pEstado=" + URLEncoder.encode("4", "UTF-8");
			log.info("URL Reporte armarURLVentasDiarias: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
		}
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

	public Integer getEmpleadoID() {
		return empleadoID;
	}

	public void setEmpleadoID(Integer empleadoID) {
		this.empleadoID = empleadoID;
	}

	public List<Empleado> getListaEmpleados() {
		return listaEmpleados;
	}

	public void setListaEmpleados(List<Empleado> listaEmpleados) {
		this.listaEmpleados = listaEmpleados;
	}

	public Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public Date getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

}
