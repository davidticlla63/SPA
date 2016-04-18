package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
import javax.servlet.http.HttpServletRequest;

import org.primefaces.component.api.UIData;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.TreeNode;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.AsientoContableRepository;
import bo.com.erp360.webapp.data.CentroCostoRepository;
import bo.com.erp360.webapp.data.ComprobanteRepository;
import bo.com.erp360.webapp.data.DetalleGrupoImpuestoRepository;
import bo.com.erp360.webapp.data.DosificacionRepository;
import bo.com.erp360.webapp.data.GrupoImpuestoRepository;
import bo.com.erp360.webapp.data.MayorRepository;
import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.data.ParametroEmpresaRepository;
import bo.com.erp360.webapp.data.PlanCuentaBancariaRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.TemplateTipoComprobanteRepository;
import bo.com.erp360.webapp.data.TipoCambioRepository;
import bo.com.erp360.webapp.data.TipoComprobanteRepository;
import bo.com.erp360.webapp.model.AsientoContable;
import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.Compra;
import bo.com.erp360.webapp.model.Comprobante;
import bo.com.erp360.webapp.model.DetalleGrupoImpuesto;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Egreso;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.GrupoImpuesto;
import bo.com.erp360.webapp.model.Mayor;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.ParametroEmpresa;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.PlanCuentaBancaria;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TipoCambio;
import bo.com.erp360.webapp.model.TipoComprobante;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.AsientoContableRegistration;
import bo.com.erp360.webapp.service.CompraRegistration;
import bo.com.erp360.webapp.service.ComprobanteRegistration;
import bo.com.erp360.webapp.service.EgresoRegistration;
import bo.com.erp360.webapp.service.MayorRegistration;
import bo.com.erp360.webapp.util.ApplicationMain;
import bo.com.erp360.webapp.util.EDAsiento;
import bo.com.erp360.webapp.util.EDCentroCosto;
import bo.com.erp360.webapp.util.EDPlanCuenta;
import bo.com.erp360.webapp.util.FacesUtil;
import bo.com.erp360.webapp.util.NumerosToLetras;
import bo.com.erp360.webapp.util.Fechas;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "comprobanteController")
@ConversationScoped
public class ComprobanteController implements Serializable {

	private static final long serialVersionUID = -4058327498353920610L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private MonedaRepository monedaRepository;

	@Inject
	private MayorRepository mayorRepository;

	@Inject
	private ComprobanteRepository comprobanteRepository;

	@Inject
	private SucursalRepository sucursalRepository;

	@Inject
	private TipoCambioRepository tipoCambioRepository;

	@Inject
	private CentroCostoRepository centroCostoRepository;

	@Inject
	private TipoComprobanteRepository tipoComprobanteRepository;

	@Inject
	private PlanCuentaBancariaRepository planCuentaBancariaRepository;

	@Inject
	private TemplateTipoComprobanteRepository templateTipoComprobanteRepository;

	@Inject
	private ComprobanteRegistration comprobanteRegistration;

	@Inject
	private EgresoRegistration egresoRegistration;

	@Inject
	private MayorRegistration mayorRegistration;

	@Inject
	private AsientoContableRegistration asientoContableRegistration;
	
	@Inject
	private CompraRegistration compraRegistration;

	@Inject
	private AsientoContableRepository asientoContableRepository;

	@Inject
	private GrupoImpuestoRepository grupoImpuestoRepository;

	@Inject
	private DetalleGrupoImpuestoRepository detalleGrupoImpuestoRepository;

	@Inject
	private ParametroEmpresaRepository parametroEmpresaRepository;

	@Inject
	private DosificacionRepository dosificacionRepository;

	private @Inject SessionMain sessionMain; //variable del login

	private @Inject ApplicationMain applicactionMain; //variable del login

	Logger log = Logger.getLogger(this.getClass());

	//parametro obtenedido desde otra controller
	private Integer idComprobante;

	// session login
	private Usuario usuarioSession;
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private String infoComprobante = "test";
	private String tituloPanel = "Registrar Comprobante";
	private String nombreCuenta ="";
	private String nombreTipoComprobante;
	private String nombreSucursal;
	private String nombreCentroCosto;
	private String nombrePersona;
	private String textoAutoCompleteCuenta;
	private String textoAutoCompleteCentroCosto;
	private String textoAutoCompleteCuentaBancaria;
	private String nombreUsuario; 
	private String glosa;
	private String nombreMonedaEmpresa;
	private String nombreCuentaBancaria;
	private String nombreGrupoImpuesto;
	private String simbolo;
	private String urlComprobante;
	private String correltativoComprobante;
	private String numeroFactura = "";
	private int selectedIdEDAsiento;
	private int numeroComprobante;
	private Date fechaActual;
	private Date fechaMinima;
	private  String numeroCheque;

	@Produces
	@Named
	private Comprobante newComprobante;
	@Produces
	@Named
	private AsientoContable newAsientoContable;
	@Produces
	@Named
	private List<AsientoContable> listAsientoContable= new ArrayList<AsientoContable>();
	private List<EDAsiento> listEDAsiento= new ArrayList<>();
	private List<TipoComprobante> listTipoComprobante;
	private List<TipoCambio> listTipoCambio;
	private List<Sucursal> listSucursal;
	private List<Usuario> listUsuario = new ArrayList<Usuario>();
	private List<PlanCuenta> listCuentasAuxiliares = new ArrayList<PlanCuenta>();
	private List<CentroCosto> listCentroCosto = new ArrayList<CentroCosto>();
	private List<MonedaEmpresa> listMonedaEmpresa;
	private List<PlanCuenta> listTemplateTipoComprobante = new ArrayList<PlanCuenta>();
	private List<PlanCuentaBancaria> listPlanCuentaBancaria = new ArrayList<PlanCuentaBancaria>();
	private List<GrupoImpuesto> listGrupoImpuesto = new ArrayList<GrupoImpuesto>();
	private String[] arrayNombrePersona = {"Recibido De:","Pagado A:","Concepto de:","Concepto de:"};
	private List<Integer> listDatosMonto = new ArrayList<Integer>();
	private Double[] arrayMonto = {0d,0d,0d,0d,0d,0d,0d,0d,0d,0d};

	//object
	private AsientoContable selectedAsientoContable;
	private EDAsiento selectedEDAsiento;
	private PlanCuenta selectedPlanCuenta;
	private CentroCosto selectedCentroCosto;	
	private TipoComprobante selectedTipoComprobante;
	private Sucursal selectedSucursal;
	private TipoCambio selectedTipoCambio;
	private TipoCambio newTipoCambio;
	private PlanCuenta busquedaCuenta;
	private CentroCosto busquedaCentroCosto;
	private MonedaEmpresa monedaEmpresa;
	private PlanCuentaBancaria busquedaPlanCuentaBancaria;
	private Egreso egreso;
	private Compra newCompra;
	private GrupoImpuesto selectedGrupoImpuesto;
	private Dosificacion dosificacion;
	private ParametroEmpresa parametroEmpresa;

	//treenode
	private TreeNode selectedNodeCentroCosto;
	private TreeNode selectedNodeCuenta;

	//estados
	private boolean crear = true;
	private boolean registrar = false;
	private boolean modificar = false;
	private boolean permitirCredito = true;
	private boolean agregoGrupoImpuesto = false;
	private boolean sinPlanCuenta = false;
	private boolean sinDosificacionActiva = false;
	private boolean sinSubCentroCosto = false;
	private boolean habilitarHaber = true ;
	private boolean habilitarDebe = true ;

	//estados edit asiento
	private boolean selectedDebeNacional;
	private boolean selectedHaberNacional;
	private boolean selectedDebeExtranjero;
	private boolean selectedHaberExtranjero;

	//totales
	private double totalDebeNacional;
	private double totalHaberNacional;
	private double totalDebeExtranjero;
	private double totalHaberExtranjero;
	private double debe;
	private double haber;

	//componente dataTable
	private UIData usersDataTable;
	private Date fechaComprobante;

	// reporte
	private Integer pMes = 0;
	private Integer pGestion = 0 ; 
	private Integer pTipoComprobante = 0 ; 
	private Integer pSucursal = 0 ; 
	private Integer pEmpresa = 0;

	//error en comprobante
	private boolean error = false;

