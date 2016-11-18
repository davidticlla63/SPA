package bo.com.erp360.webapp.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.FormatoEmpresaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.FormatoEmpresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.GrupoCentroCosto;
import bo.com.erp360.webapp.model.Moneda;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.TipoCambio;
import bo.com.erp360.webapp.model.TipoCambioUfv;
import bo.com.erp360.webapp.model.TipoComprobante;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.model.UsuarioEmpresa;
import bo.com.erp360.webapp.service.EmpresaRegistration;
import bo.com.erp360.webapp.service.FormatoEmpresaRegistration;
import bo.com.erp360.webapp.service.GestionRegistration;
import bo.com.erp360.webapp.service.MonedaEmpresaRegistration;
import bo.com.erp360.webapp.service.TipoCambioRegistration;
import bo.com.erp360.webapp.service.TipoCambioUfvRegistration;
import bo.com.erp360.webapp.service.TipoComprobanteRegistration;
import bo.com.erp360.webapp.service.UsuarioEmpresaRegistration;
import bo.com.erp360.webapp.util.FacesUtil;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "empresaController")
@ConversationScoped
public class EmpresaController implements Serializable {

	private static final long serialVersionUID = 5399619661135190257L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private StreamedContent content;

	@Inject
	private EmpresaRegistration empresaRegistration;

	@Inject
	private UsuarioEmpresaRegistration usuarioEmpresaRegistration;

	@Inject
	private TipoCambioRegistration tipoCambioRegistration;

	@Inject
	private TipoCambioUfvRegistration tipoCambioUfvRegistration;

	@Inject
	private MonedaEmpresaRegistration monedaEmpresaRegistration;

	@Inject
	private TipoComprobanteRegistration tipoComprobanteRegistration;

	@Inject
	private GestionRegistration gestionRegistration;

	@Inject
	private EmpresaRepository empresaRepository;

	@Inject
	private MonedaRepository monedaRepository;

	@Inject
	private GestionRepository gesionRepository;

	@Inject
	private FormatoEmpresaRepository formatoEmpresaRepository;

	@Inject
	private FormatoEmpresaRegistration formatoEmpresaRegistration;

	private Logger log = Logger.getLogger(this.getClass());

	private Usuario usuarioSession;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private boolean modificar = false;
	private boolean registrar = false;
	private boolean crear = true;
	private boolean seleccionadaFormEmpresa = true;
	private boolean seleccionadaFormGestion = false;

	private String tituloPanel = "Registrar Empresa";
	private String nombreEmpresa = "";
	private String nombreEstado = "ACTIVO";
	private String periodo = "enero-diciembre";
	private String formTitulo = "EMPRESA";
	private String nombreMonedaNacional;
	private String nombreMonedaExtranjera;
	private String simboloMonedaNacional;
	private String simboloMonedaExtranjera;
	private int year; // anio de la gestion actual

	// login
	private @Inject SessionMain sessionMain; // variable del login
	private String nombreUsuario;
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Produces
	@Named
	private Empresa newEmpresa;
	private Empresa selectedEmpresa;
	private Gestion selectedGestion;
	private Gestion newGestion;
	private MonedaEmpresa monedaEmpresa;
	private Moneda selectedMonedaNacional;
	private Moneda selectedMonedaExtranjera;
	private FormatoEmpresa formatoEmpresa;

	private String[] arrayPeriodo = { "enero-diciembre", "abril-marzo",
			"julio-junio", "octubre-septiembre" };
	private List<Usuario> listUsuario = new ArrayList<Usuario>();
	private List<Empresa> listaEmpresa;
	private List<Empresa> listaEmpresaActivas;
	private List<Empresa> listFilterEmpresa;
	private List<Gestion> listaGestion;
	private List<Moneda> listaMoneda;
	private List<Moneda> listaMonedaNacional;
	private List<Moneda> listaMonedaExtranjera;
	private String[] listEstado = { "ACTIVO", "INACTIVO" };

