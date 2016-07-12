package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.primefaces.component.api.UIData;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.FormatoFacturaRepository;
import bo.com.erp360.webapp.data.FormatoHojaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.FormatoFactura;
import bo.com.erp360.webapp.model.FormatoHoja;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.GrupoCentroCosto;
import bo.com.erp360.webapp.model.Moneda;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.Nivel;
import bo.com.erp360.webapp.model.ParametroEmpresa;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TipoCambio;
import bo.com.erp360.webapp.model.TipoCambioUfv;
import bo.com.erp360.webapp.model.TipoComprobante;
import bo.com.erp360.webapp.model.TipoCuenta;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.model.UsuarioEmpresa;
import bo.com.erp360.webapp.service.EDNivel;
import bo.com.erp360.webapp.service.EmpresaRegistration;
import bo.com.erp360.webapp.service.FormatoFacturaRegistration;
import bo.com.erp360.webapp.service.FormatoHojaRegistration;
import bo.com.erp360.webapp.service.GestionRegistration;
import bo.com.erp360.webapp.service.MonedaEmpresaRegistration;
import bo.com.erp360.webapp.service.NivelRegistration;
import bo.com.erp360.webapp.service.ParametroEmpresaRegistration;
import bo.com.erp360.webapp.service.SucursalRegistration;
import bo.com.erp360.webapp.service.TipoCambioRegistration;
import bo.com.erp360.webapp.service.TipoCambioUfvRegistration;
import bo.com.erp360.webapp.service.TipoComprobanteRegistration;
import bo.com.erp360.webapp.service.TipoCuentaRegistration;
import bo.com.erp360.webapp.service.UsuarioEmpresaRegistration;
import bo.com.erp360.webapp.util.EDPlanCuenta;
import bo.com.erp360.webapp.util.FacesUtil;
import bo.com.erp360.webapp.util.SessionMain;
import bo.com.erp360.webapp.util.Time;

@Named(value = "creacionController")
@ConversationScoped
public class CreacionController implements Serializable {

	private static final long serialVersionUID = 310306444101578622L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	// formato de hoja
	private @Inject FormatoHojaRegistration formatoHojaRegistration;

	private @Inject FormatoHojaRepository formatoHojaRepository;

	private List<FormatoHoja> listFormatoHoja = new ArrayList<FormatoHoja>();
	// formato de Factura
	private @Inject FormatoFacturaRegistration formatoFacturaRegistration;

	private @Inject FormatoFacturaRepository formatoFacturaRepository;

	private List<FormatoFactura> listFormatoFactura = new ArrayList<FormatoFactura>();

	@Inject
	private EmpresaRegistration empresaRegistration;

	@Inject
	private UsuarioEmpresaRegistration usuarioEmpresaRegistration;

	@Inject
	private SucursalRegistration sucursalRegistration;

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
	private ParametroEmpresaRegistration parametroEmpresaRegistration;

	@Inject
	private NivelRegistration nivelRegistration;

	@Inject
	private TipoCuentaRegistration tipoCuentaRegistration;

	@Inject
	private EmpresaRepository empresaRepository;

	@Inject
	private MonedaRepository monedaRepository;

	@Inject
	private GestionRepository gesionRepository;

	private Logger log = Logger.getLogger(this.getClass());

	private Usuario usuarioSession;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private boolean modificar = false;
	private boolean registrar = false;
	private boolean crear = true;
	private boolean seleccionada1 = false;
	private boolean seleccionadaFormEmpresa = true;
	private boolean seleccionadaFormGestion = false;
	private boolean seleccionadaFormAgregarEmpresa = true;
	private boolean seleccionarForm1 = true;
	private boolean buttonCancelar = true;

	private String tituloPanel = "Registrar Empresa";
	private String nombreEmpresa = "";
	private String nombreEstado = "ACTIVO";
	private String periodo = "enero-diciembre";
	private String formTitulo = "EMPRESA";
	private String nombreMonedaNacional;
	private String nombreMonedaExtranjera;
	private String simboloMonedaNacional;
	private String simboloMonedaExtranjera;
	private int nivel;
	private int tamanio = 1;
	private String codigo;
	private String periodoActual = "enero";
	private double tipoCambio = 6.91;
	private double tipoCambioUfv = 2.02;
	private String tipoPlanCuenta = "personalizado";
	private int nivelAnterior = 0;

	private String tabEmpresa = "active";
	private String tabGestion = "";
	private String tabPlanCuenta = "";
	private String tabParametros = "";
	private int numeroTab;
	private boolean buttonAnterior;
	private boolean buttonSiguiente;

	// login
	private @Inject SessionMain sessionMain; // variable del login
	private String nombreUsuario;
	private Empresa empresaLogin;
	private Gestion gestionLogin;
	private Sucursal sucursalLogin;

	private Empresa newEmpresa;
	private Empresa selectedEmpresa;
	private Gestion selectedGestion;
	private Gestion newGestion;
	private MonedaEmpresa monedaEmpresa;
	private Moneda selectedMonedaNacional;
	private Moneda selectedMonedaExtranjera;
	private ParametroEmpresa parametroEmpresa;
	private EDNivel selectedNivel;
	private TipoCuenta selectedTipoCuenta;
	private MonedaEmpresa monedaEmpresaNacional;
	private MonedaEmpresa monedaEmpresaExtranjera;

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
	private String[] arrayNivel = { "PRIMER NIVEL", "SEGUNDO NIVEL",
			"TERCER NIVEL", "CUARTO NIVEL", "QUINTO NIVEL", "SEXTO NIVEL",
			"SEPTIMO NIVEL", "OCTAVO NIVEL", "NOVENO NIVEL" };
	private String[] arrayPeriodoActual = { "enero", "febrero", "marzo",
			"abril", "mayo", "junio", "julio", "agosto", "septiembre",
			"octubre", "noviembre", "diciembre" };
	private List<PlanCuenta> listPlanCuentaDefault = new ArrayList<PlanCuenta>();
	private List<EDNivel> listNivel = new ArrayList<EDNivel>();
	private List<TipoCuenta> listDefinicionCuenta = new ArrayList<TipoCuenta>();
	// 1 2 3 4 5 6 7 8 9
	private Integer[] arrayTamanio = { 1, 2, 2, 3, 3, 3, 3, 3, 3 };