	@PostConstruct
	public void initNewComprobante() {

		log.info(" init new initNewComprobante contador = "+applicactionMain.getContadorTest());
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		fechaActual = new Date();
		newCompra = new Compra();
		newCompra.setFechaFactura(fechaActual);

		//preguntar  si tiene una estructura de plan de cuenta con minimo una cuenta auxiliar
		listCuentasAuxiliares = applicactionMain.findPlanCuentaAuxiliarByEmpresa(empresaLogin);// applicationMain.findPlanCuentaByEmpresa(empresaLogin);
		if(listCuentasAuxiliares.size()==0){
			log.info("sin plan de cuenta");
			sinPlanCuenta = true;
		}

		//preguntar si tiene una dosificacion asignada para una sucursal
		listSucursal = sucursalRepository.findAllActivasByEmpresa(empresaLogin);
		nombreSucursal = listSucursal.size()>0?listSucursal.get(0).getNombre():"";
		selectedSucursal = listSucursal.size()>0?listSucursal.get(0):null;
		dosificacion = obtenerDosificacion();
		if(dosificacion == null){
			log.info("sin dosificacion");
			sinDosificacionActiva = true;
		}

		//verificar si la empresa utilizara centro de costo
		parametroEmpresa = parametroEmpresaRepository.findByEmpresa(empresaLogin);
		if(parametroEmpresa.isCentroCosto()){
			//pregunta si tiene subcentro de costo registrados
			listCentroCosto = centroCostoRepository.findAllCentroCostoByEmpresa(empresaLogin);
			if(listCentroCosto.size()==0){
				sinSubCentroCosto = true;
			}
		}

		listTipoComprobante = tipoComprobanteRepository.findAllByEmpresa(empresaLogin);

		listTipoCambio = tipoCambioRepository.findAllByEmpresa(empresaLogin);
		listMonedaEmpresa = monedaRepository.findMonedaEmpresaAllByEmpresa(empresaLogin);
		monedaEmpresa = listMonedaEmpresa.get(0);
		nombreMonedaEmpresa = monedaEmpresa.getMoneda().getNombre();
		simbolo = monedaEmpresa.getSimbolo();
		listPlanCuentaBancaria = planCuentaBancariaRepository.findAllActivasByEmpresa(empresaLogin);
		nombreCuentaBancaria = listPlanCuentaBancaria.size()>0?listPlanCuentaBancaria.get(0).getDescripcion():"";
		busquedaPlanCuentaBancaria = listPlanCuentaBancaria.size()>0?listPlanCuentaBancaria.get(0):null;
		cargarFechaInicalGestion();
		loadValuesDefault();

		//verifica si se va a editar un comprobante
		String valueEditar = sessionMain.getAttributeSession("idC");
		log.info("valueEditar = "+valueEditar);
		if(valueEditar != null){
			sessionMain.removeAttributeSession("idC");
			idComprobante = Integer.parseInt(valueEditar);
			preRenderView();
		}
		//verifica si se va a copiar un comprobante
		String valueCopiar = sessionMain.getAttributeSession("idCCopia");
		log.info("valueCopiar = "+valueCopiar);
		if(valueCopiar != null){
			sessionMain.removeAttributeSession("idCCopia");
			idComprobante = Integer.parseInt(valueCopiar);
			preRenderViewCopia();
		}

		//verifica si se va a copiar un comprobante
		String valueRevertir = sessionMain.getAttributeSession("idCRev");
		log.info("valueRevertir = "+valueRevertir);
		if(valueRevertir != null){
			sessionMain.removeAttributeSession("idCRev");
			idComprobante = Integer.parseInt(valueRevertir);
			preRenderViewRevertir();
		}
	}

	private void loadValuesDefault(){
		numeroCheque = "";
		numeroFactura = "";
		debe = 0;
		haber = 0;
		totalDebeNacional = 0;
		totalHaberNacional = 0;
		totalDebeExtranjero = 0;
		totalHaberExtranjero = 0;
		numeroComprobante = 0;

		registrar = true ;
		modificar = false;

		glosa = "";
		nombreCentroCosto = "";
		tituloPanel = "Comprobante";
		textoAutoCompleteCentroCosto = "";
		textoAutoCompleteCuenta = "";
		textoAutoCompleteCuentaBancaria = "";

		selectedTipoCambio = new TipoCambio();
		newComprobante = new Comprobante();
		selectedAsientoContable = new AsientoContable();
		newAsientoContable = new AsientoContable();
		selectedPlanCuenta = new PlanCuenta();
		busquedaCentroCosto = new CentroCosto();
		busquedaCuenta = new PlanCuenta();
		egreso = new Egreso();

		listEDAsiento= new ArrayList<>();

		nombreTipoComprobante = listTipoComprobante.size()> 0 ? listTipoComprobante.get(0).getNombre():"";
		selectedTipoComprobante = listTipoComprobante.size()> 0 ? listTipoComprobante.get(0):new TipoComprobante();
		newComprobante.setNumero(numeroComprobante);

		listGrupoImpuesto = grupoImpuestoRepository.findActivosByEmpresa(empresaLogin);

		fechaComprobante = Fechas.cambiarYearDate(Integer.valueOf(gestionLogin.getGestion()));
		validacionActualizacion();

		selectedTipoCambio = tipoCambioRepository.findAllByEmpresaAndFecha(empresaLogin, new Date());
		log.info("selectedTipoCambio: "+selectedTipoCambio!=null?String.valueOf(selectedTipoCambio.getUnidad()):"null");
		cargarTemplateTipoComprobante();
		cargarNombrePersona();
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

	// ------------ Validaciones -----------

	/** 
	 * @method validar campos
	 * - tiene sucursal
	 * - tiene tipo de cambio para la fecha asignada
	 * - hacer algo si no tiene creado ningun centro de costo
	 */
	private void validacionActualizacion(){
		if(listSucursal.size()>0 && existTipoCambioParaFecha()){
			log.info("si es valido Calendar.YEAR="+ Calendar.YEAR);
			numeroComprobante = obtnerNumeroComprobante();
			newComprobante.setNumero(numeroComprobante);
			newComprobante.setCorrelativo(obtenerCorrelativo(numeroComprobante));
		}
	}

	private void cargarFechaInicalGestion(){
		int year=gestionLogin.getGestion();
		int month=1;
		int day=1;
		Calendar fecha1 = new GregorianCalendar(year, month, day);
		fechaMinima = fecha1.getTime();
	}

	private boolean existTipoCambioParaFecha(){
		TipoCambio tc = tipoCambioRepository.findAllByEmpresaAndFecha(empresaLogin, fechaComprobante);
		return tc!=null?true:false;
	} 

	private void cargarLocalTipoComprobante(String values){
		for(int i=0;i<listTipoComprobante.size();i++){
			TipoComprobante tc = listTipoComprobante.get(i);
			if(values.equals(tc.getNombre())){
				selectedTipoComprobante = tc;
			}
		}
	}

	private void cargarTemplateTipoComprobante(){
		listTemplateTipoComprobante = templateTipoComprobanteRepository.findPlanCuentaByTipoComprobanteAndEmpresa(selectedTipoComprobante, empresaLogin);
		//luego cargar todas las cuentas auxiliares
		if(listTemplateTipoComprobante.size()==0){
			listTemplateTipoComprobante = new ArrayList<PlanCuenta>();
		}
		for(PlanCuenta pc: listCuentasAuxiliares){
			if(! listTemplateTipoComprobante.contains(pc)){
				listTemplateTipoComprobante.add(pc);
			}
		}
	}

	public void cargarNombrePersona(){
		log.info("cargarNombrePersona("+nombreTipoComprobante+")");
		switch (nombreTipoComprobante) {
		case "INGRESO":
			setNombrePersona(arrayNombrePersona[0]);
			break;
		case "EGRESO":
			setNombrePersona(arrayNombrePersona[1]);
			break;
		case "TRASPASO":
			setNombrePersona(arrayNombrePersona[2]);
			break;
		case "AJUSTE":
			setNombrePersona(arrayNombrePersona[3]);
			break;
		default:
			setNombrePersona(arrayNombrePersona[3]);
			break;
		}
	}

	// ------------ Centro de Costo -----------

	public void onItemSelectCentroCosto(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(CentroCosto s : listCentroCosto){
			if(s.getNombre().equals(nombre)){
				setBusquedaCentroCosto(s);
			}
		}
	}

	public List<CentroCosto> completeTextCentroCosto(String query) {
		String upperQuery = query.toUpperCase();
		List<CentroCosto> results = new ArrayList<CentroCosto>();
		for(CentroCosto i : listCentroCosto) {
			if(i.getNombre().toUpperCase().startsWith(upperQuery)){
				results.add(i);
			}
		}         
		return results;
	}

	public void onNodeSelectCentroCosto(NodeSelectEvent event) {
		String descripcion =((EDCentroCosto) event.getTreeNode().getData()).getNombre();
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "CC Seleccionado", descripcion);
		FacesContext.getCurrentInstance().addMessage(null, message);
		selectedCentroCosto = (CentroCosto) centroCostoRepository.findByNombre(descripcion) ;
	}

	public void cargarCentroCosto(){
		for(int i=0;i< listEDAsiento.size();i++){
			if(listEDAsiento.get(i).getId()==selectedIdEDAsiento){
				EDAsiento element = listEDAsiento.get(i);
				element.setCentroCosto(selectedCentroCosto);
				listEDAsiento.set(i, element);
			}
		}
	}

	// ----------- Plan de Cuenta ---------------

	private PlanCuenta obtenerPlanCuentaByNombre(String nombre){
		for(PlanCuenta cc: listCuentasAuxiliares){
			if(cc.getDescripcion().equals(nombre)){
				return cc;
			}
		}
		return null;
	}


	public void cargarCuenta(){
		for(int i=0;i< listEDAsiento.size();i++){
			if(listEDAsiento.get(i).getId()==selectedIdEDAsiento){
				EDAsiento element = listEDAsiento.get(i);

				element.setCuenta(selectedPlanCuenta);
				listEDAsiento.set(i, element);
			}
		}
	}