	@Produces
	@Named
	public List<Empresa> getListaEmpresa() {
		return listaEmpresa;
	}

	@Produces
	@Named
	public List<Empresa> getlistaEmpresaActivas() {
		return listaEmpresaActivas;
	}

	@PostConstruct
	public void initNewEmpresa() {

		log.info(" init new initNewEmpresa");
		beginConversation();
		usuarioSession = sessionMain.getUsuarioLoggin();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		listaMoneda = monedaRepository.findAll();

		Date fecha = new Date();
		year = Integer.parseInt(new SimpleDateFormat("yyyy").format(fecha));

		loadValuesDefault();
	}

	private void loadValuesDefault() {
		formatoEmpresa = new FormatoEmpresa();
		nombreMonedaNacional = listaMoneda.get(0).getNombre();
		selectedMonedaNacional = listaMoneda.get(0);
		simboloMonedaNacional = selectedMonedaNacional.getSimboloReferencial();
		nombreMonedaExtranjera = listaMoneda.get(1).getNombre();
		selectedMonedaExtranjera = listaMoneda.get(1);
		simboloMonedaExtranjera = selectedMonedaExtranjera
				.getSimboloReferencial();

		monedaEmpresa = new MonedaEmpresa();
		newGestion = new Gestion();
		newGestion.setGestion(year);
		newEmpresa = new Empresa();

		// tituloPanel
		tituloPanel = "Registrar Empresa";
		// traer todos las Empresa ordenados por ID Desc
		listaEmpresa = empresaRepository.findAllByUsuario(usuarioSession);
		listaEmpresaActivas = empresaRepository
				.findAllActivasByUsuario(usuarioSession);
		if (listaEmpresaActivas.isEmpty()) {
			seleccionadaFormEmpresa = false;
		}
		modificar = false;
	}