	private UIData usersDataTable;

	private List<Sucursal> listaSucursalesActivas;
	private @Inject SucursalRepository sucursalRepository;

	private Sucursal selectSucursal;
	private Sucursal newSucursal;
	private boolean seleccionadaFormAgregarSucursal = false;

	private TreeNode selectedNode;

	// treeNode
	private TreeNode rootPC;

	@PostConstruct
	public void initNewEmpresa() {

		log.info(" init new initNewEmpresa");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();
		usuarioSession = sessionMain.getUsuarioLoggin();
		sucursalLogin = sessionMain.getSucursalLoggin();

		listaMoneda = monedaRepository.findAll();

		loadValuesDefault();
	}

	// ----------------- default ----------------

	private void loadValuesDefault() {

		numeroTab = 1;
		buttonAnterior = false;
		buttonSiguiente = true;

		tabEmpresa = "active";
		tabGestion = "";
		tabPlanCuenta = "";
		tabParametros = "";

		nombreMonedaNacional = listaMoneda.get(0).getNombre();
		selectedMonedaNacional = listaMoneda.get(0);
		simboloMonedaNacional = selectedMonedaNacional.getSimboloReferencial();
		nombreMonedaExtranjera = listaMoneda.get(1).getNombre();
		selectedMonedaExtranjera = listaMoneda.get(1);
		simboloMonedaExtranjera = selectedMonedaExtranjera
				.getSimboloReferencial();
		selectedNivel = new EDNivel();
		selectedNivel.setId(String.valueOf(0));
		parametroEmpresa = new ParametroEmpresa();
		selectedTipoCuenta = new TipoCuenta();
		selectedTipoCuenta.setDigito(0);

		// cargarMonedas(nombreMonedaNacional,"NACIONAL");
		monedaEmpresa = new MonedaEmpresa();
		newGestion = new Gestion();
		newEmpresa = new Empresa();
		newSucursal = new Sucursal();
		newGestion.setGestion(2015);

		// tituloPanel
		tituloPanel = "Registrar Empresa";
		// traer todos las Empresa ordenados por ID Desc
		listaEmpresa = empresaRepository.findAllByUsuario(usuarioSession);
		listaEmpresaActivas = empresaRepository
				.findAllActivasByUsuario(usuarioSession);
		seleccionadaFormAgregarEmpresa = true;
		if (listaEmpresaActivas.isEmpty()) {
			formTitulo = "CREAR EMPRESA";
			seleccionadaFormEmpresa = false;
			seleccionadaFormGestion = false;
			buttonCancelar = false;
		} else {
			elegirEmpresaSucursal();
		}
		rootPC = new DefaultTreeNode("Root", null);
		modificar = false;
		cargarPlanCuentaDefault();
		cargarTreeNiveles();
		definirListCuenta();
	}

	private void elegirEmpresaSucursal() {
		System.out.println("Ingreso a elegirEmpresaSucursal....");
		if (listaEmpresa.size() == 1) {
			seleccionadaFormEmpresa = false;
			selectedEmpresa = listaEmpresa.get(0);
			newEmpresa = selectedEmpresa;
			listaGestion = gesionRepository.findAllByEmpresa(selectedEmpresa);
			listaSucursalesActivas = sucursalRepository
					.findAllByEmpresa(selectedEmpresa);
			// resetearFitrosTabla("form1:dataTableGestion");

			verificarGestion();
			if (listaSucursalesActivas.size() == 1) {
				seleccionadaFormAgregarSucursal = false;
				selectSucursal = listaSucursalesActivas.get(0);
				newSucursal=selectSucursal;
				seleccionadaFormGestion = true;
				System.out.println("sucursal: "+selectSucursal.getNombre());
				definirFormatoFactura();
				definirFormatoTipoFactura();
				if (listaGestion.size() == 1) {
					seleccionadaFormGestion = false;
					selectedGestion = listaGestion.get(0);
					try {
						HttpSession session = (HttpSession) FacesContext
								.getCurrentInstance().getExternalContext()
								.getSession(false);
						session.setAttribute("empresa",
								selectedEmpresa.getRazonSocial());
						session.setAttribute("sucursal",
								selectSucursal.getNombre());
						session.setAttribute("gestion",
								selectedGestion.getGestion());

						FacesContext
								.getCurrentInstance()
								.getExternalContext()
								.redirect(
										((HttpServletRequest) facesContext
												.getExternalContext()
												.getRequest()).getContextPath()
												+ "pages/dashboard.xhtml");
					} catch (Exception e) {
					}
				} else {
					seleccionadaFormGestion = true;
				}
			} else {
				seleccionadaFormAgregarSucursal = true;
			}
		} else {
			seleccionadaFormEmpresa = true;
		}
	}

	private void cargarPlanCuentaDefault() {
		nivel = 2;
		codigo = "9.99";
		listPlanCuentaDefault = empresaRegistration.obtenerPlanCuentaDefault();
		loadTreeNode(rootPC, obtenerPadresNull(listPlanCuentaDefault));
	}