	public List<PlanCuenta> completeTextCuenta(String query) {
		String upperQuery = query.toUpperCase();
		List<PlanCuenta> results = new ArrayList<PlanCuenta>();
		for(PlanCuenta i : listTemplateTipoComprobante) {
			if(isNumeric(query)){
				if(i.getCodigoAuxiliar().startsWith(upperQuery)){
					results.add(i);
				}
			}else{
				if(i.getDescripcion().toUpperCase().startsWith(upperQuery)){
					results.add(i);
				}
			}
		}         
		return results;
	}

	public boolean isNumeric(Object value){
		try {
			Integer.parseInt((String) value);
			return true;
		} catch (NumberFormatException nfe){
			return false;
		}
	}

	public void onItemSelectCuenta(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(PlanCuenta s : listTemplateTipoComprobante){
			if(s.getDescripcion().equals(nombre)){
				setBusquedaCuenta(s);
			}
		}
	}

	public void onNodeSelectPlanCuenta(NodeSelectEvent event) {
		String descripcion =((EDPlanCuenta) event.getTreeNode().getData()).getCuenta().toString();
		selectedPlanCuenta = obtenerPlanCuentaByNombre(descripcion);
		setBusquedaCuenta(selectedPlanCuenta);
		textoAutoCompleteCuenta = selectedPlanCuenta.getDescripcion();
	}
	// ------------ Dosificacion -------------------

	private Dosificacion obtenerDosificacion(){
		Dosificacion d = null;
		for(Sucursal s : listSucursal){
			d = dosificacionRepository.findActivaBySucursal(s);
			if(d != null){
				nombreSucursal = s.getNombre();
				selectedSucursal = s;
				return d;
			}
		}
		return d;
	}

	// -------------------- grupo de impuesto ---------------

	private GrupoImpuesto obtenerGrupoImpuestoByLocal(String nombre){
		for(GrupoImpuesto gi :listGrupoImpuesto){
			if(gi.getNombre().equals(nombre)){
				return gi;
			}
		}
		return null;
	}

	private List<DetalleGrupoImpuesto> obtenerListDetalleGrupoImpuesto(){
		return detalleGrupoImpuestoRepository.findAllByGrupoIpuesto(selectedGrupoImpuesto);
	}


	// ----------- correlativos ------------------

	private String obtnerCorrelativoTransaccionalComprobante(){
		return String.format("%06d", comprobanteRepository.obtenerCorrelativoTransaccionalComprobante(empresaLogin, selectedSucursal));
	}

	private String obtenerCorrelativo(int comprobante){
		// pather = "1508-000001";
		Date fecha = new Date(); 
		String year = new SimpleDateFormat("yy").format(fecha);
		String mes = new SimpleDateFormat("MM").format(fecha);
		return selectedTipoComprobante.getSigla()+year+mes+"-"+String.format("%06d", comprobante);
	}

	// ------------ sucursal -------------

	public Sucursal obtenerSucursal(){
		Sucursal sucursal = new Sucursal();
		for(Sucursal s : listSucursal){
			if(s.getNombre().equals(nombreSucursal)){sucursal = s; 	}
		}
		return sucursal;
	}

	// ----------- plan de cuenta bancaria ---------------
	public List<PlanCuentaBancaria> completeTextCuentaBancaria(String query) {
		String upperQuery = query.toUpperCase();
		List<PlanCuentaBancaria> results = new ArrayList<PlanCuentaBancaria>();
		for(PlanCuentaBancaria i : listPlanCuentaBancaria) {
			if(i.getDescripcion().toUpperCase().startsWith(upperQuery)){
				results.add(i);
			}
		}         
		return results;
	}

	public void buscarCuentaBancariaByLocal(String value) {
		for(PlanCuentaBancaria s : listPlanCuentaBancaria){
			if(s.getDescripcion().equals(value)){
				setBusquedaPlanCuentaBancaria(s);
			}
		}
	}

	// ------------ metodos comprobante --------------

	private int obtnerNumeroComprobante(){
		return comprobanteRepository.obtenerNumeroComprobante(fechaComprobante,empresaLogin, selectedSucursal,selectedTipoComprobante);
	}

	public void registrarEImprimir(){
		registrarComprobante(false);
		if(! error){
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgComprobanteVistaPrevia').show();");
			resetearFitrosTabla("formTableComprobante:dataTableAsiento");
			loadValuesDefault();
		}
	}

	public void registrarComprobante(boolean state) {
		try {
			Date fechaRegistro = new Date();
			error = false;
			// tipo error 1
			if(totalDebeNacional == 0 ){
				error = true;
				setInfoComprobante("Comprobante vacío.");
				RequestContext context = RequestContext.getCurrentInstance();
				context.execute("PF('dlgInfoComprobante').show();");
				return;
			}
			// tipo error 2
			if(totalDebeNacional != totalHaberNacional){
				error = true;
				setInfoComprobante( "Comprobante Desbalanceado.");
				RequestContext context = RequestContext.getCurrentInstance();
				context.execute("PF('dlgInfoComprobante').show();");
				return;
			}
			//tipo error 3

			//verificar si glosa y nombre de la persona no estan vacio
			//			if(newComprobante.getGlosa().equals("")){
			//				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
			//						"Glosa no puede esta vacia!", "");
			//				facesContext.addMessage(null, m);
			//				return;
			//			}
			newComprobante.setFecha(fechaComprobante);
			newComprobante.setUsuarioRegistro(nombreUsuario);
			newComprobante.setFechaRegistro(fechaRegistro);
			newComprobante.setTipoComprobante(selectedTipoComprobante);
			newComprobante.setMonedaEmpresa(monedaEmpresa);
			newComprobante.setTipoCambio(selectedTipoCambio);
			newComprobante.setSucursal(selectedSucursal);
			newComprobante.setImporteTotalDebeNacional(totalDebeNacional);
			newComprobante.setImporteTotalHaberNacional(totalDebeNacional);
			newComprobante.setImporteLiteralNacional(obtenerMontoLiteral(totalDebeNacional));
			newComprobante.setImporteTotalDebeExtranjero(totalDebeExtranjero);
			newComprobante.setImporteTotalHaberExtranjero(totalDebeExtranjero);
			newComprobante.setImporteLiteralExtranjero("");
			newComprobante.setEmpresa(empresaLogin);
			newComprobante.setEstado("AC");
			newComprobante.setGestion(gestionLogin);
			newComprobante.setCorrelativoTransaccional(obtnerCorrelativoTransaccionalComprobante());
			newComprobante = comprobanteRegistration.create(newComprobante);
			//id numero nombre fecha glosa importe sucursal moneda tipoComprobante
			for(EDAsiento eda : listEDAsiento){
				AsientoContable ac = new AsientoContable();
				ac.setCentroCosto(eda.getCentroCosto());
				ac.setDebeExtranjero( eda.getDebeExtranjero());//totalDebeExtranjero);
				ac.setDebeNacional(eda.getDebeNacional() );//totalDebeNacional);
				ac.setGlosa(eda.getGlosa());
				ac.setHaberExtranjero( eda.getHaberExtranjero());//totalHaberExtranjero);
				ac.setHaberNacional( eda.getHaberNacional());//totalHaberNacional);
				ac.setPlanCuenta(eda.getCuenta());
				ac.setComprobante(newComprobante);
				ac.setUsuarioRegistro(nombreUsuario);
				ac.setEstado("AC");
				ac.setFecha(fechaRegistro);
				ac.setFechaRegistro(fechaRegistro);
				ac.setNumeroCheque(eda.getNumeroCheque());
				ac.setNumeroFactura(eda.getNumeroFactura());
				ac = asientoContableRegistration.create(ac);
				Mayor mayorAnterior = mayorRepository.findNumeroByPlanCuenta(ac.getPlanCuenta(),gestionLogin);
				mayorRegistration.registrarMayor(mayorAnterior,ac);
				Compra compra = eda.getCompra();
				if(compra!=null){
					compra.setComprobante(newComprobante);
					compraRegistration.create(newCompra);
				}
			}
			if(nombreTipoComprobante.equals("EGRESO")){
				egreso.setEstado("AC");
				egreso.setFechaRegistro(new Date());
				egreso.setPlanCuentaBancaria(busquedaPlanCuentaBancaria);
				egreso.setUsuarioRegistro(nombreUsuario);
				egreso.setComprobante(newComprobante);
				egresoRegistration.create(egreso);
			}
			cagarDatosParaReporte();
			urlComprobante = getURLImprimir();
			FacesUtil.infoMessage("Comprobante Guardado!",newComprobante.getCorrelativo());

			if(state){
				FacesUtil.redirect("comprobante_index.xhtml");
			}
		} catch (Exception e) {
			String errorMessage = e.getMessage();
			log.error("Error al guardar Comprobante : "+errorMessage);
			FacesUtil.warnMessage("Error al guardar Comprobante");
		}
	}

