package bo.com.erp360.webapp.controller;

import java.io.IOException;
import java.io.Serializable;
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
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.DosificacionRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.FacturaRepository;
import bo.com.erp360.webapp.data.FormatoHojaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.TamanoHojaRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.FormatoHoja;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TamanoHoja;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.FacturaRegistration;
import bo.com.erp360.webapp.util.CodigoControl7;
import bo.com.erp360.webapp.util.Time;

@Named(value = "facturaIndexController")
@SuppressWarnings("serial")
@ConversationScoped
public class FacturaIndexController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject TamanoHojaRepository tamanoHojaRepository;
	@Inject
	private GestionRepository gestionRepository;

	@Inject
	private EmpresaRepository empresaRepository;

	@Inject
	private FacturaRepository facturaRepository;

	private Dosificacion dosificacion = new Dosificacion();

	@Inject
	private FacturaRegistration facturaRegistration;

	@Inject
	private DosificacionRepository dosificacionRepository;

	Logger log = Logger.getLogger(FacturaIndexController.class);

	private Date fechaInicio = getPrimerDiaDelMes();
	private Date fechaFin = getUltimoDiaDelMes();

	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Usuario usuarioSession;
	private Empresa empresaLogin;

	private Gestion gestionLogin;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private String nombreUsuario;
	private String nombreMes;
	private Integer numeroFactura;
	private String urlFactura;

	private List<Factura> listFactura = new ArrayList<Factura>();
	private List<Factura> listFilterFactura = new ArrayList<Factura>();
	private String[] arrayMes = { "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO",
			"JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE",
			"DICIEMBRE", "TODO" };

	private Factura selectedFactura;

	// estados
	private boolean seleccionado = false;
	private boolean crear = true;
	private boolean seleccionarFactura = false;

	private String urlCodeQR;

	private String url;

	private @Inject FormatoHojaRepository formatoHojaRepository;
	private FormatoHoja formatoHoja;

	private Sucursal sucursalLogin;

	private @Inject SucursalRepository sucursalRepository;
	private @Inject UsuarioRepository usuarioRepository;
	

	@PostConstruct
	public void initNewReporteFactura() {

		log.info(" init new initNewReporteFactura");
		beginConversation();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);
		gestionLogin = estadoUsuarioLogin.getGestionSession(empresaRepository,
				gestionRepository);
		sucursalLogin = estadoUsuarioLogin.getSucursalSession(
				empresaRepository, sucursalRepository);
		usuarioSession=estadoUsuarioLogin.getUsuarioSession(usuarioRepository);
		System.out.println("Usuario : "+usuarioSession.getLogin());
		loadValuesDefaul();
	}

	private void loadValuesDefaul() {
		consultar();
		urlFactura = "";
		seleccionado = false;
		nombreMes = "TODO";
		numeroFactura = 0;

		selectedFactura = new Factura();
		dosificacion = dosificacionRepository
				.findActivaBySucursal(sucursalLogin);
	}

	public void consultar() {
		if (usuarioSession.getState().equals("SU")) {
			listFactura = facturaRepository.traerFacturasEntreFechasActivas(
					empresaLogin, sucursalLogin, fechaInicio, fechaFin);
		} else {
			listFactura = facturaRepository.traerFacturasEntreFechasActivas(
					nombreUsuario, empresaLogin, sucursalLogin, fechaInicio,
					fechaFin);
		}

		log.info("consultar : " + listFactura.size());
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

	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance()
				.getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void onRowSelect(SelectEvent event) {
		// seleccionado = true ;
		crear = false;
		seleccionarFactura = true;
	}

	public String getUrlCodeQR() {
		try {
			System.out.println("Ingreso a getUrlCodeQR...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			System.out.println("urlPath >> " + urlPath);

			urlCodeQR = urlPath + "codeQR?qrtext="
					+ this.selectedFactura.getCodigoRespuestaRapida();
			log.info("urlCodeQR: " + urlCodeQR);
			return urlCodeQR;
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error en getUrlCodeQR: " + e.getMessage());
			return null;
		}

	}

	public void armarUrl() {
		try {

			// TODO: handle exception
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";

			TamanoHoja tamanoHoja = tamanoHojaRepository.traerHojaActiva();
			String tamano = tamanoHoja.getTamano();

			String urlLogo = urlPath + "resources/gfx/"
					+ sucursalLogin.getPathLogo();

			formatoHoja = formatoHojaRepository.findActivosByEmpresa(
					empresaLogin, sucursalLogin).get(0);
			if (formatoHoja.getNombre().equals("COMPLETO")) {
				url = urlPath
						+ "ReportFactura?pIdFactura="
						+ selectedFactura.getId()
						+ "&pEmpresa="
						+ empresaLogin.getRazonSocial()
						+ "&pCiudad="
						+ empresaLogin.getCiudad()
						+ "&pPais=BOLIVIA&pLogo="
						+ urlLogo
						+ "&pNit="
						+ empresaLogin.getNit()
						+ "&pQr="
						+ selectedFactura.getCodigoRespuestaRapida()
						+ "&pLeyenda="
						+ URLEncoder.encode(dosificacion.getLeyendaInferior2(),
								"ISO-8859-1") + "&pInpresion="
						+ selectedFactura.isImpresion() + "&pTamano=" + tamano;
			}
			if (formatoHoja.getNombre().equals("SIN LOGO")) {
				url = urlPath
						+ "ReportFacturaSinCredFiscal?pIdFactura="
						+ selectedFactura.getId()
						+ "&pEmpresa="
						+ empresaLogin.getRazonSocial()
						+ "&pCiudad="
						+ empresaLogin.getCiudad()
						+ "&pPais=BOLIVIA&pLogo="
						+ urlLogo
						+ "&pNit="
						+ empresaLogin.getNit()
						+ "&pQr="
						+ selectedFactura.getCodigoRespuestaRapida()
						+ "&pLeyenda="
						+ URLEncoder.encode(dosificacion.getLeyendaInferior2(),
								"ISO-8859-1") + "&pInpresion="
						+ selectedFactura.isImpresion() + "&pTamano=" + tamano;
			}
			if (formatoHoja.getNombre().equals("SIN LOGO, SIN BORDE")) {
				url = urlPath
						+ "ReportFacturaSinCredFiscal?pIdFactura="
						+ selectedFactura.getId()
						+ "&pEmpresa="
						+ empresaLogin.getRazonSocial()
						+ "&pCiudad="
						+ empresaLogin.getCiudad()
						+ "&pPais=BOLIVIA&pLogo="
						+ urlLogo
						+ "&pNit="
						+ empresaLogin.getNit()
						+ "&pQr="
						+ selectedFactura.getCodigoRespuestaRapida()
						+ "&pLeyenda="
						+ URLEncoder.encode(dosificacion.getLeyendaInferior2(),
								"ISO-8859-1") + "&pInpresion="
						+ selectedFactura.isImpresion() + "&pTamano=" + tamano;
			}

			log.info("getURL() -> " + url);
		} catch (Exception e) {
		}
	}

	public void setUrlCodeQR(String urlCodeQR) {
		this.urlCodeQR = urlCodeQR;
	}

	public void procesar() {
		// listFactura = facturaRepository.findByNumero(numeroFactura);
	}

	// reporte

	public String loadURL() {
		try {
			/*
			 * $P{pMes} * $P{pGestion} $P{pTipoComprobante} * $P{pSucursal} *
			 * $P{pEmpresa}
			 */
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			String urlPDFreporte = urlPath + "ReporteFactura?pGestion="
					+ gestionLogin.getId() + "&pEmpresa="
					+ empresaLogin.getId() + "&pNumero=" + numeroFactura;
			urlFactura = urlPDFreporte;
			log.info("getURL() -> " + urlPDFreporte);
			return urlPDFreporte;
		} catch (Exception e) {
			log.info("getURL error: " + e.getMessage());
			return "error";
		}
	}

	public void actualizarFormReg() {
		log.info("actualizarFormReg");
		seleccionarFactura = false;
		crear = true;
		selectedFactura = new Factura();
		resetearFitrosTabla("formTableFactura:dataTableFactura");
	}

	public void actualizarForm2() {
		// armar URL
		seleccionado = false;
		urlFactura = loadURL();
		log.info("cargando......");
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgVistaPreviaFactura').show();");

		// resetearFitrosTabla("formTableFactura:dataTableFactura");
		// loadValuesDefaul();
	}

	public String armarCadenaQR(Factura factura) {
		String cadenaQR = "";
		try {
			cadenaQR = new String();

			// NIT emisor
			cadenaQR = cadenaQR.concat(empresaLogin.getNit());
			cadenaQR = cadenaQR.concat("|");

			// Numero de Factura
			cadenaQR = cadenaQR.concat(factura.getNumeroFactura());
			cadenaQR = cadenaQR.concat("|");

			// Numero de Autorizacion
			cadenaQR = cadenaQR.concat(factura.getNumeroAutorizacion());
			cadenaQR = cadenaQR.concat("|");

			// Fecha de Emision
			cadenaQR = cadenaQR.concat(obtenerFechaEmision(factura
					.getFechaFactura()));
			cadenaQR = cadenaQR.concat("|");

			// Total Bs
			cadenaQR = cadenaQR.concat(String.valueOf(factura
					.getTotalFacturado()));
			cadenaQR = cadenaQR.concat("|");

			// Importe Base para el Credito Fiscal
			cadenaQR = cadenaQR.concat(String.valueOf(factura
					.getTotalFacturado()));
			cadenaQR = cadenaQR.concat("|");

			// Codigo de Control
			cadenaQR = cadenaQR.concat(factura.getCodigoControl());
			cadenaQR = cadenaQR.concat("|");

			// NIT / CI del Comprador
			cadenaQR = cadenaQR.concat(factura.getNitCi());
			cadenaQR = cadenaQR.concat("|");

			// Importe ICE/IEHD/TASAS [cuando corresponda]
			cadenaQR = cadenaQR.concat("0");
			cadenaQR = cadenaQR.concat("|");

			// Importe por ventas no Gravadas o Gravadas a Tasa Cero [cuando
			// corresponda]
			cadenaQR = cadenaQR.concat("0");
			cadenaQR = cadenaQR.concat("|");

			// Importe no Sujeto a Credito Fiscal [cuando corresponda]
			cadenaQR = cadenaQR.concat("0");
			cadenaQR = cadenaQR.concat("|");

			// Descuentos Bonificaciones y Rebajas Obtenidas [cuando
			// corresponda]
			cadenaQR = cadenaQR.concat("0");

			return cadenaQR;

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error en armarCadenaQR: " + e.getMessage());
			return cadenaQR;
		}
	}

	private String obtenerFechaEmision(Date fechaEmision) {
		try {
			String DATE_FORMAT = "dd/MM/yyyy";
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.format(fechaEmision);

		} catch (Exception e) {
			log.error("Error en obtenerFechaEmision: " + e.getMessage());
			return "Error Fecha Emision";
		}
	}

	public void anularFactura() {
		try {
			log.info("Ingreso a anularFactura");

			selectedFactura.setEstado("A");
			selectedFactura.setImpresion(true);
			facturaRegistration.update(selectedFactura);
			/* facturaRegistration.anularFactura(selectedFactura); */
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Factura Anulada !!!! ", selectedFactura.getNumeroFactura());
			facesContext.addMessage(null, m);
			loadValuesDefaul();
		} catch (Exception e) {
			log.error("Error en anularFactura : " + e.getStackTrace());
		}
	}

	public void dialogClose() {
		System.out.println("Ingreso a dialogClose ");
		if (selectedFactura.isImpresion()
				&& !selectedFactura.getEstado().equals("A")) {
			selectedFactura.setImpresion(false);
			facturaRegistration.update(selectedFactura);
		}
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgFacturaVistaPrevia').hide();");
		loadValuesDefaul();

	}

	// ---- get and set ---

	public Usuario getUsuario() {
		return usuarioSession;
	}

	public void setUsuario(Usuario usuarioSession) {
		this.usuarioSession = usuarioSession;
	}

	public String[] getArrayMes() {
		return arrayMes;
	}

	public void setArrayMes(String[] arrayMes) {
		this.arrayMes = arrayMes;
	}

	public boolean isSeleccionado() {
		return seleccionado;
	}

	public void setSeleccionado(boolean seleccionado) {
		this.seleccionado = seleccionado;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getNombreMes() {
		return nombreMes;
	}

	public void setNombreMes(String nombreMes) {
		this.nombreMes = nombreMes;
	}

	public Factura getSelectedFactura() {
		return selectedFactura;
	}

	public void setSelectedFactura(Factura selectedFactura) {
		this.selectedFactura = selectedFactura;
	}

	public List<Factura> getListFactura() {
		return listFactura;
	}

	public void setListFactura(List<Factura> listFactura) {
		this.listFactura = listFactura;
	}

	public Integer getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(Integer numeroFactura) {
		this.numeroFactura = numeroFactura;
	}

	public String getUrlFactura() {
		return urlFactura;
	}

	public void setUrlFactura(String urlFactura) {
		this.urlFactura = urlFactura;
	}

	public boolean isCrear() {
		return crear;
	}

	public void setCrear(boolean crear) {
		this.crear = crear;
	}

	public boolean isSeleccionarFactura() {
		return seleccionarFactura;
	}

	public void setSeleccionarFactura(boolean seleccionarFactura) {
		this.seleccionarFactura = seleccionarFactura;
	}

	public List<Factura> getListFilterFactura() {
		return listFilterFactura;
	}

	public void setListFilterFactura(List<Factura> listFilterFactura) {
		this.listFilterFactura = listFilterFactura;
	}

	public Empresa getEmpresaLogin() {
		return empresaLogin;
	}

	public void setEmpresaLogin(Empresa empresaLogin) {
		this.empresaLogin = empresaLogin;
	}

	public Dosificacion getDosificacion() {
		return dosificacion;
	}

	public void setDosificacion(Dosificacion dosificacion) {
		this.dosificacion = dosificacion;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