	private List<PlanCuenta> obtenerPadresNull(
			List<PlanCuenta> listPlanCuentaDefault) {
		List<PlanCuenta> listResult = new ArrayList<PlanCuenta>();
		for (PlanCuenta pc : listPlanCuentaDefault) {
			if (pc.getPlanCuentaPadre() == null) {
				listResult.add(pc);
			}
		}
		return listResult;
	}

	private void definirListCuenta() {
		listDefinicionCuenta = new ArrayList<>();
		TipoCuenta tc = new TipoCuenta();
		tc.setEmpresa(null);
		tc.setGestion(null);
		tc.setUsuarioRegistro(nombreUsuario);
		tc.setEstado("AC");
		tc.setFechaModificacion(null);
		tc.setFechaRegistro(null);
		tc.setDigito(1);
		tc.setNombre("ACTIVO");
		selectedTipoCuenta = tc;
		digitoAnterior = tc.getDigito();
		listDefinicionCuenta.add(tc);
		tc = new TipoCuenta();
		tc.setEmpresa(null);
		tc.setGestion(null);
		tc.setUsuarioRegistro(nombreUsuario);
		tc.setEstado("AC");
		tc.setFechaModificacion(null);
		tc.setFechaRegistro(null);
		tc.setDigito(2);
		tc.setNombre("PASIVO");
		listDefinicionCuenta.add(tc);
		tc = new TipoCuenta();
		tc.setEmpresa(null);
		tc.setGestion(null);
		tc.setUsuarioRegistro(nombreUsuario);
		tc.setEstado("AC");
		tc.setFechaModificacion(null);
		tc.setFechaRegistro(null);
		tc.setDigito(3);
		tc.setNombre("PATRIMONIO");
		listDefinicionCuenta.add(tc);
		tc = new TipoCuenta();
		tc.setEmpresa(null);
		tc.setGestion(null);
		tc.setUsuarioRegistro(nombreUsuario);
		tc.setEstado("AC");
		tc.setFechaModificacion(null);
		tc.setFechaRegistro(null);
		tc.setDigito(4);
		tc.setNombre("INGRESO");
		listDefinicionCuenta.add(tc);

		tc = new TipoCuenta();
		tc.setEmpresa(null);
		tc.setGestion(null);
		tc.setUsuarioRegistro(nombreUsuario);
		tc.setEstado("AC");
		tc.setFechaModificacion(null);
		tc.setFechaRegistro(null);
		tc.setDigito(5);
		tc.setNombre("EGRESO");
		listDefinicionCuenta.add(tc);

		tc = new TipoCuenta();
		tc.setEmpresa(null);
		tc.setGestion(null);
		tc.setUsuarioRegistro(nombreUsuario);
		tc.setEstado("AC");
		tc.setFechaModificacion(null);
		tc.setFechaRegistro(null);
		tc.setDigito(6);
		tc.setNombre("GASTOS");
		listDefinicionCuenta.add(tc);

		tc = new TipoCuenta();
		tc.setEmpresa(null);
		tc.setGestion(null);
		tc.setUsuarioRegistro(nombreUsuario);
		tc.setEstado("AC");
		tc.setFechaModificacion(null);
		tc.setFechaRegistro(null);
		tc.setDigito(7);
		tc.setNombre("COSTOS");
		listDefinicionCuenta.add(tc);

		tc = new TipoCuenta();
		tc.setEmpresa(null);
		tc.setGestion(null);
		tc.setUsuarioRegistro(nombreUsuario);
		tc.setEstado("AC");
		tc.setFechaModificacion(null);
		tc.setFechaRegistro(null);
		tc.setDigito(8);
		tc.setNombre("CTA. ORDEN DEUDORA");
		listDefinicionCuenta.add(tc);

		tc = new TipoCuenta();
		tc.setEmpresa(null);
		tc.setGestion(null);
		tc.setUsuarioRegistro(nombreUsuario);
		tc.setEstado("AC");
		tc.setFechaModificacion(null);
		tc.setFechaRegistro(null);
		tc.setDigito(9);
		tc.setNombre("CTA. ORDEN ACREEDORA");
		listDefinicionCuenta.add(tc);
	}

	// ----------------- treenode ----------------

	public void cargarTreeNiveles() {
		listNivel = new ArrayList<EDNivel>();
		EDNivel edNivel1 = new EDNivel("1", arrayNivel[0], arrayTamanio[0]);
		listNivel.add(edNivel1);
		EDNivel edNivel2 = new EDNivel("2", arrayNivel[1], arrayTamanio[1]);
		listNivel.add(edNivel2);

		for (int i = 2; i < nivel; i++) {
			EDNivel edNivel3 = new EDNivel(String.valueOf(i + 1),
					arrayNivel[i], arrayTamanio[i]);
			listNivel.add(edNivel3);
		}
	}

	private void loadTreeNode(TreeNode root, List<PlanCuenta> listPlanCuenta) {
		if (listPlanCuenta.size() > 0) {
			for (PlanCuenta pc : listPlanCuenta) {
				String moneda = pc.getMonedaEmpresa() != null ? pc
						.getMonedaEmpresa().getMoneda().getNombre() : "";
				TreeNode tn = new DefaultTreeNode(new EDPlanCuenta(pc.getId(),
						pc.getCodigo(), pc.getDescripcion(), pc.getClase(),
						moneda, pc), root);
				tn.setExpanded(true);
				loadTreeNode(tn, obtenerHijas(pc));
			}
		}
	}