	public void modificarEImprimir(){
		modificarComprobante(false);
		if(! error){
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgComprobanteVistaPrevia').show();");
			resetearFitrosTabla("formTableComprobante:dataTableAsiento");
			loadValuesDefault();
		}
	}
	public void modificarComprobante(boolean state) {
		try {
			error = false;
			Date fechaRegistro = new Date();
			// tipo error 1
			if(totalDebeNacional == 0 ){
				error = true;
				setInfoComprobante("Comprobante vacío.");
				RequestContext context = RequestContext.getCurrentInstance();
				context.execute("PF('dlgInfoComprobante').show();");
				return;
			}
			// tipo error 2
			if(totalDebeNacional != totalHaberNacional){
				error = true;
				setInfoComprobante( "Comprobante Desbalanceado.");
				RequestContext context = RequestContext.getCurrentInstance();
				context.execute("PF('dlgInfoComprobante').show();");
				return;
			}
			//tipo error 3

			//verificar si glosa y nombre de la persona no estan vacio
			//			if(newComprobante.getGlosa().equals("")){
			//				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
			//						"Glosa no puede esta vacia!", "");
			//				facesContext.addMessage(null, m);
			//				return;
			//			}
			newComprobante.setFecha(fechaComprobante);
			newComprobante.setFechaModificacion(fechaRegistro);
			newComprobante.setTipoComprobante(selectedTipoComprobante);
			newComprobante.setMonedaEmpresa(monedaEmpresa);
			newComprobante.setTipoCambio(selectedTipoCambio);
			newComprobante.setSucursal(selectedSucursal);
			newComprobante.setImporteTotalDebeNacional(totalDebeNacional);
			newComprobante.setImporteTotalHaberNacional(totalDebeNacional);
			newComprobante.setImporteLiteralNacional(obtenerMontoLiteral(totalDebeNacional));
			newComprobante.setImporteTotalDebeExtranjero(totalDebeExtranjero);
			newComprobante.setImporteTotalHaberExtranjero(totalDebeExtranjero);
			newComprobante.setImporteLiteralExtranjero("");
			newComprobante.setCorrelativoTransaccional(obtnerCorrelativoTransaccionalComprobante());
			newComprobante = comprobanteRegistration.update(newComprobante);
			//id numero nombre fecha glosa importe sucursal moneda tipoComprobante
			for(EDAsiento eda : listEDAsiento){
				if(eda.getIdAsientoContable()==0){// cero porque no esta registrado en DB
					AsientoContable ac = new AsientoContable();
					ac.setCentroCosto(eda.getCentroCosto());
					ac.setDebeExtranjero( eda.getDebeExtranjero());//totalDebeExtranjero);
					ac.setDebeNacional(eda.getDebeNacional() );//totalDebeNacional);
					ac.setGlosa(eda.getGlosa());
					ac.setHaberExtranjero( eda.getHaberExtranjero());//totalHaberExtranjero);
					ac.setHaberNacional( eda.getHaberNacional());//totalHaberNacional);
					ac.setPlanCuenta(eda.getCuenta());
					ac.setComprobante(newComprobante);
					ac.setUsuarioRegistro(nombreUsuario);
					ac.setEstado("AC");
					ac.setFecha(fechaRegistro);
					ac.setFechaRegistro(fechaRegistro);
					ac = asientoContableRegistration.create(ac);
					Mayor mayorAnterior = mayorRepository.findNumeroByPlanCuenta(ac.getPlanCuenta(),gestionLogin);
					mayorRegistration.registrarMayor(mayorAnterior,ac);
				}else{
					AsientoContable ac = new AsientoContable();
					ac.setId(eda.getIdAsientoContable());
					ac.setCentroCosto(eda.getCentroCosto());
					ac.setDebeExtranjero( eda.getDebeExtranjero());//totalDebeExtranjero);
					ac.setDebeNacional(eda.getDebeNacional() );//totalDebeNacional);
					ac.setGlosa(eda.getGlosa());
					ac.setHaberExtranjero( eda.getHaberExtranjero());//totalHaberExtranjero);
					ac.setHaberNacional( eda.getHaberNacional());//totalHaberNacional);
					ac.setPlanCuenta(eda.getCuenta());
					ac.setComprobante(newComprobante);
					ac.setUsuarioRegistro(nombreUsuario);
					ac.setFechaModificacion(fechaRegistro);
					ac = asientoContableRegistration.update(ac);
					// lanzara un trigger que actualizara los mayores
				}
			}
			//esta parte se anadira a comprobante
			//se anadira los siguientes campos a comprobante
			// - plan de cuenta bancaria
			// - y otras necesarias

			cagarDatosParaReporte();
			urlComprobante = getURLImprimir();
			FacesUtil.infoMessage("Comprobante Guardado!",newComprobante.getCorrelativo());

			if(state){
				FacesUtil.redirect("comprobante_index.xhtml");
			}
			loadValuesDefault();
		} catch (Exception e) {
			error = true;
			String errorMessage = e.getMessage();
			log.error("Error al modificar Comprobante : "+errorMessage);
			FacesUtil.warnMessage("Error al modificar Comprobante");
		}
	}


	// --------- metodos linea contable -------------

	//agrega nueva linea contable
	public void agregarNuevaFila(){
		glosa = glosa.toUpperCase();
		double haberNacional = 0;
		double debeNacional = 0;
		double haberExtranjero = 0;
		double debeExtranjero = 0;
		if(monedaEmpresa.getTipo().equals("NACIONAL")){
			haberNacional= haber;
			debeNacional = debe;
			haberExtranjero = haber / selectedTipoCambio.getUnidad();
			debeExtranjero = debe / selectedTipoCambio.getUnidad();
		}else{ 
			haberExtranjero = haber;
			debeExtranjero = debe ;
			haberNacional = haber * selectedTipoCambio.getUnidad();
			debeNacional = debe * selectedTipoCambio.getUnidad(); 
		}
		log.info("busquedaCuenta.getId():"+busquedaCuenta.getId());
		//verificar si:
		//- agrego una cuenta
		if(busquedaCuenta.getId() == 0){
			FacesUtil.warnMessage("Seleccione una Cuenta");
			return ;
		}

		//- agrego glosa
		if(glosa.isEmpty()){
			FacesUtil.warnMessage("La glosa no puede estar vacia");
			return ;
		}

		//- agrego debe o haber
		if(debe== 0 && haber==0){
			FacesUtil.warnMessage("Ingrese un monto en el debe o en el haber");
			return;
		}

		//verificar si la cuenta es de INGRESO, EGRESO, COSTO, GASTO, entonces perdirle cntroCosto
		if(busquedaCuenta.getTipoCuenta().getNombre().equals("INGRESO") || busquedaCuenta.getTipoCuenta().getNombre().equals("EGRESO") || busquedaCuenta.getTipoCuenta().getNombre().equals("GASTO")||busquedaCuenta.getTipoCuenta().getNombre().equals("COSTO")){
			//- agrego centro de costo?
			if (busquedaCentroCosto.getId() == 0 ){//&& parametroEmpresa.isCentroCosto() && isSinSubCentroCosto()){
				FacesUtil.warnMessage("Seleccione un centro de costo");
				return ;
			}
		}else{
			busquedaCentroCosto = null;
		}

		//preguntar si agrego el grupo Impuesto
		if(agregoGrupoImpuesto){
			EDAsiento eda = new EDAsiento(listEDAsiento.size()+1, busquedaCuenta, glosa, busquedaCentroCosto, haberNacional, debeNacional, haberExtranjero, debeExtranjero,newCompra,0,numeroFactura,numeroCheque);
			listEDAsiento.add(eda);
			log.info("si , se agrego grupo de Impuesto");
			List<DetalleGrupoImpuesto> listDetalleGrupoImpuesto = obtenerListDetalleGrupoImpuesto();
			for(DetalleGrupoImpuesto dgi : listDetalleGrupoImpuesto){
				DetalleGrupoImpuesto detalleGrupoImpuesto = dgi;
				EDAsiento eda2 = new EDAsiento();
				if(detalleGrupoImpuesto.getTipo().equals("DEBE")){
					eda2 = new EDAsiento(listEDAsiento.size()+1, detalleGrupoImpuesto.getPlanCuenta(), glosa, busquedaCentroCosto, haberNacional, (debeNacional*detalleGrupoImpuesto.getPorcentaje())/100, haberExtranjero, (debeExtranjero*detalleGrupoImpuesto.getPorcentaje())/100, newCompra,0,numeroFactura,numeroCheque);					
				}else{
					eda2 = new EDAsiento(listEDAsiento.size()+1, detalleGrupoImpuesto.getPlanCuenta(), glosa, busquedaCentroCosto, (haberNacional*detalleGrupoImpuesto.getPorcentaje())/100, debeNacional, (haberExtranjero*detalleGrupoImpuesto.getPorcentaje())/100, debeExtranjero, newCompra,0,numeroFactura,numeroCheque);
				}
				listEDAsiento.add(eda2);
			}
		}else{
			EDAsiento eda = new EDAsiento(listEDAsiento.size()+1, busquedaCuenta, glosa, busquedaCentroCosto, haberNacional, debeNacional, haberExtranjero, debeExtranjero,newCompra,0,numeroFactura,numeroCheque);
			listEDAsiento.add(eda);
		}
		newCompra = null;
		selectedGrupoImpuesto = new GrupoImpuesto();
		numeroFactura = "";
		numeroCheque = "";
		agregoGrupoImpuesto = false;
		FacesUtil.infoMessage("Cuenta Agregada !",busquedaCuenta.getDescripcion());
		calcularTotales();
		resetValueHeaderAsiento();
	}

	//elimina una linea contable
	public void eliminarAsiento(int idAsiento){
		for(int i=0; i<listEDAsiento.size();i++){
			EDAsiento asiento = listEDAsiento.get(i);
			if(asiento.getId()==idAsiento){
				listEDAsiento.remove(i);
				calcularTotales();
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Cuenta Eliminada !" ,asiento.getCuenta().getDescripcion());
				facesContext.addMessage(null, m);
			}
		}
	}