	private Moneda buscarMonedaByLocal(String moneda) {
		for (Moneda m : listaMoneda) {
			if (m.getNombre().equals(moneda)) {
				return m;
			}
		}
		return null;
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

	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance()
				.getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	private StreamedContent imageDefault(Empresa empresa) {
		String url = FacesContext.getCurrentInstance().getExternalContext()
				.getRealPath("/");
		System.out.println("url = " + url);
		String path = "";
		// if
		// (empresa.getSexo().toString().trim().equalsIgnoreCase("Masculino")) {
		path = url + "resources/paciente_default.png";
		// } else {
		// path = url + "resources/usuaria_default.png";
		// }
		try {
			content = new DefaultStreamedContent(new FileInputStream(path),
					"image/png");
			return content;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new DefaultStreamedContent();
		}
	}

	public void registrarEmpresa() {
		try {
			Date fechaActual = new Date();
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			newEmpresa
					.setRazonSocial(newEmpresa.getRazonSocial().toUpperCase());
			newEmpresa.setEstado(estado);
			newEmpresa.setUsuarioRegistro(usuarioSession.getLogin());
			newEmpresa.setFecha_registro(fechaActual);
			if (!newEmpresa.validate(facesContext, empresaLogin, gestionLogin)) {
				resetearFitrosTabla("formTableEmpresa:dataTableEmpresa");
				return;
			}

			byte[] image = (byte[]) FacesUtil
					.getSessionAttribute("imagePersonal");
			if (image != null) {
				newEmpresa.setLogo(image);
				newEmpresa.setPesoLogo(image.length);
			} else {
				StreamedContent streamedContent = imageDefault(newEmpresa);
				InputStream inputStream = streamedContent.getStream();
				byte[] bs = IOUtils.toByteArray(inputStream);
				newEmpresa.setLogo(bs);
				newEmpresa.setPesoLogo(bs.length);
			}

			Empresa empresa = empresaRegistration.create(newEmpresa);

			// UsuarioEmpresa
			UsuarioEmpresa ue = new UsuarioEmpresa();
			ue.setEmpresa(empresa);
			ue.setUsuario(usuarioSession);
			usuarioEmpresaRegistration.create(ue);

			// Gestion
			newGestion.setPeriodo(periodo);
			newGestion.setEmpresa(empresa);
			gestionRegistration.create(newGestion);

			// Moneda nacional
			MonedaEmpresa monedaEmpresaNacional = new MonedaEmpresa();
			monedaEmpresaNacional.setEmpresa(empresa);
			monedaEmpresaNacional.setMoneda(selectedMonedaNacional);
			monedaEmpresaNacional.setSimbolo(simboloMonedaNacional);
			monedaEmpresaNacional.setTipo("NACIONAL");
			monedaEmpresaNacional.setEstado("AC");
			monedaEmpresaRegistration.create(monedaEmpresaNacional);

			// Moneda extranjera
			MonedaEmpresa monedaEmpresaExtranjera = new MonedaEmpresa();
			monedaEmpresaExtranjera.setEmpresa(empresa);
			monedaEmpresaExtranjera.setMoneda(selectedMonedaExtranjera);
			monedaEmpresaExtranjera.setSimbolo(simboloMonedaExtranjera);
			monedaEmpresaExtranjera.setTipo("EXTRANJERA");
			monedaEmpresaExtranjera.setEstado("AC");
			monedaEmpresaRegistration.create(monedaEmpresaExtranjera);

			// no se agrega plan de cuenta
			// empresaRegistration.cargarPlanCuentaDesdeArchivo(empresa,
			// usuarioSession,monedaEmpresaNacional,monedaEmpresaExtranjera,5);

			// tipo de cambio
			cargarTipoCambio(empresa, fechaActual);

			// tipo de cambio ufv
			cargarTipoCambioUfv(empresa, fechaActual);

			cargarTipoComprobante(empresa);

			// registro de formato
			formatoEmpresa.setEmpresa(empresa);
			formatoEmpresa.setLogo(null);
			formatoEmpresa.setPesoFoto(0);
			formatoEmpresa.setEstado("AC");
			formatoEmpresa.setUsuarioRegistro(nombreUsuario);
			formatoEmpresa.setFechaRegistro(fechaActual);

			formatoEmpresaRegistration.create(formatoEmpresa);

			FacesUtil.infoMessage("Empresa Registrada!",
					"Empresa " + empresa.getRazonSocial());
			crear = false;
			registrar = true;
			modificar = false;
			resetearFitrosTabla("formTableEmpresa:dataTableEmpresa");
			loadValuesDefault();
		} catch (Exception e) {
			FacesUtil.errorMessage("Error al registrar");
		}
	}

	private void cargarTipoCambio(Empresa empresaUx, Date fechaActual) {
		TipoCambio tc = new TipoCambio();
		tc.setUnidad(6.92);
		tc.setFecha(fechaActual);
		tc.setFechaLiteral(obtenerLiteralFecha(fechaActual));
		tc.setEstado("AC");
		tc.setEmpresa(empresaUx);
		tipoCambioRegistration.create(tc);
	}

	private String obtenerLiteralFecha(Date fechaActual) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fechaActual);
		String year = new SimpleDateFormat("yyyy").format(new Date());
		Integer month = Integer.parseInt(new SimpleDateFormat("MM").format(
				new Date()).toString());
		String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
		return year + "-" + month + "-" + day;
	}

	private void cargarTipoCambioUfv(Empresa empresaUx, Date fechaActual) {
		TipoCambioUfv tcUfv = new TipoCambioUfv();
		tcUfv.setUnidad(2.06795);
		tcUfv.setFecha(fechaActual);
		tcUfv.setFechaLiteral(obtenerLiteralFecha(fechaActual));
		tcUfv.setEstado("AC");
		tcUfv.setEmpresa(empresaUx);
		tipoCambioUfvRegistration.create(tcUfv);
	}

	private void cargarTipoComprobante(Empresa empresa) {
		// tipo de comprobante
		TipoComprobante tipoComp = new TipoComprobante();
		tipoComp.setEstado("AC");
		tipoComp.setFechaRegistro(new Date());
		tipoComp.setUsuarioRegistro(nombreUsuario);
		tipoComp.setNombre("INGRESO");
		tipoComp.setEmpresa(empresa);
		tipoComprobanteRegistration.registrarTipoComprobanteEmpresa(tipoComp);
		tipoComp = new TipoComprobante();
		tipoComp.setEstado("AC");
		tipoComp.setFechaRegistro(new Date());
		tipoComp.setUsuarioRegistro(nombreUsuario);
		tipoComp.setEmpresa(empresa);
		tipoComp.setNombre("EGRESO");
		tipoComprobanteRegistration.registrarTipoComprobanteEmpresa(tipoComp);
		tipoComp = new TipoComprobante();
		tipoComp.setEstado("AC");
		tipoComp.setFechaRegistro(new Date());
		tipoComp.setUsuarioRegistro(nombreUsuario);
		tipoComp.setEmpresa(empresa);
		tipoComp.setNombre("TRASPASO");
		tipoComprobanteRegistration.registrarTipoComprobanteEmpresa(tipoComp);
		tipoComp = new TipoComprobante();
		tipoComp.setEstado("AC");
		tipoComp.setFechaRegistro(new Date());
		tipoComp.setUsuarioRegistro(nombreUsuario);
		tipoComp.setEmpresa(empresa);
		tipoComp.setNombre("AJUSTE");
		tipoComprobanteRegistration.registrarTipoComprobanteEmpresa(tipoComp);
	}

	public void registrarGestion() {
		try {
			newGestion.setPeriodo(periodo);
			newGestion.setEmpresa(selectedEmpresa);
			gestionRegistration.create(newGestion);
			listaGestion = gesionRepository.findAllByEmpresa(selectedEmpresa);
			seleccionadaFormEmpresa = false;
			formTitulo = "GESTIÓN - "
					+ selectedEmpresa.getRazonSocial().toUpperCase();
		} catch (Exception e) {
			log.info("registrarGestion() -> error :" + e.getMessage());
		}
	}

	public void modificarEmpresa() {
		try {
			Date fechaActual = new Date();
			newEmpresa.setEstado(nombreEstado.equals("ACTIVO") ? "AC" : "IN");
			newEmpresa.setFecha_registro(fechaActual);
			byte[] image = (byte[]) FacesUtil
					.getSessionAttribute("imagePersonal");
			if (image != null) {
				newEmpresa.setLogo(image);
				newEmpresa.setPesoLogo(image.length);

			} else
				newEmpresa.setPesoLogo(0);
			FacesUtil.removeSessionAttribute("imagePersonal");

			empresaRegistration.update(newEmpresa);

			// modificacion de Fomrato empresa
			if (formatoEmpresa.getId() == 0) {
				// registro de formato
				formatoEmpresa.setEmpresa(newEmpresa);
				formatoEmpresa.setLogo(null);
				formatoEmpresa.setPesoFoto(0);
				formatoEmpresa.setEstado("AC");
				formatoEmpresa.setUsuarioRegistro(nombreUsuario);
				formatoEmpresa.setFechaRegistro(fechaActual);
				formatoEmpresaRegistration.create(formatoEmpresa);
			} else {
				formatoEmpresa.setEstado(nombreEstado.equals("ACTIVO") ? "AC"
						: "IN");
				formatoEmpresa.setFechaModificacion(fechaActual);
				formatoEmpresaRegistration.update(formatoEmpresa);
			}
			FacesUtil.infoMessage("Empresa Modificada",
					"Empresa " + newEmpresa.getRazonSocial());
			crear = false;
			registrar = true;
			modificar = false;
			resetearFitrosTabla("formTableEmpresa:dataTableEmpresa");
			loadValuesDefault();
			FacesUtil.removeSessionAttribute("imagePersonal");
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Modificado Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminarEmpresa() {
		try {
			if (newEmpresa.getRazonSocial().equals(empresaLogin)) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"No se puede Eliminar", "Empresa logeada actualmente");
				facesContext.addMessage(null, m);
				return;
			}
			newEmpresa.setEstado("RM");
			empresaRegistration.update(newEmpresa);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Empresa Borrada!", newEmpresa.getRazonSocial());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			resetearFitrosTabla("formTableEmpresa:dataTableEmpresa");
			loadValuesDefault();
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Borrado Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	private String getRootErrorMessage(Exception e) {
		String errorMessage = "Registration failed. See server log for more information";
		if (e == null) {
			return errorMessage;
		}
		Throwable t = e;
		while (t != null) {
			errorMessage = t.getLocalizedMessage();
			t = t.getCause();
		}
		return errorMessage;
	}

	public void onRowSelectEmpresa(SelectEvent event) {
		newEmpresa = selectedEmpresa;
		listaGestion = gesionRepository.findAllByEmpresa(selectedEmpresa);
		nombreEstado = newEmpresa.getEstado().equals("AC") ? "ACTIVO"
				: "INACTIVO";
		formatoEmpresa = formatoEmpresaRepository
				.findByEmpresa(selectedEmpresa);
		modificar = true;
		crear = false;
		registrar = false;

		// moneda
		selectedMonedaNacional = monedaRepository.findMonedaByEmpresaAndTipo(
				newEmpresa, "NACIONAL");
		nombreMonedaNacional = selectedMonedaNacional.getNombre();
		simboloMonedaNacional = selectedMonedaNacional.getSimboloReferencial();

		selectedMonedaExtranjera = monedaRepository.findMonedaByEmpresaAndTipo(
				newEmpresa, "EXTRANJERA");
		nombreMonedaExtranjera = selectedMonedaExtranjera.getNombre();
		simboloMonedaExtranjera = selectedMonedaExtranjera
				.getSimboloReferencial();

		resetearFitrosTabla("formTableEmpresa:dataTableEmpresa");
	}

	// para pagina index.xhtml
	public void onRowSelectEmpresa2(SelectEvent event) {
		newEmpresa = selectedEmpresa;
	}

	public void onRowSelectGestion(SelectEvent event) {
		this.selectedGestion = (Gestion) event.getObject();
		// cargar siguiente pagina
		try {
			HttpSession session = (HttpSession) FacesContext
					.getCurrentInstance().getExternalContext()
					.getSession(false);
			session.setAttribute("empresa", selectedEmpresa.getRazonSocial());
			session.setAttribute("gestion", selectedGestion.getGestion());

			FacesContext.getCurrentInstance().getExternalContext()
					.redirect("/webapp/pages/dashboard.xhtml");
		} catch (Exception e) {
		}
	}

	public void actualizarForm() {
		crear = true;
		modificar = false;
		registrar = false;
		newEmpresa = new Empresa();
		resetearFitrosTabla("formTableEmpresa:dataTableEmpresa");
		selectedEmpresa = new Empresa();
	}

	public void onRowUnSelect(UnselectEvent event) {
		FacesMessage msg = new FacesMessage("Grupo Centro Costo Selected",
				((GrupoCentroCosto) event.getObject()).getNombre());
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public void crearEmpresa() {
		crear = false;
		modificar = false;
		registrar = true;
	}

	// //button form index.xhtml
	public void formButtonAtras() {
		seleccionadaFormEmpresa = true;
		seleccionadaFormGestion = false;
		formTitulo = "EMPRESA";
		selectedEmpresa = new Empresa();
	}

	public String urlServletLogoEmpresa() {
		String url = FacesUtil.getUrlPath()
				+ "ServletLogoEmpresa?idFormatoEmpresa="
				+ formatoEmpresa.getId();
		log.info("url = " + url);
		return url;
	}

	// ---------------- get and set ----------------------
	public String getTituloPanel() {
		return tituloPanel;
	}

	public void setTituloPanel(String tituloPanel) {
		this.tituloPanel = tituloPanel;
	}

	public boolean isModificar() {
		return modificar;
	}

	public void setModificar(boolean modificar) {
		this.modificar = modificar;
	}

	public Empresa getSelectedEmpresa() {
		return selectedEmpresa;
	}

	public void setSelectedEmpresa(Empresa selectedEmpresa) {
		seleccionadaFormEmpresa = false;
		seleccionadaFormGestion = true;
		formTitulo = "GESTIÓN - "
				+ selectedEmpresa.getRazonSocial().toUpperCase();
		this.selectedEmpresa = selectedEmpresa;
		listaGestion = gesionRepository.findAllByEmpresa(selectedEmpresa);
	}

	public List<Usuario> getListUsuario() {
		return listUsuario;
	}

	public void setListUsuario(List<Usuario> listUsuario) {
		this.listUsuario = listUsuario;
	}

	public List<Gestion> getListaGestion() {
		return listaGestion;
	}

	public void setListaGestion(List<Gestion> listaGestion) {
		this.listaGestion = listaGestion;
	}

	public Gestion getSelectedGestion() {
		return selectedGestion;
	}

	public void setSelectedGestion(Gestion selectedGestion) {
		this.selectedGestion = selectedGestion;
	}

	public String getNombreEmpresa() {
		return nombreEmpresa;
	}

	public void setNombreEmpresa(String nombreEmpresa) {
		this.nombreEmpresa = nombreEmpresa;
	}

	public Gestion getNewGestion() {
		return newGestion;
	}

	public void setNewGestion(Gestion newGestion) {
		this.newGestion = newGestion;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public String[] getArrayPeriodo() {
		return arrayPeriodo;
	}

	public void setArrayPeriodo(String[] arrayPeriodo) {
		this.arrayPeriodo = arrayPeriodo;
	}

	public List<Empresa> getListFilterEmpresa() {
		return listFilterEmpresa;
	}

	public void setListFilterEmpresa(List<Empresa> listFilterEmpresa) {
		this.listFilterEmpresa = listFilterEmpresa;
	}

	public String getNombreEstado() {
		return nombreEstado;
	}

	public void setNombreEstado(String nombreEstado) {
		this.nombreEstado = nombreEstado;
	}

	public String[] getListEstado() {
		return listEstado;
	}

	public void setListEstado(String[] listEstado) {
		this.listEstado = listEstado;
	}

	public boolean isSeleccionadaFormEmpresa() {
		return seleccionadaFormEmpresa;
	}

	public void setSeleccionadaFormEmpresa(boolean seleccionadaFormEmpresa) {
		this.seleccionadaFormEmpresa = seleccionadaFormEmpresa;
	}

	public boolean isSeleccionadaFormGestion() {
		return seleccionadaFormGestion;
	}

	public void setSeleccionadaFormGestion(boolean seleccionadaFormGestion) {
		this.seleccionadaFormGestion = seleccionadaFormGestion;
	}

	//
	// public boolean isSeleccionadaFormAgregarEmpresa() {
	// return seleccionadaFormAgregarEmpresa;
	// }
	//
	// public void setSeleccionadaFormAgregarEmpresa(
	// boolean seleccionadaFormAgregarEmpresa) {
	// this.seleccionadaFormAgregarEmpresa = seleccionadaFormAgregarEmpresa;
	// }

	public String getFormTitulo() {
		return formTitulo;
	}

	public void setFormTitulo(String formTitulo) {
		this.formTitulo = formTitulo;
	}

	// public boolean isSeleccionadaFormAgregarGestion() {
	// return seleccionadaFormAgregarGestion;
	// }
	//
	// public void setSeleccionadaFormAgregarGestion(
	// boolean seleccionadaFormAgregarGestion) {
	// this.seleccionadaFormAgregarGestion = seleccionadaFormAgregarGestion;
	// }

	public boolean isCrear() {
		return crear;
	}

	public void setCrear(boolean crear) {
		this.crear = crear;
	}

	public boolean isRegistrar() {
		return registrar;
	}

	public void setRegistrar(boolean registrar) {
		this.registrar = registrar;
	}

	public List<Moneda> getListaMoneda() {
		return listaMoneda;
	}

	public void setListaMoneda(List<Moneda> listaMoneda) {
		this.listaMoneda = listaMoneda;
	}

	public MonedaEmpresa getMonedaEmpresa() {
		return monedaEmpresa;
	}

	public void setMonedaEmpresa(MonedaEmpresa monedaEmpresa) {
		this.monedaEmpresa = monedaEmpresa;
	}

	public String getNombreMonedaNacional() {
		return nombreMonedaNacional;
	}

	public void setNombreMonedaNacional(String nombreMonedaNacional) {
		this.nombreMonedaNacional = nombreMonedaNacional;
		selectedMonedaNacional = buscarMonedaByLocal(nombreMonedaNacional);
		simboloMonedaNacional = selectedMonedaNacional.getSimboloReferencial();
		// cargarMonedas(nombreMonedaNacional, "NACIONAL");
	}

	public String getNombreMonedaExtranjera() {
		return nombreMonedaExtranjera;
	}

	public void setNombreMonedaExtranjera(String nombreMonedaExtranjera) {
		this.nombreMonedaExtranjera = nombreMonedaExtranjera;
		selectedMonedaExtranjera = buscarMonedaByLocal(nombreMonedaExtranjera);
		simboloMonedaExtranjera = selectedMonedaExtranjera
				.getSimboloReferencial();
		// cargarMonedas(nombreMonedaExtranjera, "EXTRANJERA");
	}

	public Moneda getSelectedMonedaNacional() {
		return selectedMonedaNacional;
	}

	public void setSelectedMonedaNacional(Moneda selectedMonedaNacional) {
		this.selectedMonedaNacional = selectedMonedaNacional;
	}

	public Moneda getSelectedMonedaExtranjera() {
		return selectedMonedaExtranjera;
	}

	public void setSelectedMonedaExtranjera(Moneda selectedMonedaExtranjera) {
		this.selectedMonedaExtranjera = selectedMonedaExtranjera;
	}

	public String getSimboloMonedaNacional() {
		return simboloMonedaNacional;
	}

	public void setSimboloMonedaNacional(String simboloMonedaNacional) {
		this.simboloMonedaNacional = simboloMonedaNacional;
	}

	public String getSimboloMonedaExtranjera() {
		return simboloMonedaExtranjera;
	}

	public void setSimboloMonedaExtranjera(String simboloMonedaExtranjera) {
		this.simboloMonedaExtranjera = simboloMonedaExtranjera;
	}

	public List<Moneda> getListaMonedaNacional() {
		return listaMonedaNacional;
	}

	public void setListaMonedaNacional(List<Moneda> listaMonedaNacional) {
		this.listaMonedaNacional = listaMonedaNacional;
	}

	public List<Moneda> getListaMonedaExtranjera() {
		return listaMonedaExtranjera;
	}

	public void setListaMonedaExtranjera(List<Moneda> listaMonedaExtranjera) {
		this.listaMonedaExtranjera = listaMonedaExtranjera;
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

	public FormatoEmpresa getFormatoEmpresa() {
		return formatoEmpresa;
	}

	public void setFormatoEmpresa(FormatoEmpresa formatoEmpresa) {
		this.formatoEmpresa = formatoEmpresa;
	}

	public StreamedContent getContent() {
		return content;
	}

	public void setContent(StreamedContent content) {
		this.content = content;
	}
}