	private List<PlanCuenta> obtenerHijas(PlanCuenta paramPC) {
		List<PlanCuenta> listResult = new ArrayList<PlanCuenta>();
		for (PlanCuenta pc : listPlanCuentaDefault) {
			if (pc.getPlanCuentaPadre() != null) {
				if (paramPC.getCodigo().equals(
						pc.getPlanCuentaPadre().getCodigo())) {
					listResult.add(pc);
				}
			}
		}
		return listResult;
	}

	// ---------------- acciones para nivel --------------------

	public void aumentarOSubir() {
		loadVarTab();
		if (!swNivel) {
			return;
		}// salir
		if (nivel == 5) {
			tipoPlanCuenta = "default";
		} else {
			tipoPlanCuenta = "personalizado";
		}
		if (nivelAnterior < nivel) {
			// aumentar una codificacion al nivel aumentado
			codigo = aumentar1Nivel();
			int nivelAux = this.nivel;
			EDNivel edNivel3 = new EDNivel(String.valueOf(nivelAux),
					arrayNivel[nivelAux - 1], arrayTamanio[nivelAux - 1]);
			listNivel.add(edNivel3);
		} else {
			// quitar una codificacion al nivel disminuido
			codigo = quitar1Nivel();
			listNivel.remove(listNivel.size() - 1);
		}
	}

	public void loadVarTab() {
		tabEmpresa = "";
		tabGestion = "";
		tabPlanCuenta = "active";
		tabParametros = "";
	}

	public void loadVarTabItem(int item) {
		tabEmpresa = "";
		tabGestion = "";
		tabPlanCuenta = "";
		tabParametros = "";
		switch (item) {
		case 1:
			tabEmpresa = "active";
			break;
		case 2:
			tabGestion = "active";
			break;
		case 3:
			tabPlanCuenta = "active";
			break;
		case 4:
			tabParametros = "active";
			break;

		default:
			break;
		}
	}

	private String aumentar1Nivel() {
		log.info("aumentar1Nivel()");
		String aux = ".";
		int nivelAux = nivel;
		int tamanioAux = arrayTamanio[nivelAux - 1];
		for (int i = 0; i < tamanioAux; i++) {
			aux = aux + "9";
		}
		return codigo + aux;
	}

	private String quitar1Nivel() {
		log.info("quitar1Nivel()");
		String aux = codigo;
		int length = aux.length();
		log.info("length: " + length);
		for (int index = length; index > 0; index--) {
			String letra = String.valueOf(codigo.charAt(index - 1));
			log.info("letra: " + letra);
			if (letra.equals(".")) {
				return aux.substring(0, index - 1);
			}
		}
		return aux;
	}

	public void cargarTamanio() {
		loadVarTab();
		EDNivel aux = selectedNivel;
		aux.setTamanio(tamanio);
		cargarNivelToList(aux);
		actualizarCodigo();
	}

	public void cargarDigitoDefinicionCuenta() {
		loadVarTab();
		ordenarListTipoCuenta();
	}

	private void ordenarListTipoCuenta() {
		// se ordena de tal modo que no haiga 2 numeros repetidos
		for (TipoCuenta tc : listDefinicionCuenta) {
			if (tc.getDigito() == selectedTipoCuenta.getDigito()
					&& !tc.getNombre().equals(selectedTipoCuenta.getNombre())) {
				// cargar el numero que falta
				tc.setDigito(digitoAnterior);
			}
		}
		digitoAnterior = selectedTipoCuenta.getDigito();
	}

	private void actualizarCodigo() {
		String aux = "";
		for (int i = 0; i < nivel; i++) {
			Integer t = arrayTamanio[i];
			for (int j = 0; j < t; j++) {
				aux = aux + "9";
			}
			aux = aux + ".";
		}
		codigo = aux.substring(0, aux.length() - 1);
	}

	private void cargarNivelToList(EDNivel aux) {
		for (int index = 0; index < listNivel.size(); index++) {
			EDNivel edNivel = listNivel.get(index);
			if (edNivel.equals(aux)) {
				listNivel.set(index, aux);
				arrayTamanio[index] = aux.getTamanio();
				return;
			}
		}
	}

	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance()
				.getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	// ----------------------busqueda de objetos localmente -----------------

	private Moneda buscarMonedaByLocal(String moneda) {
		for (Moneda m : listaMoneda) {
			if (m.getNombre().equals(moneda)) {
				return m;
			}
		}
		return null;
	}

	// ------------------------- conversation -------------------------

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

	// ------------- registros, modificaciones --------------------