	// accion del boton para modificar una linea contable
	public void buttonModificarAsiento(Integer id){
		log.info("buttonModificarAsiento() -> id = "+id);
		for(EDAsiento eda : listEDAsiento){
			if(eda.getId()==id){
				selectedEDAsiento = eda;
			}
		}
		this.selectedIdEDAsiento = selectedEDAsiento.getId();
		this.newCompra = selectedEDAsiento.getCompra();
		this.numeroFactura = (newCompra!=null)?newCompra.getNumeroFactura():null;

		this.busquedaCentroCosto = selectedEDAsiento.getCentroCosto();
		this.textoAutoCompleteCentroCosto = busquedaCentroCosto==null? "": busquedaCentroCosto.getNombre();

		this.glosa = selectedEDAsiento.getGlosa();

		this.busquedaCuenta = selectedEDAsiento.getCuenta();
		this.textoAutoCompleteCuenta = busquedaCuenta.getDescripcion();

		if(monedaEmpresa.getTipo().equals("NACIONAL")){
			this.debe = selectedEDAsiento.getDebeNacional();
			this.haber = selectedEDAsiento.getHaberNacional();
		}else{
			this.debe = selectedEDAsiento.getDebeExtranjero();
			this.haber = selectedEDAsiento.getHaberExtranjero();
		}
		if(this.debe==0){
			this.habilitarDebe = false;
			this.habilitarHaber = true;
		}else if(haber==0){
			this.habilitarDebe = true;
			this.habilitarHaber = false;
		}
		FacesUtil.showDialog("dlgEditarCuenta");
	}

	public void modificarAsiento(){
		log.info("modificarAsiento()");
		for(EDAsiento eda : listEDAsiento){
			if(eda.getId()==selectedIdEDAsiento){
				eda.setId(selectedIdEDAsiento);
				eda.setCentroCosto(busquedaCentroCosto);
				eda.setCompra(newCompra);
				eda.setGlosa(glosa);
				eda.setCuenta(busquedaCuenta);
				if(monedaEmpresa.getTipo().equals("NACIONAL")){
					eda.setDebeNacional(debe);
					eda.setHaberNacional(haber);
				}else{
					eda.setDebeExtranjero(debe);
					eda.setHaberExtranjero(haber);
				}
			}
		}
		calcularTotales();
		resetValueHeaderAsiento();
		FacesUtil.updateComponent("formTableComprobante:dataTableAsiento");
		FacesUtil.hideDialog("dlgEditarCuenta");
	}

	public void resetValueHeaderAsiento(){
		busquedaCentroCosto = new CentroCosto();
		busquedaCuenta = new PlanCuenta();
		textoAutoCompleteCentroCosto = "";
		textoAutoCompleteCuenta = "" ;
		debe = 0;
		haber = 0;
		habilitarDebe = true;
		habilitarHaber = true;
	}

	public void verificarSeleccionado(String var){
		selectedDebeNacional = true;
		switch (var) {
		case "DEBE NACIONAL":
			selectedDebeNacional = true;
			selectedHaberNacional = false;
			selectedDebeExtranjero = false;
			selectedHaberExtranjero = false;
			break;
		case "HABER NACIONAL":
			selectedDebeNacional = false;
			selectedHaberNacional = true;
			selectedDebeExtranjero = false;
			selectedHaberExtranjero = false;
			break;
		case "DEBE EXTRANJERO":
			selectedDebeNacional = false;
			selectedHaberNacional = false;
			selectedDebeExtranjero = true;
			selectedHaberExtranjero = false;
			break;
		case "HABER EXTRANJERO":
			selectedDebeNacional = false;
			selectedHaberNacional = false;
			selectedDebeExtranjero = false;
			selectedHaberExtranjero = true;
			break;

		default:
			break;
		}
	}

	public boolean verificarTipoMoneda(String tipo){
		if(monedaEmpresa.getTipo().equals(tipo)){
			return true;
		}
		return false;
	}

	public void onRowSelectAsiento(SelectEvent event){
		log.info("onRowSelectAsiento");
		log.info("selectedIdEDAsiento" + selectedIdEDAsiento);
	}

	public void onRowEdit(RowEditEvent event) {
		EDAsiento data = (EDAsiento) usersDataTable.getRowData();
		if(selectedDebeNacional){
			double newDebeNacional = data.getDebeNacional();
			for(int i=0; i < listEDAsiento.size(); i++){
				EDAsiento eda = listEDAsiento.get(i);
				if(eda.getId() == data.getId()){
					double newDebeExtranjero = newDebeNacional / selectedTipoCambio.getUnidad();
					eda.setDebeExtranjero(newDebeExtranjero);
					eda.setDebeNacional(newDebeNacional);					
					listEDAsiento.set(i, eda);
				}
			}
		}
		if(selectedHaberNacional){
			double newHaberNacional = data.getHaberNacional();
			for(int i=0; i < listEDAsiento.size(); i++){
				EDAsiento eda = listEDAsiento.get(i);
				if(eda.getId() == data.getId()){
					double newHaberExtranjero = newHaberNacional / selectedTipoCambio.getUnidad();
					eda.setHaberExtranjero(newHaberExtranjero);
					eda.setHaberNacional(newHaberNacional);
					listEDAsiento.set(i, eda);
				}
			}
		}
		if(selectedDebeExtranjero){
			double newDebeExtranjero = data.getDebeExtranjero();
			for(int i=0; i < listEDAsiento.size(); i++){ 
				EDAsiento eda = listEDAsiento.get(i);
				if(eda.getId() == data.getId()){
					double newDebeNacional = newDebeExtranjero * selectedTipoCambio.getUnidad();
					eda.setDebeNacional(newDebeNacional);
					eda.setDebeExtranjero(newDebeExtranjero);
					listEDAsiento.set(i, eda);
				}
			}
		}
		if(selectedHaberExtranjero){
			double newHaberExtranjero = data.getHaberExtranjero();
			for(int i=0; i < listEDAsiento.size(); i++){ 
				EDAsiento eda = listEDAsiento.get(i);
				if(eda.getId() == data.getId()){
					double newHaberNacional = newHaberExtranjero * selectedTipoCambio.getUnidad();
					eda.setHaberNacional(newHaberNacional);
					eda.setHaberExtranjero(newHaberExtranjero);
					listEDAsiento.set(i, eda);
				}
			}
		}
		calcularTotales();
	}

	private void calcularTotales(){
		double totalDebeNacionalAux = 0;
		double totalHaberNacionalAux = 0;
		double totalDebeExtranjeroAux = 0;
		double totalHaberExtranjeroAux = 0;
		for(EDAsiento element : listEDAsiento){
			totalDebeNacionalAux	= totalDebeNacionalAux + element.getDebeNacional();
			totalHaberNacionalAux	= totalHaberNacionalAux + element.getHaberNacional();	
			totalDebeExtranjeroAux	= totalDebeExtranjeroAux + element.getDebeExtranjero();	
			totalHaberExtranjeroAux	= totalHaberExtranjeroAux + element.getHaberExtranjero();	
		}
		this.totalDebeExtranjero = totalDebeExtranjeroAux;
		this.totalHaberExtranjero = totalHaberExtranjeroAux;
		this.totalDebeNacional = totalDebeNacionalAux;
		this.totalHaberNacional = totalHaberNacionalAux;
	}

	public void onCellEdit(CellEditEvent event) {
		Object newValue = event.getNewValue();
		if(newValue != null ){
			if(selectedDebeNacional){
				double newDebeNacional = 0;
				for(EDAsiento eda: listEDAsiento){
					newDebeNacional = newDebeNacional + eda.getDebeNacional();
				}
				totalDebeNacional = newDebeNacional;
				totalDebeExtranjero = newDebeNacional * selectedTipoCambio.getUnidad();
			}
			if(selectedHaberNacional){
				double newHaberNacional = 0;
				for(EDAsiento eda: listEDAsiento){
					newHaberNacional = newHaberNacional + eda.getHaberNacional();
				}
				totalHaberNacional = newHaberNacional;
				totalHaberExtranjero = newHaberNacional * selectedTipoCambio.getUnidad();
			}
			if(selectedDebeExtranjero){
				double newDebeExtranjero = 0;
				for(EDAsiento eda: listEDAsiento){
					newDebeExtranjero = newDebeExtranjero + eda.getDebeExtranjero();
				}
				totalDebeExtranjero = newDebeExtranjero;
			}
			if(selectedHaberExtranjero){
				double newHaberExtranjero = 0;
				for(EDAsiento eda: listEDAsiento){
					newHaberExtranjero = newHaberExtranjero + eda.getHaberExtranjero();
				}
				totalHaberExtranjero = newHaberExtranjero;
			}
		}
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("formTableComprobante:dataTableAsiento");
		table.reset();
	}

