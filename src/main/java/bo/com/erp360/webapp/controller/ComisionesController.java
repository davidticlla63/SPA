package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.richfaces.cdi.push.Push;



import bo.com.erp360.webapp.data.EmpleadoRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.FacturaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empleado;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;

@Named(value = "comisionesController")
@ConversationScoped
public class ComisionesController implements Serializable {






	/**
	 * 
	 */
	private static final long serialVersionUID = -1258097387223913756L;

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
	Event<String> pushEventComisiones;

	Logger log = Logger.getLogger(ComisionesController.class);

	// parametros filtros
	private int sucursalID;
	private String mes = obtenerMes(new Date());
	private String gestion = obtenerGestion(new Date());
	private Integer numeroFolio;

	private List<String> listaGestiones = new ArrayList<String>();
	private List<Sucursal> listaSucursales = new ArrayList<Sucursal>();



	// vista previa Ventas
	private String urlVentasNSF;

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

		listaEmpleados = empleadoEmpresaRepository
				.findAllActive(empresaLogin);
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

	public void generarExcel() {
		List<Factura> listaFacturas = new ArrayList<Factura>();
		listaFacturas = facturaRepository.traerComprasPeriodoFiscal("2015",
				"11", "1");
		log.info("Size : " + listaFacturas.size());
		/*ExcelGenerateReport report = new ExcelGenerateReport();*/
		// report.createExcel();
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

			String urlLogo = urlPath + "resources/gfx/"
					+ sucursalLogin.getPathLogo();

			Sucursal sucursal = em.find(Sucursal.class, this.getSucursalID());
			log.info("Sucursal Seleccionada : " + sucursal.getNombre());

			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3
			urlVentasNSF = urlPath + "ReporteComisiones?pGestion="
					+ this.getGestion() + "&pMes="
					+ obtenerMesLiteral(this.getMes()) + "&pMesNun="
					+ this.getMes() + "&pIdEmpleado=" + this.getEmpleadoID()
					+ "&pIdSucursal=" + this.getSucursalID()
					+ "&pPais=BOLIVIA&pLogo=" + urlLogo+"&pEstado=" + 1;
			log.info("URL Reporte urlVentasNSF: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
		}
	}
	
	public void armarURLVentasNSFServicios() {
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

			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3
			urlVentasNSF = urlPath + "ReporteComisiones?pGestion="
					+ this.getGestion() + "&pMes="
					+ obtenerMesLiteral(this.getMes()) + "&pMesNun="
					+ this.getMes() + "&pIdEmpleado=" + this.getEmpleadoID()
					+ "&pIdSucursal=" + this.getSucursalID()
					+ "&pPais=BOLIVIA&pLogo=" + urlLogo+"&pEstado=" + 2;
			log.info("URL Reporte urlVentasNSF: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
		}
	}
	
	public void armarURLVentasNSFProductos() {
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

			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3
			urlVentasNSF = urlPath + "ReporteComisiones?pGestion="
					+ this.getGestion() + "&pMes="
					+ obtenerMesLiteral(this.getMes()) + "&pMesNun="
					+ this.getMes() + "&pIdEmpleado=" + this.getEmpleadoID()
					+ "&pIdSucursal=" + this.getSucursalID()
					+ "&pPais=BOLIVIA&pLogo=" + urlLogo+"&pEstado=" +3;
			log.info("URL Reporte urlVentasNSF: " + urlVentasNSF);
			// http://localhost:8080/buffalo/ReporteLibroVentasNotariado?pGestion=2015&pMes=01&pSucursal=3

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLVentasNSF: " + e.getMessage());
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

}