	public void registrarEmpresa2() {
		try {
			Date fechaActual = new Date();
			newEmpresa
					.setRazonSocial(newEmpresa.getRazonSocial().toUpperCase());
			newEmpresa.setEstado("AC");
			newEmpresa.setUsuarioRegistro(usuarioSession.getLogin());
			newEmpresa.setFecha_registro(fechaActual);

			Empresa empresaAux = empresaRegistration.create(newEmpresa);

			// gestion
			newGestion.setPeriodo(periodo);
			newGestion.setPeriodoActual(periodoActual);
			newGestion.setEmpresa(empresaAux);
			newGestion.setEstado("AC");
			newGestion.setFechaRegistro(fechaActual);
			newGestion.setUsuarioRegistro(nombreUsuario);
			newGestion = gestionRegistration.create(newGestion);

			// cargar moneda
			cargarMoneda(fechaActual, empresaAux);

			// Usuario Empresa
			cargarUsuarioEmpresa(fechaActual, empresaAux);

			// tipo de cambio y tipo de cambio ufv
			cargarTipoCambio(fechaActual, empresaAux);

			// tipoComprobante
			cargarTipoComprobante(fechaActual, empresaAux);

			// cargar sucursal
			cargarSucursal(fechaActual, empresaAux);

			// cargar niveles
			List<Nivel> listNivelAux = cargarNivel(empresaAux);

			// cargar tipo de cuentas
			cargarTipoCuenta(fechaActual, empresaAux, newGestion);

			// cargar plan de cuenta
			// preguntar si el usuario quiere cargar plan de cuenta por defecto
			if (tipoPlanCuenta.equals("default") && nivel == 5) {
				empresaRegistration.cargarPlanCuentaDesdeArchivo(
						listDefinicionCuenta, listNivelAux, empresaAux,
						usuarioSession, monedaEmpresaNacional,
						monedaEmpresaExtranjera);
				parametroEmpresa.setCodificacionEtandar("9.9.9.99.999");
			}
			// parametro empresa
			cargarParametroEmpresa(fechaActual, empresaAux);

			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Empresa Registrada!", newEmpresa.getRazonSocial());
			facesContext.addMessage(null, m);
			formTitulo = "EMPRESA";
			seleccionadaFormEmpresa = true;
			seleccionadaFormGestion = false;
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			FacesUtil.redirect(request.getContextPath() + "/pages/index.xhtml");
			loadValuesDefault();
		} catch (Exception e) {
			log.error("registrarEmpresa2() -> " + e.getMessage());
			FacesUtil.errorMessage("Error al registrar.");
		}
	}

	private void cargarTipoCuenta(Date fechaActual, Empresa empresa,
			Gestion gestion) {
		for (TipoCuenta tc : listDefinicionCuenta) {
			tc.setFechaRegistro(fechaActual);
			tc.setEmpresa(empresa);
			tc.setGestion(gestion);
			tc = tipoCuentaRegistration.create(tc);
		}
	}

	private List<Nivel> cargarNivel(Empresa empresa) {
		List<Nivel> listNivelAux = new ArrayList<Nivel>();
		for (EDNivel edNivel : listNivel) {
			Nivel nivel = new Nivel();
			nivel.setEmpresa(empresa);
			nivel.setNivel(Integer.parseInt(edNivel.getId()));
			nivel.setNroDigito(edNivel.getTamanio());
			nivelRegistration.create(nivel);
			listNivelAux.add(nivel);
		}
		return listNivelAux;
	}

	private void cargarSucursal(Date fechaActual, Empresa empresa) {
		Sucursal sucursal = new Sucursal();
		sucursal.setDescripcion("SUCURSAL PRINCIPAL");
		sucursal.setDireccion("calle 000");
		sucursal.setEmpresa(empresa);
		sucursal.setEstado("AC");
		sucursal.setFechaRegistro(fechaActual);
		sucursal.setNombre("CASA MATRIZ");
		sucursal.setTelefono("000");
		sucursal.setUsuarioRegistro(nombreUsuario);
		sucursalRegistration.create(sucursal);
	}

	private void cargarParametroEmpresa(Date fechaActual, Empresa empresa) {
		parametroEmpresa.setCodificacionEtandar(codigo);
		parametroEmpresa.setEmpresa(empresa);
		parametroEmpresa.setNivelMaximo(nivel);
		parametroEmpresa.setEstado("AC");
		parametroEmpresa.setFechaRegistro(fechaActual);
		parametroEmpresa.setUsuarioRegistro(nombreUsuario);
		parametroEmpresaRegistration.create(parametroEmpresa);
	}

	private void cargarUsuarioEmpresa(Date fechaActual, Empresa empresa) {
		UsuarioEmpresa ue = new UsuarioEmpresa();
		ue.setEmpresa(empresa);
		ue.setUsuario(usuarioSession);
		usuarioEmpresaRegistration.create(ue);
	}

	private void cargarMoneda(Date fechaActual, Empresa empresa) {
		log.info("cargarMoneda");
		selectedMonedaNacional = monedaRepository.findByNombre("BOLIVIANOS");
		selectedMonedaExtranjera = monedaRepository.findByNombre("DOLAR");
		monedaEmpresaNacional = new MonedaEmpresa();
		monedaEmpresaNacional.setEmpresa(empresa);
		monedaEmpresaNacional.setMoneda(selectedMonedaNacional);
		monedaEmpresaNacional.setSimbolo(selectedMonedaNacional
				.getSimboloReferencial());
		monedaEmpresaNacional.setTipo("NACIONAL");
		monedaEmpresaNacional.setEstado("AC");
		monedaEmpresaNacional.setFechaRegistro(fechaActual);
		monedaEmpresaNacional = monedaEmpresaRegistration
				.create(monedaEmpresaNacional);
		monedaEmpresaExtranjera = new MonedaEmpresa();
		monedaEmpresaExtranjera.setEmpresa(empresa);
		monedaEmpresaExtranjera.setMoneda(selectedMonedaExtranjera);
		monedaEmpresaExtranjera.setSimbolo(selectedMonedaExtranjera
				.getSimboloReferencial());
		monedaEmpresaExtranjera.setTipo("EXTRANJERA");
		monedaEmpresaExtranjera.setEstado("AC");
		monedaEmpresaExtranjera.setFechaRegistro(fechaActual);
		monedaEmpresaExtranjera = monedaEmpresaRegistration
				.create(monedaEmpresaExtranjera);
	}