	public void actualizarImportes(){
		try {
			log.info("Ingreso a actualizarImportes..."+this.getDosificacion().getNormaAplicada());
			if(this.getDosificacion().getNormaAplicada().equals("NSF-07")){
				//importeBaseCreditoFiscal = ImporteICE - ImporteExcentos
				double importeBaseCreditoFiscal = this.getNewCompra().getImporteTotal()-this.getNewCompra().getImporteICE()-this.getNewCompra().getImporteExcentos();
				this.getNewCompra().setImporteBaseCreditoFiscal(importeBaseCreditoFiscal);
			}
			if(this.getDosificacion().getNormaAplicada().equals("SFV-14")){
				//importeSubTotal = ImporteTotal = Importe No Sujeto a Credito Fiscal
				double importeSubTotal =  this.getNewCompra().getImporteTotal() - this.getNewCompra().getImporteNoSujetoCreditoFiscal();
				this.getNewCompra().setImporteSubTotal(importeSubTotal);

				//importeBaseCreditoFiscal = importeSubTotal - descuentosBonosRebajas;
				double importeBaseCreditoFiscal = this.getNewCompra().getImporteSubTotal()-this.getNewCompra().getDescuentosBonosRebajas();
				this.getNewCompra().setImporteBaseCreditoFiscal(importeBaseCreditoFiscal);
			}

		} catch (Exception e) {
			log.info("Error en actualizarImportes: "+e.getMessage());
		}
	}
	
	public void cargarGlosa(){
		this.glosa = newComprobante.getGlosa();
	}
	
	public void cancelarNuevoTipoCambio(){
		newTipoCambio = new TipoCambio();
	}

	public void onRowSelectTipoCambio(SelectEvent event) {
		//tipoCambio = selectedTipoCambio.getUnidad();
		cancelarNuevoTipoCambio();
	}

	public String obtenerMontoLiteral(double totalFactura) {
		log.info("Total Entero Factura >>>>> " + totalFactura);
		NumerosToLetras convert = new NumerosToLetras();
		String totalLiteral;
		try {
			totalLiteral = convert.convertNumberToLetter(totalFactura);
			return totalLiteral;
		} catch (Exception e) {
			log.info("Error en obtenerMontoLiteral: "
					+ e.getMessage());
			return "Error Literal";
		}
	}
	
	public void cargarLibroCompra(){
		newCompra = new Compra();
	}

	public void registrarCompra(){
		log.info("registrarCompra()");
		newCompra.setEmpresa(empresaLogin);
		newCompra.setFechaRegistro(new Date());
		newCompra.setEstado("AC");
		numeroFactura = newCompra.getNumeroFactura();
	}

	private MonedaEmpresa buscarMonedaEmpresaByLocal(String nombreMonedaEmpresa){
		for(MonedaEmpresa me: listMonedaEmpresa){
			if(nombreMonedaEmpresa.equals(me.getMoneda().getNombre())){
				return me;
			}
		}
		return null;
	}

	//-------Plan de cuenta

	public void onRowSelectPlanCuenta(SelectEvent event) {
		setBusquedaCuenta(selectedPlanCuenta);
		textoAutoCompleteCuenta = selectedPlanCuenta.getDescripcion();
	}

	public void limpiarBusquedaPlanCuenta(){
		selectedPlanCuenta = new PlanCuenta();
		selectedNodeCuenta = null;
		FacesUtil.showDialog("dlgPlanCuenta");

	}
	// ------------------ reportes ------------------

	private void cagarDatosParaReporte(){
		log.info("cagarDatosParaReporte()");
		pMes = (Integer) (newComprobante.getFecha().getMonth()+1);
		pGestion = (Integer) gestionLogin.getGestion();
		pTipoComprobante = (Integer) selectedTipoComprobante.getId();
		pSucursal = (Integer) selectedSucursal.getId() ;
		pEmpresa = (Integer) empresaLogin.getId();
		log.info(pMes+" | "+pGestion+" | "+pTipoComprobante+" | "+pSucursal+" | "+pEmpresa);
	}

	public String getURLImprimir(){
		try{
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();  
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
			String urlPDFreporte = urlPath+"ReporteComprobante?pMes="+pMes+"&pGestion="+pGestion+"&pTipoComprobante="+pTipoComprobante+"&pSucursal="+pSucursal+"&pEmpresa="+pEmpresa+"&pNumeroComprobante="+(Integer)numeroComprobante;
			log.info("getURL() -> "+urlPDFreporte);
			return urlPDFreporte;
		}catch(Exception e){
			log.info("getURL error: "+e.getMessage());
			return "error";
		}
	}

	//-------------- acciones de la vista

	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	//el usuario va a modificar
	public void preRenderView(){
		log.info("exec preRenderView() idComprobante="+idComprobante);
		if(idComprobante!=null && idComprobante!=-1){
			//obtener el comprobante y cargar correlativo
			Comprobante comprobante = comprobanteRepository.findById(idComprobante);
			//obtener tipo de Comprobante
			if(comprobante!=null){
				this.newComprobante = comprobante;
				selectedTipoComprobante = newComprobante.getTipoComprobante();
				nombreTipoComprobante = selectedTipoComprobante.getNombre();
				//obtener sucursal
				selectedSucursal = newComprobante.getSucursal();
				nombreSucursal = selectedSucursal.getNombre();
				//obtener moneda

				//obtener asientos
				List<AsientoContable> listAsientoContable = asientoContableRepository.findByComprobante(newComprobante);
				for(AsientoContable ac : listAsientoContable){
					String glosa = ac.getGlosa();
					double haberNacional = ac.getHaberNacional();
					double debeNacional = ac.getDebeNacional();
					double haberExtranjero = ac.getHaberExtranjero();
					double debeExtranjero = ac.getDebeExtranjero();
					Compra compra = null;
					EDAsiento eda = new EDAsiento(listEDAsiento.size()+1, ac.getPlanCuenta(), glosa, ac.getCentroCosto(), haberNacional, debeNacional, haberExtranjero, debeExtranjero, compra,ac.getId(),numeroFactura,numeroCheque);
					listEDAsiento.add(eda);
				}
				calcularTotales();
				//obtener tipo de cambio
				selectedTipoCambio = newComprobante.getTipoCambio();
				//fecha comprobante
				fechaComprobante = newComprobante.getFecha();
				//cambiar botones (cancelar , Modificar , Imprimir)

				//estados
				modificar = true;
				registrar = false;
			}
		}
	}

	//si el usuario va a copiar un comprobante
	public void preRenderViewCopia(){
		log.info("exec preRenderViewCopia() idComprobante="+idComprobante);
		if(idComprobante!=null && idComprobante!=-1){
			//obtener el comprobante y cargar correlativo
			Comprobante comprobante = comprobanteRepository.findById(idComprobante);
			//obtener tipo de Comprobante
			if(comprobante!=null){
				this.newComprobante = comprobante;
				selectedTipoComprobante = newComprobante.getTipoComprobante();
				nombreTipoComprobante = selectedTipoComprobante.getNombre();
				//obtener sucursal
				selectedSucursal = newComprobante.getSucursal();
				nombreSucursal = selectedSucursal.getNombre();
				//obtener moneda

				//obtener asientos
				List<AsientoContable> listAsientoContable = asientoContableRepository.findByComprobante(newComprobante);
				for(AsientoContable ac : listAsientoContable){
					String glosa = ac.getGlosa();
					double haberNacional = ac.getHaberNacional();
					double debeNacional = ac.getDebeNacional();
					double haberExtranjero = ac.getHaberExtranjero();
					double debeExtranjero = ac.getDebeExtranjero();
					Compra compra = null;
					EDAsiento eda = new EDAsiento(listEDAsiento.size()+1, ac.getPlanCuenta(), glosa, ac.getCentroCosto(), haberNacional, debeNacional, haberExtranjero, debeExtranjero, compra,0,numeroFactura,numeroCheque);
					listEDAsiento.add(eda);
				}
				//borrar el id de comprobante
				this.newComprobante.setId(0);
				calcularTotales();
				//obtener tipo de cambio
				selectedTipoCambio = newComprobante.getTipoCambio();
				//fecha comprobante
				fechaComprobante = Fechas.cambiarYearDate(Integer.valueOf(gestionLogin.getGestion()));

				//corelativos
				validacionActualizacion();

				//estados
				modificar = false;
				registrar = true;
			}
		}
	}

	//si el usuario va a copiar y revertir un comprobante
	public void preRenderViewRevertir(){
		log.info("exec preRenderViewRevertir() idComprobante="+idComprobante);
		if(idComprobante!=null && idComprobante!=-1){
			//obtener el comprobante y cargar correlativo
			Comprobante comprobante = comprobanteRepository.findById(idComprobante);
			//obtener tipo de Comprobante
			if(comprobante!=null){
				this.newComprobante = comprobante;
				selectedTipoComprobante = newComprobante.getTipoComprobante();
				nombreTipoComprobante = selectedTipoComprobante.getNombre();
				//obtener sucursal
				selectedSucursal = newComprobante.getSucursal();
				nombreSucursal = selectedSucursal.getNombre();
				//obtener moneda

				//obtener asientos
				List<AsientoContable> listAsientoContable = asientoContableRepository.findByComprobante(newComprobante);
				for(AsientoContable ac : listAsientoContable){
					String glosa = ac.getGlosa();
					//invertir debe con haber
					double haberNacional = ac.getDebeNacional();
					double debeNacional = ac.getHaberNacional();
					double haberExtranjero = ac.getDebeExtranjero();
					double debeExtranjero = ac.getHaberExtranjero();
					Compra compra = null;

					EDAsiento eda = new EDAsiento(listEDAsiento.size()+1, ac.getPlanCuenta(), glosa, ac.getCentroCosto(), haberNacional, debeNacional, haberExtranjero, debeExtranjero, compra,0,numeroFactura,numeroCheque);
					listEDAsiento.add(eda);
				}
				//borrar el id de comprobante
				this.newComprobante.setId(0);
				calcularTotales();
				//obtener tipo de cambio
				selectedTipoCambio = newComprobante.getTipoCambio();
				//fecha comprobante
				fechaComprobante = Fechas.cambiarYearDate(Integer.valueOf(gestionLogin.getGestion()));

				//corelativos
				validacionActualizacion();

				//estados
				modificar = false;
				registrar = true;
			}
		}
	}

