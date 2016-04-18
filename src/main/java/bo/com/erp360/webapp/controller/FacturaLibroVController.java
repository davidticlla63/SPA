package bo.com.erp360.webapp.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.primefaces.event.SelectEvent;
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
import bo.com.erp360.webapp.service.FacturaRegistration;

@Named(value = "facturaLibroVController")
@ConversationScoped
public class FacturaLibroVController implements Serializable {



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
	FacturaRegistration facturaRegistration;

	@Inject
	UsuarioRepository usuarioRepository;
	private Usuario usuarioSession;

	// login
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private String nombreUsuario;
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Inject
	private EmpresaRepository empresaRepository;

	@Inject
	private GestionRepository gestionRepository;

	private String urlCodeQR;

	// Vista Previa Factura
	private String urlFactura;

	private Date fechaInicial = getPrimerDiaDelMes();
	private Date fechaFinal = getUltimoDiaDelMes();
	private String estadoFactura = "%";
	private int sucursalID;
	private List<Sucursal> listaSucursales = new ArrayList<Sucursal>();

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private boolean modificar = false;
	private String tituloPanel = "Modificar Factura";
	private Factura selectedFactura;
	private Factura newFactura;

	private List<Factura> listaFacturasEmitidas = new ArrayList<Factura>();

	// reporte factura
	private StreamedContent streamedContent;

	private Sucursal sucursalLogin;


	@Produces
	@Named
	public List<Factura> getListaFacturasEmitidas() {
		return listaFacturasEmitidas;
	}

	@PostConstruct
	public void initNewFactura() {

		// initConversation();
		beginConversation();

		HttpServletRequest request = (HttpServletRequest) facesContext
				.getExternalContext().getRequest();
		System.out.println("init NewProforma*********************************");
		System.out.println("request.getClass().getName():"
				+ request.getClass().getName());
		System.out.println("isVentas:" + request.isUserInRole("ventas"));
		System.out.println("remoteUser:" + request.getRemoteUser());
		System.out.println("userPrincipalName:"
				+ (request.getUserPrincipal() == null ? "null" : request
						.getUserPrincipal().getName()));

		usuarioSession = usuarioRepository.findByLogin(request
				.getUserPrincipal().getName());

		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		setEmpresaLogin(estadoUsuarioLogin.getEmpresaSession(empresaRepository));
		setGestionLogin(estadoUsuarioLogin.getGestionSession(empresaRepository,
				gestionRepository));
		
		sucursalLogin = estadoUsuarioLogin.getSucursalSession(empresaRepository, sucursalRepository);

		System.out.println("Sucursal Usuario: "
				+ sucursalLogin.getNombre());

		// cargar lista sucursales
		listaSucursales.clear();
		listaSucursales = sucursalRepository.traerSucursalesFacturas();

		// actualizar lista facturas
		listaFacturasEmitidas.clear();
		listaFacturasEmitidas = facturaRepository.traerFacturasEntreFechasActivas2(nombreUsuario,
				empresaLogin,this.getFechaInicial(),this.getFechaFinal());

		// Formulario Proforma
		newFactura = new Factura();
		modificar = false;
		tituloPanel = "Modificar Factura";

	}

	public void buscarFacturas() {
		System.out.println("Ingreos a buscar ventas....");
		System.out.println("Estado: " + this.getEstadoFactura());
		System.out.println("Sucursal ID: " + this.getSucursalID());
		System.out.println("Fecha Inicial: " + this.getFechaInicial());
		System.out.println("Fecha Final: " + this.getFechaFinal());
		listaFacturasEmitidas.clear();

		try {
			listaFacturasEmitidas = facturaRepository.buscarFacturasSucursal(
					this.getFechaInicial(), this.getFechaFinal(),
					this.getEstadoFactura(), this.getSucursalID());
			System.out.println("Cantidad Facturas Encontradas: "
					+ listaFacturasEmitidas.size());
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error en buscarFacturas: " + e.getMessage());
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

	// SELECT FACTURA CLICK
	public void onRowSelectFacturaClick(SelectEvent event) {
		try {
			Factura factura = (Factura) event.getObject();
			System.out.println("onRowSelectFacturaClick  " + factura.getId());
			selectedFactura = factura;
			newFactura = em.find(Factura.class, factura.getId());
			newFactura.setFechaRegistro(new Date());
			newFactura.setUsuarioRegistro(usuarioSession.getLogin());

			tituloPanel = "Modificar Factura";
			modificar = true;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("Error in onRowSelectFacturaClick: "
					+ e.getMessage());
		}
	}

	public void beginConversation() {

		if (conversation.isTransient()) {
			System.out.println("beginning conversation : " + this.conversation);
			conversation.begin();
			System.out.println("---> Init Conversation");
		}
	}

	public void endConversation() {
		if (!conversation.isTransient()) {
			conversation.end();
		}
	}

	// SELECT FACTURA DBL CLIC
	public void onRowSelectFacturaDblClic(SelectEvent event) {
		try {
			Factura factura = (Factura) event.getObject();
			System.out.println("onRowSelectFacturaDblClic  " + factura.getId());
			this.setSelectedFactura(factura);

			armarURLFactura();

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error in onRowSelectFacturaDblClic: "
					+ e.getMessage());
		}
	}

	// SELECT FACTURA CLIC
	public void onRowSelectFacturaClic(SelectEvent event) {
		try {
			Factura factura = (Factura) event.getObject();
			System.out.println("onRowSelectFacturaClic  " + factura.getId());
			this.setSelectedFactura(factura);

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error in onRowSelectFacturaClic: "
					+ e.getMessage());
		}
	}