	private void cargarTipoCambio(Date fechaActual, Empresa empresaUx) {
		TipoCambio tc = new TipoCambio();
		tc.setUnidad(tipoCambio);
		tc.setFecha(fechaActual);
		tc.setFechaLiteral(obtenerLiteralFecha(fechaActual));
		tc.setEstado("AC");
		tc.setEmpresa(empresaUx);
		tipoCambioRegistration.create(tc);
		// tipo cambio UFV
		TipoCambioUfv tcUfv = new TipoCambioUfv();
		tcUfv.setEmpresa(empresaUx);
		tcUfv.setEstado("AC");
		tcUfv.setFecha(fechaActual);
		tcUfv.setFechaLiteral(obtenerLiteralFecha(fechaActual));
		tcUfv.setUnidad(tipoCambioUfv);
		tipoCambioUfvRegistration.create(tcUfv);
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

	// ----------- tipoComprobante -----------

	private void cargarTipoComprobante(Date fechaACtual, Empresa empresa) {
		// tipo de comprobante
		TipoComprobante tipoComp = new TipoComprobante();
		tipoComp.setEstado("AC");
		tipoComp.setFechaRegistro(fechaACtual);
		tipoComp.setUsuarioRegistro(nombreUsuario);
		tipoComp.setNombre("INGRESO");
		tipoComp.setEmpresa(empresa);
		tipoComp.setSigla("I");
		tipoComprobanteRegistration.registrarTipoComprobanteEmpresa(tipoComp);
		tipoComp = new TipoComprobante();
		tipoComp.setEstado("AC");
		tipoComp.setFechaRegistro(fechaACtual);
		tipoComp.setUsuarioRegistro(nombreUsuario);
		tipoComp.setEmpresa(empresa);
		tipoComp.setNombre("EGRESO");
		tipoComp.setSigla("E");
		tipoComprobanteRegistration.registrarTipoComprobanteEmpresa(tipoComp);
		tipoComp = new TipoComprobante();
		tipoComp.setEstado("AC");
		tipoComp.setFechaRegistro(fechaACtual);
		tipoComp.setUsuarioRegistro(nombreUsuario);
		tipoComp.setEmpresa(empresa);
		tipoComp.setNombre("TRASPASO");
		tipoComp.setSigla("T");
		tipoComprobanteRegistration.registrarTipoComprobanteEmpresa(tipoComp);
		tipoComp = new TipoComprobante();
		tipoComp.setEstado("AC");
		tipoComp.setFechaRegistro(fechaACtual);
		tipoComp.setUsuarioRegistro(nombreUsuario);
		tipoComp.setEmpresa(empresa);
		tipoComp.setNombre("AJUSTE");
		tipoComp.setSigla("A");
		tipoComprobanteRegistration.registrarTipoComprobanteEmpresa(tipoComp);
	}

	public void onRowSelectEmpresa(SelectEvent event) {
		newEmpresa = selectedEmpresa;
		listaGestion = gesionRepository.findAllByEmpresa(selectedEmpresa);
		nombreEstado = newEmpresa.getEstado().equals("AC") ? "ACTIVO"
				: "INACTIVO";
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
		seleccionadaFormAgregarEmpresa = false;
		seleccionadaFormAgregarSucursal = true;
		seleccionadaFormGestion = false;
		listaSucursalesActivas = sucursalRepository
				.findAllByEmpresa(newEmpresa);
		// resetearFitrosTabla("form1:dataTableGestion");

		verificarGestion();
	}

	private void verificarGestion() {
		try {
			log.info("Ingreso a verificarGestion");
			Integer gestion = Integer.parseInt(Time
					.obtenerFormatoYYYY(new Date()));
			List<Gestion> listaGestionActual = gesionRepository
					.findByGestionForEmpresa(gestion, newEmpresa);
			if (listaGestionActual.size() == 0) {

				gestionRegistration.create(new Gestion(gestion, newEmpresa,
						nombreUsuario, Time.mes(Integer.parseInt(Time
								.obtenerFormatoMM(new Date())))));
				listaGestion = gesionRepository.findAllByEmpresa(newEmpresa);
			}
		} catch (Exception e) {
			log.error("Error en verificarGestion : " + e.getMessage());
		}
	}

	private void definirFormatoFactura() {

		listFormatoHoja = formatoHojaRepository.findActivosByEmpresa(
				newEmpresa, newSucursal);
		if (listFormatoHoja.size() == 0) {
			formatoHojaRegistration.create(new FormatoHoja("COMPLETO",
					usuarioSession.getNombre(), newEmpresa, newSucursal, "AC"));
			formatoHojaRegistration.create(new FormatoHoja("SIN LOGO",
					usuarioSession.getNombre(), newEmpresa, newSucursal, "IN"));
			formatoHojaRegistration.create(new FormatoHoja(
					"SIN LOGO, SIN BORDE", usuarioSession.getNombre(),
					newEmpresa, newSucursal, "IN"));
		}

	}

	private void definirFormatoTipoFactura() {

		listFormatoFactura = formatoFacturaRepository.findActivosByEmpresa(
				newEmpresa, newSucursal);
		if (listFormatoFactura.size() == 0) {
			formatoFacturaRegistration.create(new FormatoFactura(
					"CUATRO COLUMNAS", usuarioSession.getNombre(), newEmpresa,
					newSucursal, "AC"));
			formatoFacturaRegistration.create(new FormatoFactura(
					"DOS COLUMNAS", usuarioSession.getNombre(), newEmpresa,
					newSucursal, "IN"));
		}

	}

	public void onRowSelectSucursal(SelectEvent event) {
		selectSucursal = (Sucursal) event.getObject();
		newSucursal = selectSucursal;
		log.info(newSucursal.getNombre());
		seleccionadaFormAgregarSucursal = false;
		seleccionadaFormGestion = true;
		definirFormatoFactura();
		definirFormatoTipoFactura();
		// resetearFitrosTabla("form1:dataTableGestion");
	}

	public void onRowSelectGestion(SelectEvent event) {
		this.selectedGestion = (Gestion) event.getObject();
		// cargar siguiente pagina

		try {
			HttpSession session = (HttpSession) FacesContext
					.getCurrentInstance().getExternalContext()
					.getSession(false);
			session.setAttribute("empresa", selectedEmpresa.getRazonSocial());
			session.setAttribute("sucursal", selectSucursal.getNombre());
			session.setAttribute("gestion", selectedGestion.getGestion());

			FacesContext
					.getCurrentInstance()
					.getExternalContext()
					.redirect(
							((HttpServletRequest) facesContext
									.getExternalContext().getRequest())
									.getContextPath()
									+ "pages/dashboard.xhtml");
		} catch (Exception e) {
		}
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

	// ------------------- action for view --------------------------
	// button form index.xhtml
	public void formButtonAtras() {
		seleccionadaFormEmpresa = true;
		seleccionadaFormGestion = false;
		seleccionadaFormAgregarEmpresa = true;
		formTitulo = "EMPRESA";
		selectedEmpresa = new Empresa();
	}

	public void onRowSelectNivel(SelectEvent event) {
		loadVarTab();
		tamanio = selectedNivel.getTamanio();
	}

	private int digitoAnterior = 0;

	public void onRowSelectTipoCuenta(SelectEvent event) {
		loadVarTab();
		digitoAnterior = selectedTipoCuenta.getDigito();
	}

	public void actualizarComponentes() {

	}

	public void actionButtonSiguiente() {
		log.info("actionButtonSiguiente()");
		int numeroAux = numeroTab + 1;
		if (numeroTab == 3 && numeroAux == 4) {
			numeroTab = 4;
			buttonAnterior = true;
			buttonSiguiente = false;
		} else {
			numeroTab++;
			buttonAnterior = true;
			buttonSiguiente = true;
		}
		loadVarTabItem(numeroTab);
	}

	public void actionButtonAnterior() {
		log.info("actionButtonAnterior()");
		int numeroAux = numeroTab - 1;
		if (numeroTab == 2 && numeroAux == 1) {
			numeroTab = 1;
			buttonAnterior = false;
			buttonSiguiente = true;
		} else {
			numeroTab--;
			buttonAnterior = true;
			buttonSiguiente = true;
		}
		loadVarTabItem(numeroTab);
	}

	// ---------------- get and set ----------------------

	public List<Empresa> getListaEmpresa() {
		return listaEmpresa;
	}

	public List<Empresa> getlistaEmpresaActivas() {
		return listaEmpresaActivas;
	}

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
		seleccionada1 = true;
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
		// cargar siguiente pagina
		try {
			HttpSession session = (HttpSession) FacesContext
					.getCurrentInstance().getExternalContext()
					.getSession(false);
			session.setAttribute("empresa", selectedEmpresa.getRazonSocial());
			session.setAttribute("gestion", selectedGestion.getGestion());
			session.setAttribute("sucursal", selectSucursal.getNombre());
			FacesContext
					.getCurrentInstance()
					.getExternalContext()
					.redirect(
							((HttpServletRequest) facesContext
									.getExternalContext().getRequest())
									.getContextPath()
									+ "/pages/dashboard.xhtml");
		} catch (Exception e) {
		}
	}

	public boolean isSeleccionada1() {
		return seleccionada1;
	}

	public void setSeleccionada1(boolean seleccionada1) {
		this.seleccionada1 = seleccionada1;
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

	public boolean isSeleccionadaFormAgregarEmpresa() {
		return seleccionadaFormAgregarEmpresa;
	}

	public void setSeleccionadaFormAgregarEmpresa(
			boolean seleccionadaFormAgregarEmpresa) {
		this.seleccionadaFormAgregarEmpresa = seleccionadaFormAgregarEmpresa;
	}

	public String getFormTitulo() {
		return formTitulo;
	}

	public void setFormTitulo(String formTitulo) {
		this.formTitulo = formTitulo;
	}

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
	}

	public String getNombreMonedaExtranjera() {
		return nombreMonedaExtranjera;
	}

	public void setNombreMonedaExtranjera(String nombreMonedaExtranjera) {
		this.nombreMonedaExtranjera = nombreMonedaExtranjera;
		selectedMonedaExtranjera = buscarMonedaByLocal(nombreMonedaExtranjera);
		simboloMonedaExtranjera = selectedMonedaExtranjera
				.getSimboloReferencial();
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

	public Empresa getNewEmpresa() {
		return newEmpresa;
	}

	public void setNewEmpresa(Empresa newEmpresa) {
		this.newEmpresa = newEmpresa;
	}

	public String[] getArrayNivel() {
		return arrayNivel;
	}

	public void setArrayNivel(String[] arrayNivel) {
		this.arrayNivel = arrayNivel;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public int getNivel() {
		return nivel;
	}

	// para verificar que no siga aumentando al nivel maximo =9 o disminuyendo
	// al nivelminimo=2
	private boolean swNivel = true;

	public void setNivel(int nivel) {
		nivelAnterior = this.nivel;
		log.info("nivelAnterior=" + nivelAnterior + " | nivel nuevo=" + nivel
				+ " | nivel antiguo=" + this.nivel);
		if ((nivelAnterior == nivel && nivel == 2)
				|| (nivelAnterior == nivel && nivel == 9)) {
			swNivel = false;
		} else {
			swNivel = true;
		}
		this.nivel = nivel;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getPeriodoActual() {
		return periodoActual;
	}

	public void setPeriodoActual(String periodoActual) {
		this.periodoActual = periodoActual;
	}

	public String[] getArrayPeriodoActual() {
		return arrayPeriodoActual;
	}

	public void setArrayPeriodoActual(String[] arrayPeriodoActual) {
		this.arrayPeriodoActual = arrayPeriodoActual;
	}

	public boolean isSeleccionarForm1() {
		return seleccionarForm1;
	}

	public void setSeleccionarForm1(boolean seleccionarForm1) {
		this.seleccionarForm1 = seleccionarForm1;
	}

	public double getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(double tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public double getTipoCambioUfv() {
		return tipoCambioUfv;
	}

	public void setTipoCambioUfv(double tipoCambioUfv) {
		this.tipoCambioUfv = tipoCambioUfv;
	}

	public String getTipoPlanCuenta() {
		return tipoPlanCuenta;
	}

	public void setTipoPlanCuenta(String tipoPlanCuenta) {
		this.tipoPlanCuenta = tipoPlanCuenta;
	}

	public TreeNode getRootPC() {
		return rootPC;
	}

	public void setRootPC(TreeNode rootPC) {
		this.rootPC = rootPC;
	}

	public ParametroEmpresa getParametroEmpresa() {
		return parametroEmpresa;
	}

	public void setParametroEmpresa(ParametroEmpresa parametroEmpresa) {
		this.parametroEmpresa = parametroEmpresa;
	}

	public List<EDNivel> getListNivel() {
		return listNivel;
	}

	public void setListNivel(List<EDNivel> listNivel) {
		this.listNivel = listNivel;
	}

	public UIData getUsersDataTable() {
		return usersDataTable;
	}

	public void setUsersDataTable(UIData usersDataTable) {
		this.usersDataTable = usersDataTable;
	}

	public EDNivel getSelectedNivel() {
		return selectedNivel;
	}

	public void setSelectedNivel(EDNivel selectedNivel) {
		this.selectedNivel = selectedNivel;
	}

	public boolean isButtonCancelar() {
		return buttonCancelar;
	}

	public void setButtonCancelar(boolean buttonCancelar) {
		this.buttonCancelar = buttonCancelar;
	}

	public int getTamanio() {
		return tamanio;
	}

	public void setTamanio(int tamanio) {
		this.tamanio = tamanio;
	}

	public String getTabEmpresa() {
		return tabEmpresa;
	}

	public void setTabEmpresa(String tabEmpresa) {
		this.tabEmpresa = tabEmpresa;
	}

	public String getTabGestion() {
		return tabGestion;
	}

	public void setTabGestion(String tabGestion) {
		this.tabGestion = tabGestion;
	}

	public String getTabPlanCuenta() {
		return tabPlanCuenta;
	}

	public void setTabPlanCuenta(String tabPlanCuenta) {
		this.tabPlanCuenta = tabPlanCuenta;
	}

	public String getTabParametros() {
		return tabParametros;
	}

	public void setTabParametros(String tabParametros) {
		this.tabParametros = tabParametros;
	}

	public int getNumeroTab() {
		return numeroTab;
	}

	public void setNumeroTab(int numeroTab) {
		this.numeroTab = numeroTab;
	}

	public boolean isButtonAnterior() {
		return buttonAnterior;
	}

	public void setButtonAnterior(boolean buttonAnterior) {
		this.buttonAnterior = buttonAnterior;
	}

	public boolean isButtonSiguiente() {
		return buttonSiguiente;
	}

	public void setButtonSiguiente(boolean buttonSiguiente) {
		this.buttonSiguiente = buttonSiguiente;
	}

	public List<TipoCuenta> getListDefinicionCuenta() {
		return listDefinicionCuenta;
	}

	public void setListDefinicionCuenta(List<TipoCuenta> listDefinicionCuenta) {
		this.listDefinicionCuenta = listDefinicionCuenta;
	}

	public TipoCuenta getSelectedTipoCuenta() {
		return selectedTipoCuenta;
	}

	public void setSelectedTipoCuenta(TipoCuenta selectedTipoCuenta) {
		this.selectedTipoCuenta = selectedTipoCuenta;
	}

	public List<Sucursal> getListaSucursalesActivas() {
		return listaSucursalesActivas;
	}

	public void setListaSucursalesActivas(List<Sucursal> listaSucursalesActivas) {
		this.listaSucursalesActivas = listaSucursalesActivas;
	}

	public Sucursal getSelectSucursal() {
		return selectSucursal;
	}

	public void setSelectSucursal(Sucursal selectSucursal) {
		this.selectSucursal = selectSucursal;
	}

	public Sucursal getNewSucursal() {
		return newSucursal;
	}

	public void setNewSucursal(Sucursal newSucursal) {
		this.newSucursal = newSucursal;
	}

	public boolean isSeleccionadaFormAgregarSucursal() {
		return seleccionadaFormAgregarSucursal;
	}

	public void setSeleccionadaFormAgregarSucursal(
			boolean seleccionadaFormAgregarSucursal) {
		this.seleccionadaFormAgregarSucursal = seleccionadaFormAgregarSucursal;
	}

	public Sucursal getSucursalLogin() {
		return sucursalLogin;
	}

	public void setSucursalLogin(Sucursal sucursalLogin) {
		this.sucursalLogin = sucursalLogin;
	}

	public List<FormatoFactura> getListFormatoFactura() {
		return listFormatoFactura;
	}

	public void setListFormatoFactura(List<FormatoFactura> listFormatoFactura) {
		this.listFormatoFactura = listFormatoFactura;
	}

}