	//actualizar tipoCabio

	public void actualizarTipoCambio(){
		log.info("actualizarTipoCambio() fechaComprobante="+fechaComprobante); 
		selectedTipoCambio = tipoCambioRepository.findAllByEmpresaAndFecha(empresaLogin, fechaComprobante);
		log.info("actualizarTipoCambio() selectedTipoCambio="+selectedTipoCambio);
	}

	// ------------- get and set ---------------
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

	public void cambiarModificar(){
		setModificar(false);
	}

	public List<Usuario> getListUsuario() {
		return listUsuario;
	}

	public void setListUsuario(List<Usuario> listUsuario) {
		this.listUsuario = listUsuario;
	}

	public Usuario getUsuario() {
		return usuarioSession;
	}

	public void setUsuario(Usuario usuarioSession) {
		this.usuarioSession = usuarioSession;
	}

	public AsientoContable getSelectedAsientoContable() {
		return selectedAsientoContable;
	}

	public void setSelectedAsientoContable(AsientoContable selectedAsientoContable) {
		this.selectedAsientoContable = selectedAsientoContable;
	}

	public String getNombreTipoComprobante() {
		return nombreTipoComprobante;
	}

	public void setNombreTipoComprobante(String nombreTipoComprobante) {
		this.nombreTipoComprobante = nombreTipoComprobante;
		cargarLocalTipoComprobante(nombreTipoComprobante);
		cargarNombrePersona();
		cargarTemplateTipoComprobante();
		validacionActualizacion();
	}

	public PlanCuenta getSelectedPlanCuenta() {
		return selectedPlanCuenta;
	}

	public void setSelectedPlanCuenta(PlanCuenta selectedPlanCuenta) {
		this.selectedPlanCuenta = selectedPlanCuenta;
	}

	public String getNombreSucursal() {
		return nombreSucursal;
	}

	public void setNombreSucursal(String nombreSucursal) {
		this.nombreSucursal = nombreSucursal;
		selectedSucursal = obtenerSucursal();
	}

	public String getNombreCentroCosto() {
		return nombreCentroCosto;
	}

	public void setNombreCentroCosto(String nombreCentroCosto) {
		this.nombreCentroCosto = nombreCentroCosto;
	}

	public String getNombreCuenta() {
		return nombreCuenta;
	}

	public void setNombreCuenta(String nombreCuenta) {
		this.nombreCuenta = nombreCuenta;
	}

	public List<TipoComprobante> getListTipoComprobante() {
		return listTipoComprobante;
	}

	public void setListTipoComprobante(List<TipoComprobante> listTipoComprobante) {
		this.listTipoComprobante = listTipoComprobante;
	}

	public int getNumeroComprobante() {
		return numeroComprobante;
	}

	public void setNumeroComprobante(int numeroComprobante) {
		this.numeroComprobante = numeroComprobante;
	}

	public List<Sucursal> getListSucursal() {
		return listSucursal;
	}

	public void setListSucursal(List<Sucursal> listSucursal) {
		this.listSucursal = listSucursal;
	}

	public List<EDAsiento> getListEDAsiento() {
		return listEDAsiento;
	}

	public void setListEDAsiento(List<EDAsiento> listEDAsiento) {
		this.listEDAsiento = listEDAsiento;
	}

	public EDAsiento getSelectedEDAsiento() {
		return selectedEDAsiento;
	}

	public void setSelectedEDAsiento(EDAsiento selectedEDAsiento) {
		this.selectedEDAsiento = selectedEDAsiento;
	}

	public int getSelectedIdEDAsiento() {
		return selectedIdEDAsiento;
	}

	public void setSelectedIdEDAsiento(int selectedIdEDAsiento) {
		this.selectedIdEDAsiento = selectedIdEDAsiento;
	}

	public CentroCosto getSelectedCentroCosto() {
		return selectedCentroCosto;
	}

	public void setSelectedCentroCosto(CentroCosto selectedCentroCosto) {
		this.selectedCentroCosto = selectedCentroCosto;
	}

	public double getTotalDebeNacional() {
		return totalDebeNacional;
	}

	public void setTotalDebeNacional(double totalDebeNacional) {
		this.totalDebeNacional = totalDebeNacional;
	}

	public double getTotalHaberNacional() {
		return totalHaberNacional;
	}

	public void setTotalHaberNacional(double totalHaberNacional) {
		this.totalHaberNacional = totalHaberNacional;
	}

	public double getTotalDebeExtranjero() {
		return totalDebeExtranjero;
	}

	public void setTotalDebeExtranjero(double totalDebeExtranjero) {
		this.totalDebeExtranjero = totalDebeExtranjero;
	}

	public double getTotalHaberExtranjero() {
		return totalHaberExtranjero;
	}

	public void setTotalHaberExtranjero(double totalHaberExtranjero) {
		this.totalHaberExtranjero = totalHaberExtranjero;
	}

	public TreeNode getSelectedNodeCentroCosto() {
		return selectedNodeCentroCosto;
	}

	public void setSelectedNodeCentroCosto(TreeNode selectedNodeCentroCosto) {
		this.selectedNodeCentroCosto = selectedNodeCentroCosto;
	}

	public TreeNode getSelectedNodeCuenta() {
		return selectedNodeCuenta;
	}

	public void setSelectedNodeCuenta(TreeNode selectedNodeCuenta) {
		this.selectedNodeCuenta = selectedNodeCuenta;
	}

	public UIData getUsersDataTable() {
		return usersDataTable;
	}

	public void setUsersDataTable(UIData usersDataTable) {
		this.usersDataTable = usersDataTable;
	}

	public String getInfoComprobante() {
		return infoComprobante;
	}

	public void setInfoComprobante(String infoComprobante) {
		this.infoComprobante = infoComprobante;
	}

	public String getNombrePersona() {
		return nombrePersona;
	}

	public void setNombrePersona(String nombrePersona) {
		this.nombrePersona = nombrePersona;
	}

	public List<TipoCambio> getListTipoCambio() {
		return listTipoCambio;
	}

	public void setListTipoCambio(List<TipoCambio> listTipoCambio) {
		this.listTipoCambio = listTipoCambio;
	}

	public TipoCambio getSelectedTipoCambio() {
		return selectedTipoCambio;
	}

	public void setSelectedTipoCambio(TipoCambio selectedTipoCambio) {
		this.selectedTipoCambio = selectedTipoCambio;
	}

	public TipoCambio getNewTipoCambio() {
		return newTipoCambio;
	}

	public void setNewTipoCambio(TipoCambio newTipoCambio) {
		this.newTipoCambio = newTipoCambio;
	}

	public List<PlanCuenta> getListCuentasAuxiliares() {
		return listCuentasAuxiliares;
	}

	public void setListCuentasAuxiliares(List<PlanCuenta> listCuentasAuxiliares) {
		this.listCuentasAuxiliares = listCuentasAuxiliares;
	}

	public List<CentroCosto> getListCentroCosto() {
		return listCentroCosto;
	}

	public void setListCentroCosto(List<CentroCosto> listCentroCosto) {
		this.listCentroCosto = listCentroCosto;
	}

	public String getTextoAutoCompleteCuenta() {
		log.info("getTextoAutoCompleteCuenta("+textoAutoCompleteCuenta+")");
		return textoAutoCompleteCuenta;
	}

	public void setTextoAutoCompleteCuenta(String textoAutoCompleteCuenta) {
		this.textoAutoCompleteCuenta = textoAutoCompleteCuenta;
	}

	public String getTextoAutoCompleteCentroCosto() {
		return textoAutoCompleteCentroCosto;
	}

	public void setTextoAutoCompleteCentroCosto(
			String textoAutoCompleteCentroCosto) {
		this.textoAutoCompleteCentroCosto = textoAutoCompleteCentroCosto;
	}

	public PlanCuenta getBusquedaCuenta() {
		log.info("getBusquedaCuenta()"+busquedaCuenta.getDescripcion());
		return busquedaCuenta;
	}

	public void setBusquedaCuenta(PlanCuenta busquedaCuenta) {
		log.info("setBusquedaCuenta("+busquedaCuenta.getDescripcion()+")");
		this.busquedaCuenta = busquedaCuenta;
	}

	public CentroCosto getBusquedaCentroCosto() {
		return busquedaCentroCosto;
	}

	public void setBusquedaCentroCosto(CentroCosto busquedaCentroCosto) {
		this.busquedaCentroCosto = busquedaCentroCosto;
	}

	public double getDebe() {
		return debe;
	}