	public void modificarFactura() {
		try {
			System.out.println("Ingreso a modificarFactura: "
					+ selectedFactura.getId());

			if (selectedFactura.getEstado().equals("A")) {
				facturaRegistration.anularFactura(selectedFactura);
				// actualizar lista facturas
				listaFacturasEmitidas.clear();
				listaFacturasEmitidas = facturaRepository
						.findAllOrderedByFechaRegistro();

				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Factura Anulada!", "Numero: "
								+ selectedFactura.getNumeroFactura());
				facesContext.addMessage(null, m);

			} else {
				facturaRegistration.update(selectedFactura);
				// actualizar lista facturas
				listaFacturasEmitidas.clear();
				listaFacturasEmitidas = facturaRepository
						.findAllOrderedByFechaRegistro();

				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Factura Modificada!", "Numero: "
								+ selectedFactura.getNumeroFactura());
				facesContext.addMessage(null, m);
				pushEventSucursal.fire(String.format(
						"Factura Modificada: %s (id: %d)",
						selectedFactura.getNombreFactura(),
						selectedFactura.getId()));
			}

			initNewFactura();

		} catch (Exception e) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "Error al Modificar Factura.");
			facesContext.addMessage(null, m);
		}
	}

	public Sucursal getSucursal(int sucursalId) {
		try {
			return em.find(Sucursal.class, sucursalId);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error en getSucursal: " + e.getMessage());
			return null;
		}
	}

	public Sucursal getSucursalSelected() {
		try {
			return em.find(Sucursal.class, this.getSelectedFactura()
					.getSucursal().getId());
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error en getSucursalSelected: "
					+ e.getMessage());
			return null;
		}
	}

	// get and set
	public Factura getNewFactura() {
		return newFactura;
	}

	public void setNewProforma(Factura newFactura) {
		this.newFactura = newFactura;
	}

	public String getTituloPanel() {
		return tituloPanel;
	}

	public void setTituloPanel(String tituloPanel) {
		this.tituloPanel = tituloPanel;
	}

	public Factura getSelectedFactura() {
		return selectedFactura;
	}

	public void setSelectedFactura(Factura selectedFactura) {
		this.selectedFactura = selectedFactura;
	}

	public boolean isModificar() {
		return modificar;
	}

	public void setModificar(boolean modificar) {
		this.modificar = modificar;
	}

	public StreamedContent getStreamedContent() {
		try {
			System.out.println("Ingreso a descargarPDF Factura...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			System.out.println("urlPath >> " + urlPath);

			String urlPDFreporte = urlPath + "ReporteFactura?pFactura="
					+ newFactura.getId();
			System.out.println("URL Reporte PDF: " + urlPDFreporte);

			URL url = new URL(urlPDFreporte);

			// Read the PDF from the URL and save to a local file
			InputStream is1 = url.openStream();
			File f = stream2file(is1);
			System.out.println("Size Bytes: " + f.length());
			InputStream stream = new FileInputStream(f);
			streamedContent = new DefaultStreamedContent(stream,
					"application/pdf", "Factura" + newFactura.getId() + ".pdf");
			return streamedContent;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("Error en descargarPDF Factura: "
					+ e.getMessage());
			return null;
		}
	}

	public Date getFechaInicial() {
		return fechaInicial;
	}

	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}

	public Date getFechaFinal() {
		return fechaFinal;
	}

	public void setFechaFinal(Date fechaFinal) {
		this.fechaFinal = fechaFinal;
	}

	private static File stream2file(InputStream in) throws IOException {

		final File tempFile = File.createTempFile("Factura", ".pdf");
		tempFile.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(tempFile)) {
			IOUtils.copy(in, out);
		}

		return tempFile;
	}

	public void setStreamedContent(StreamedContent streamedContent) {
		this.streamedContent = streamedContent;
	}

	public String getEstadoFactura() {
		return estadoFactura;
	}

	public void setEstadoFactura(String estadoFactura) {
		this.estadoFactura = estadoFactura;
	}

	public int getSucursalID() {
		return sucursalID;
	}

	public void setSucursalID(int sucursalID) {
		this.sucursalID = sucursalID;
	}

	public List<Sucursal> getListaSucursales() {
		return listaSucursales;
	}

	public void setListaSucursales(List<Sucursal> listaSucursales) {
		this.listaSucursales = listaSucursales;
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
					+ this.getSelectedFactura().getCodigoRespuestaRapida();
			return urlCodeQR;
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error en getUrlCodeQR: " + e.getMessage());
			return null;
		}
	}

	public void armarURLFactura() {
		try {
			System.out.println("Ingreso a armarURLVentasSFV...");
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			System.out.println("urlPath >> " + urlPath);

			urlFactura = urlPath + "factura2?idFactura="
					+ this.getSelectedFactura().getId();
			System.out.println("URL Reporte Factura: " + urlFactura);

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error en armarURLFactura: " + e.getMessage());
		}
	}

	public void setUrlCodeQR(String urlCodeQR) {
		this.urlCodeQR = urlCodeQR;
	}

	public String getUrlFactura() {
		return urlFactura;
	}

	public void setUrlFactura(String urlFactura) {
		this.urlFactura = urlFactura;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public Empresa getEmpresaLogin() {
		return empresaLogin;
	}

	public void setEmpresaLogin(Empresa empresaLogin) {
		this.empresaLogin = empresaLogin;
	}

	public Gestion getGestionLogin() {
		return gestionLogin;
	}

	public void setGestionLogin(Gestion gestionLogin) {
		this.gestionLogin = gestionLogin;
	}

	public Sucursal getSucursalLogin() {
		return sucursalLogin;
	}

	public void setSucursalLogin(Sucursal sucursalLogin) {
		this.sucursalLogin = sucursalLogin;
	}
}