	public void cargarArrayMonto(Double data){
		if(data!=0){
			log.info("data = " + data);
			Double[] arrayMontoBolivanoAux = {data,0d,0d,0d,0d,0d,0d,0d,0d,0d};
			for(int i=0;i<10;i++){
				Double d = this.arrayMonto[i];
				log.info("monto = " + d);
				int aux = i+1;
				log.info("aux = " + aux);
				if(aux<10){
					arrayMontoBolivanoAux[aux] = d;
				}
			}
			for(int j=0;j<10;j++){
				Double a = arrayMontoBolivanoAux[j];
				log.info("a = " + a);
				this.arrayMonto[j] = a;
			}
		}
	}
	//
	//	public void cargarMontoAInputDebe(int item){
	//		this.debe = arrayMonto[item];
	//	}
	//
	//	public void cargarMontoAInputHaber(int item){
	//		this.haber = arrayMonto[item];
	//	}

	public void setDebe(double debe) {
		this.debe = debe;
		log.info("debe = "+debe);
		cargarArrayMonto(debe);
		if(debe == 0 && haber == 0 ){
			habilitarHaber = true ;
			habilitarDebe = true ;
		}else if(debe != 0){
			habilitarHaber = false ;
			habilitarDebe = true ;
		}
	}

	public double getHaber() {
		return haber;
	}

	public void setHaber(double haber) {
		this.haber = haber;
		log.info("haber = "+haber);
		cargarArrayMonto(haber);
		if(haber == 0 && debe == 0 ){
			habilitarHaber = true ;
			habilitarDebe = true ;
		}else if(haber != 0){
			habilitarHaber = true ;
			habilitarDebe = false ;
		}
	}

	public String getGlosa() {
		return glosa;
	}

	public void setGlosa(String glosa) {
		this.glosa = glosa;
	}

	public Date getFechaComprobante() {
		return fechaComprobante;
	}

	public void setFechaComprobante(Date fechaComprobante) {
		this.fechaComprobante = fechaComprobante;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getNombreMonedaEmpresa() {
		return nombreMonedaEmpresa;
	}

	public void setNombreMonedaEmpresa(String nombreMonedaEmpresa) {
		this.nombreMonedaEmpresa = nombreMonedaEmpresa;
		monedaEmpresa = buscarMonedaEmpresaByLocal(this.nombreMonedaEmpresa);
		simbolo = monedaEmpresa.getSimbolo();
	}

	public List<MonedaEmpresa> getListMonedaEmpresa() {
		return listMonedaEmpresa;
	}

	public void setListMonedaEmpresa(List<MonedaEmpresa> listMonedaEmpresa) {
		this.listMonedaEmpresa = listMonedaEmpresa;
	}

	public MonedaEmpresa getMonedaEmpresa() {
		return monedaEmpresa;
	}

	public void setMonedaEmpresa(MonedaEmpresa monedaEmpresa) {
		this.monedaEmpresa = monedaEmpresa;
	}

	public String getSimbolo() {
		return simbolo;
	}

	public void setSimbolo(String simbolo) {
		this.simbolo = simbolo;
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

	public boolean isPermitirCredito() {
		return permitirCredito;
	}

	public void setPermitirCredito(boolean permitirCredito) {
		this.permitirCredito = permitirCredito;
	}

	public String getCorreltativoComprobante() {
		return correltativoComprobante;
	}

	public void setCorreltativoComprobante(String correltativoComprobante) {
		this.correltativoComprobante = correltativoComprobante;
	}

	public List<PlanCuenta> getListTemplateTipoComprobante() {
		return listTemplateTipoComprobante;
	}

	public void setListTemplateTipoComprobante(
			List<PlanCuenta> listTemplateTipoComprobante) {
		this.listTemplateTipoComprobante = listTemplateTipoComprobante;
	}

	public List<PlanCuentaBancaria> getListPlanCuentaBancaria() {
		return listPlanCuentaBancaria;
	}

	public void setListPlanCuentaBancaria(List<PlanCuentaBancaria> listPlanCuentaBancaria) {
		this.listPlanCuentaBancaria = listPlanCuentaBancaria;
	}

	public String getTextoAutoCompleteCuentaBancaria() {
		return textoAutoCompleteCuentaBancaria;
	}

	public void setTextoAutoCompleteCuentaBancaria(
			String textoAutoCompleteCuentaBancaria) {
		this.textoAutoCompleteCuentaBancaria = textoAutoCompleteCuentaBancaria;
	}

	public PlanCuentaBancaria getBusquedaPlanCuentaBancaria() {
		return busquedaPlanCuentaBancaria;
	}

	public void setBusquedaPlanCuentaBancaria(PlanCuentaBancaria busquedaPlanCuentaBancaria) {
		this.busquedaPlanCuentaBancaria = busquedaPlanCuentaBancaria;
	}

	public Egreso getEgreso() {
		return egreso;
	}

	public void setEgreso(Egreso egreso) {
		this.egreso = egreso;
	}

	public String getUrlComprobante() {
		return urlComprobante;
	}

	public void setUrlComprobante(String urlComprobante) {
		this.urlComprobante = urlComprobante;
	}

	public String getNombreCuentaBancaria() {
		return nombreCuentaBancaria;
	}

	public void setNombreCuentaBancaria(String nombreCuentaBancaria) {
		this.nombreCuentaBancaria = nombreCuentaBancaria;
		buscarCuentaBancariaByLocal(nombreCuentaBancaria);
	}

	public Date getFechaActual() {
		return fechaActual;
	}

	public void setFechaActual(Date fechaActual) {
		this.fechaActual = fechaActual;
	}

	public Date getFechaMinima() {
		return fechaMinima;
	}

	public void setFechaMinima(Date fechaMinima) {
		this.fechaMinima = fechaMinima;
	}

	public Compra getNewCompra() {
		return newCompra;
	}

	public void setNewCompra(Compra newCompra) {
		this.newCompra = newCompra;
	}

	public Sucursal getSelectedSucursal() {
		return selectedSucursal;
	}

	public void setSelectedSucursal(Sucursal selectedSucursal) {
		this.selectedSucursal = selectedSucursal;
	}

	public List<GrupoImpuesto> getListGrupoImpuesto() {
		return listGrupoImpuesto;
	}

	public void setListGrupoImpuesto(List<GrupoImpuesto> listGrupoImpuesto) {
		this.listGrupoImpuesto = listGrupoImpuesto;
	}

	public String getNombreGrupoImpuesto() {
		return nombreGrupoImpuesto;
	}

	public void setNombreGrupoImpuesto(String nombreGrupoImpuesto) {
		this.nombreGrupoImpuesto = nombreGrupoImpuesto;
		if(! nombreGrupoImpuesto.equals("NINGUNO")){
			agregoGrupoImpuesto = true;
			selectedGrupoImpuesto = obtenerGrupoImpuestoByLocal(nombreGrupoImpuesto);
		}else{
			agregoGrupoImpuesto = false;
		}
	}

	public String getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(String numeroFactura) {
		this.numeroFactura = numeroFactura;
	}

	public Dosificacion getDosificacion() {
		return dosificacion;
	}

	public void setDosificacion(Dosificacion dosificacion) {
		this.dosificacion = dosificacion;
	}

	public boolean isSinPlanCuenta() {
		return sinPlanCuenta;
	}

	public void setSinPlanCuenta(boolean sinPlanCuenta) {
		this.sinPlanCuenta = sinPlanCuenta;
	}

	public boolean isSinDosificacionActiva() {
		return sinDosificacionActiva;
	}

	public void setSinDosificacionActiva(boolean sinDosificacionActiva) {
		this.sinDosificacionActiva = sinDosificacionActiva;
	}

	public boolean isSinSubCentroCosto() {
		return sinSubCentroCosto;
	}

	public void setSinSubCentroCosto(boolean sinSubCentroCosto) {
		this.sinSubCentroCosto = sinSubCentroCosto;
	}

	public ParametroEmpresa getParametroEmpresa() {
		return parametroEmpresa;
	}

	public void setParametroEmpresa(ParametroEmpresa parametroEmpresa) {
		this.parametroEmpresa = parametroEmpresa;
	}

	public List<Integer> getListDatosMonto() {
		return listDatosMonto;
	}

	public void setListDatosMonto(List<Integer> listDatosMonto) {
		this.listDatosMonto = listDatosMonto;
	}

	public Integer getIdComprobante() {
		log.info("getIdComprobante()");
		return idComprobante;
	}

	public void setIdComprobante(Integer idComprobante) {
		this.idComprobante = idComprobante;
	}

	public boolean isHabilitarHaber() {
		return habilitarHaber;
	}

	public void setHabilitarHaber(boolean habilitarHaber) {
		this.habilitarHaber = habilitarHaber;
	}

	public boolean isHabilitarDebe() {
		return habilitarDebe;
	}

	public void setHabilitarDebe(boolean habilitarDebe) {
		this.habilitarDebe = habilitarDebe;
	}

	public Double[] getArrayMonto() {
		return arrayMonto;
	}

	public void setArrayMonto(Double[] arrayMonto) {
		this.arrayMonto = arrayMonto;
	}

	public Double getItemArray(int i){
		return arrayMonto[i];
	}

	public String getNumeroCheque() {
		return numeroCheque;
	}

	public void setNumeroCheque(String numeroCheque) {
		this.numeroCheque = numeroCheque;
	}
}
