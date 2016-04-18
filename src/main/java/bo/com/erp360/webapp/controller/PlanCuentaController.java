package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
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
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.data.NivelRepository;
import bo.com.erp360.webapp.data.ParametroEmpresaRepository;
import bo.com.erp360.webapp.data.TipoCuentaRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.Nivel;
import bo.com.erp360.webapp.model.ParametroEmpresa;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.TipoCuenta;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.PlanCuentaRegistration;
import bo.com.erp360.webapp.util.ApplicationMain;
import bo.com.erp360.webapp.util.EDPlanCuenta;
import bo.com.erp360.webapp.util.PlanCuentaUtil;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "planCuentaController")
@ConversationScoped
public class PlanCuentaController implements Serializable {

	private static final long serialVersionUID = 4163720388986378680L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private PlanCuentaRegistration planCuentaRegistration;

	@Inject
	private NivelRepository nivelRepository;

	@Inject
	private MonedaRepository monedaRepository;

	@Inject
	private TipoCuentaRepository tipoCuentaRepository;

	@Inject
	private ParametroEmpresaRepository parametroEmpresaRepository;

	@Inject
	private ApplicationMain applicationMain;

	private Logger log = Logger.getLogger(this.getClass());

	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private PlanCuenta selectedPlanCuenta;
	private PlanCuenta newPlanCuenta;
	private MonedaEmpresa monedaEmpresa;

	private List<Usuario> listUsuario = new ArrayList<Usuario>();
	private String[] arrayClase= {"CONTROL","AUXILIAR"};
	private List<MonedaEmpresa> listMonedaEmpresa = new ArrayList<MonedaEmpresa>();
	private List<Integer> listTamanio = new ArrayList<Integer>();
	private List<TipoCuenta> listTipoCuenta = new ArrayList<>();

	private String tituloPanel = "Registrar PlanCuenta";
	private String tituloHeaderDialog2 = "NUEVA CUENTA";
	private String nombreMoneda = "";
	private String nombreClase = "CONTROL";
	private String filterByCuenta = "";
	private String nombreUsuario; 

	//treeNode
	private TreeNode root;
	private TreeNode selectedNode;

	//estados
	private boolean nuevo = true;

	private boolean stateButtonGroupHeader = false;
	private boolean stateButtonGroupHeaderModificar = false;
	private boolean stateButtonGroupHeaderCancelar = false;
	private boolean stateButtonGroupHeaderEliminar = false;

	private boolean stateInputTextCodigo = true;
	private boolean stateInputTextCuenta = true;
	private boolean stateOneMenuClase ;
	private boolean stateOnMenuMoneda ;
	private boolean stateButtonRegistrar = true;
	private boolean stateButtonAgregar = false;
	private boolean stateButtonCancelar = false;
	private boolean stateButtonModificar = false;
	private boolean stateExpandingPlanCuenta = true;
	private boolean permitirUfv = true;

	//plan de cuenta
	private List<PlanCuenta> listPlanCuentaGeneral = new ArrayList<PlanCuenta>();

	//configuracion de estructura de cuenta
	private ParametroEmpresa parametroEmpresa;
	private int tamanioDigito;
	private int nivelSeleccionado;

	@PostConstruct
	public void initNewPlanCuenta() {

		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		selectedPlanCuenta = new PlanCuenta();
		newPlanCuenta = new PlanCuenta();
		// tituloPanel
		tituloPanel = "Registrar PlanCuenta";
		listMonedaEmpresa = monedaRepository.findMonedaEmpresaAllByEmpresa(empresaLogin);
		monedaEmpresa = listMonedaEmpresa.get(0);
		nombreMoneda = monedaEmpresa.getMoneda().getNombre();

		parametroEmpresa = parametroEmpresaRepository.findByEmpresa(empresaLogin);
		cargarParametroEmpresa();

		loadValuesDefault();
	}

	private void loadValuesDefault(){
		//estados
		nuevo = true;

		stateButtonGroupHeaderEliminar = false;
		stateButtonGroupHeaderCancelar = false;
		stateButtonGroupHeaderModificar = false;
		stateButtonGroupHeader = false;
		stateInputTextCodigo = true;
		stateInputTextCuenta = true;
		stateOneMenuClase = true;
		stateOnMenuMoneda = false;
		stateButtonRegistrar = true;
		stateButtonAgregar = false;
		stateButtonCancelar = false;
		stateButtonModificar = false;
		stateExpandingPlanCuenta = true;
		permitirUfv = true;

		tamanioDigito = 1;
		nivelSeleccionado = 1;

		newPlanCuenta = new PlanCuenta();
		root = new DefaultTreeNode("Root", null);
		listPlanCuentaGeneral = applicationMain.findAllActivoByEmpresa(empresaLogin);
		listTipoCuenta = tipoCuentaRepository.findAllByEmpresaGestion(empresaLogin, gestionLogin);
		cargarNodos();
		collapsarORexpandir(root,true);
		stateExpandingPlanCuenta = true;
	}

	private void cargarParametroEmpresa(){
		String codigo = "";
		codigo = parametroEmpresa.getCodificacionEtandar();
		int anterior = 0;
		for(int i=0;i<codigo.length();i++){
			String letra = String.valueOf(codigo.charAt(i));
			if(letra.equals(".")){
				String numeroString = codigo.substring(anterior, i);
				int numero = 1;
				for(int j=1 ; j < numeroString.length(); j++){
					numero = numero + 1;
				}
				listTamanio.add(numero);
				anterior = i + 1;
			}
		}

		String numeroString = codigo.substring(anterior, codigo.length());
		int numero = 1;
		for(int j=1 ; j < numeroString.length(); j++){
			numero = numero + 1;
		}
		listTamanio.add(numero);
	}

	private  void collapsarORexpandir(TreeNode n, boolean option) {
		if(n.getChildren().size() == 0) {
			n.setSelected(false);
		}
		else {
			for(TreeNode s: n.getChildren()) {
				collapsarORexpandir(s, option);
			}
			n.setExpanded(option);
			n.setSelected(false);
		}
	}

	private List<PlanCuenta> findCuentasRootByLocal(){
		List<PlanCuenta>  listPlanCuentaAux = new ArrayList<PlanCuenta>();
		for(PlanCuenta pc: listPlanCuentaGeneral){
			if(pc.getPlanCuentaPadre()==null){
				listPlanCuentaAux.add(pc);
			}
		}
		return listPlanCuentaAux;
	}

	public void cargarNodos(){
		List<PlanCuenta> listPlanCuentaRoot = findCuentasRootByLocal();
		loadTreeNode(root, listPlanCuentaRoot);
	}

	//--------------------------- busqueda de cuentas -------------------------------

	private void buscarNodos(String nombre){
		List<PlanCuenta> listPlanCuentaCoincidencias2 = obtenerPlanCuentaCoincidencias(nombre);
		List<PlanCuenta> listPlanCuentaUltimoNivel = obtenerPlanCuentaUltimoNivel(listPlanCuentaCoincidencias2);
		listPlanCuentaCoincidencias = listPlanCuentaUltimoNivel;
	}

	private List<PlanCuenta> listPlanCuentaCoincidencias = new ArrayList<PlanCuenta>();
	private boolean mostrarTableBusqueda = true;

	private List<PlanCuenta> obtenerPlanCuentaCoincidencias(String query){
		String upperQuery = query.toUpperCase();
		List<PlanCuenta> list = new ArrayList<PlanCuenta>();
		for(PlanCuenta pc: listPlanCuentaGeneral){
			if(pc.getDescripcion().toUpperCase().startsWith(upperQuery)){
				list.add(pc);
			}
		}
		return list;
	}

	private List<PlanCuenta> obtenerPlanCuentaUltimoNivel(List<PlanCuenta> list){
		List<PlanCuenta> listAux = new ArrayList<PlanCuenta>();
		for(PlanCuenta pc: list){
			if(pc.getNivel().getNivel() == parametroEmpresa.getNivelMaximo()){
				listAux.add(pc);
			}
		}
		return listAux;
	}

	// -----------------------------------------------------------------------------------------------


	/**
	 * method recursivo que carga el plan de cuenta
	 * @param root
	 * @param listPlanCuenta
	 */
	private void loadTreeNode(TreeNode root, List<PlanCuenta> listPlanCuenta){
		if(listPlanCuenta.size()>0){
			for(PlanCuenta pc : listPlanCuenta){
				String moneda = pc.getMonedaEmpresa()!=null?pc.getMonedaEmpresa().getMoneda().getNombre():"";
				TreeNode tn = new DefaultTreeNode(new EDPlanCuenta(pc.getId() ,pc.getCodigo(), pc.getDescripcion(), pc.getClase(),moneda,pc),root);
				loadTreeNode(tn, obtenerHijas(pc));
			}
		}
	}

	private List<PlanCuenta> obtenerHijas(PlanCuenta padre){
		return obtenerHijasByLocal( padre);
	}

	private List<PlanCuenta> obtenerHijasByLocal(PlanCuenta padre){
		List<PlanCuenta>  listPlanCuentaAux = new ArrayList<PlanCuenta>();
		for(PlanCuenta pc: listPlanCuentaGeneral){
			if(pc.getPlanCuentaPadre()!=null){
				if(pc.getPlanCuentaPadre().equals(padre)){
					listPlanCuentaAux.add(pc);
				}
			}
		}
		return listPlanCuentaAux;
	}

	public void expanding(){
		collapsarORexpandir(root, stateExpandingPlanCuenta);
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

	public void onRowSelectPlanCuenta(SelectEvent event) {
		
		//cargando parametros de codigo y nivel de cuenta
		nivelSeleccionado = selectedPlanCuenta.getNivel().getNivel() ;// cuenta nivel =1  nivel_maximo = 2
		log.info("nivelSeleccionado = "+nivelSeleccionado);


		nuevo = false;
		stateButtonGroupHeaderEliminar =  nivelSeleccionado!=1?true:false;
		stateButtonGroupHeaderCancelar = true;
		stateButtonGroupHeaderModificar = true ;
		stateButtonGroupHeader = true;

		stateOneMenuClase = false;
		stateOnMenuMoneda = false;
		stateInputTextCodigo = false;
		stateInputTextCuenta = true;
		stateButtonAgregar = false;
		stateButtonRegistrar = false;
		stateButtonCancelar = true;
		stateButtonModificar = false;
		//verificar que cuando este en la ultima cuenta auxiliar ya no le permita agregar mas cuentas
		if( nivelSeleccionado >= parametroEmpresa.getNivelMaximo()){
			stateButtonGroupHeader = false;
			selectedNode = null;
		}else{
			tamanioDigito = listTamanio.get(nivelSeleccionado);
		}
		//--
		nombreClase = selectedPlanCuenta.getClase();
		if(selectedPlanCuenta.getClase().equals("AUXILIAR")){
			nombreMoneda = selectedPlanCuenta.getMonedaEmpresa().getMoneda().getNombre();
			monedaEmpresa = buscarMonedaEmpresaByLocal(nombreMoneda);
			stateOnMenuMoneda = true;
			stateOneMenuClase = true;
			permitirUfv = selectedPlanCuenta.getUfv().equals("SI")?true:false;
		}
		
		
	}

	// id codigo  descripcion clase tipoCuenta planCuentaPadre moneda tipoRegistro correlativo1 correlativo2 empresa nivel fecha estado usuarioRegistro
	public void agregarSubCuenta(){
		try {
			log.info("Ingreso a agegarSubCuenta cuenta: "+newPlanCuenta.getDescripcion()+" | nombreClase: "+nombreClase);
			if(nombreClase.equals("AUXILIAR")){
				newPlanCuenta.setMonedaEmpresa(monedaEmpresa);
				newPlanCuenta.setUfv(permitirUfv?"SI":"NO");
			}else{
				newPlanCuenta.setMonedaEmpresa(null);
				newPlanCuenta.setUfv("NO");
			}
			String newCodigo = PlanCuentaUtil.llenarDelanteConCeroCodificacion(newPlanCuenta.getCodigo(),tamanioDigito);
			String parteCodigoAuxiliarPadre = obtenerParteCodigoAuxiliar(selectedPlanCuenta.getCodigoAuxiliar(),selectedPlanCuenta.getNivel());
			String newCodigoAuxiliar = PlanCuentaUtil.cargarCodificacion(parteCodigoAuxiliarPadre,newPlanCuenta.getCodigo(),tamanioDigito,listTamanio);

			log.info("-----newCodigo:"+newCodigo+" | newCodigoAuxiliar:"+newCodigoAuxiliar+"-----");

			newPlanCuenta.setCodigo(newCodigo);
			newPlanCuenta.setCodigoAuxiliar(newCodigoAuxiliar);
			newPlanCuenta.setClase(nombreClase);
			newPlanCuenta.setEmpresa(empresaLogin);
			newPlanCuenta.setUsuarioRegistro(nombreUsuario);
			newPlanCuenta.setPlanCuentaPadre(selectedPlanCuenta);
			newPlanCuenta.setFecha(new Date());
			newPlanCuenta.setEstado("AC");
			newPlanCuenta.setTipoCuenta(selectedPlanCuenta.getTipoCuenta());
			Nivel nivel = nivelRepository.findByNivelEmpresa(nivelSeleccionado + 1 ,empresaLogin);
			newPlanCuenta.setNivel(nivel);
			planCuentaRegistration.create(newPlanCuenta);
			int nivelMostrar = nivelSeleccionado + 1;
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cuenta Registrada", newPlanCuenta.getDescripcion()+" NIVEL "+nivelMostrar);
			facesContext.addMessage(null, m);

			//ocultar dialog 
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgGestionCuenta').hide();");

			loadValuesDefault();
		} catch (Exception e) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	private TipoCuenta findTipoCuentaByLocal(String codigo){
		int primerDigito = Integer.valueOf(codigo.substring(0,1));
		for(TipoCuenta tc: listTipoCuenta){
			if(tc.getDigito()==primerDigito){
				return tc;
			}
		}
		return null;
	}

	public void registrarCuenta() {
		try {
			log.info("Ingreso a registrarPlanCuenta: ");
			if(nombreClase.equals("AUXILIAR")){
				newPlanCuenta.setMonedaEmpresa(monedaEmpresa);
				newPlanCuenta.setUfv(permitirUfv?"SI":"NO");
			}else{
				newPlanCuenta.setMonedaEmpresa(null);
				newPlanCuenta.setUfv("NO");
			}
			newPlanCuenta.setCodigo(PlanCuentaUtil.llenarDelanteConCeroCodificacion(newPlanCuenta.getCodigo(),tamanioDigito));
			newPlanCuenta.setCodigoAuxiliar(PlanCuentaUtil.llenarDespuesConCeroCodificacion(newPlanCuenta.getCodigo(),tamanioDigito,listTamanio));
			newPlanCuenta.setClase(nombreClase);
			newPlanCuenta.setUsuarioRegistro(nombreUsuario);
			newPlanCuenta.setFecha(new Date());
			newPlanCuenta.setEstado("AC");
			newPlanCuenta.setEmpresa(empresaLogin);
			Nivel nivel = nivelRepository.findByNivelEmpresa(nivelSeleccionado ,empresaLogin);
			newPlanCuenta.setNivel(nivel);
			newPlanCuenta.setPlanCuentaPadre(null);
			newPlanCuenta.setTipoCuenta(findTipoCuentaByLocal(newPlanCuenta.getCodigo()));
			planCuentaRegistration.create(newPlanCuenta);

			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cuenta Registrada !", newPlanCuenta.getDescripcion() +" Nivel "+nivelSeleccionado);
			facesContext.addMessage(null, m);

			//ocultar dialog 
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgGestionCuenta').hide();");

			loadValuesDefault();
		} catch (Exception e) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	private String obtenerParteCodigoAuxiliar(String codigoAuxiliar, Nivel nivel) {
		int nroDigitosTotal =PlanCuentaUtil.obtenerTamanioHastaNIvel(nivel.getNivel(),listTamanio);
		String cadena = "";
		cadena = codigoAuxiliar.substring(0, nroDigitosTotal );
		return  cadena;
	}


	public void modificarCuenta() {
		try {
			log.info("Ingreso a modificarPlanCuenta: "
					+ newPlanCuenta.getId());
			if(nombreClase.equals("AUXILIAR")){
				newPlanCuenta.setMonedaEmpresa(monedaEmpresa);
				newPlanCuenta.setUfv(permitirUfv?"SI":"NO");
			}
			planCuentaRegistration.update(newPlanCuenta);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cuenta Modificada!", newPlanCuenta.getDescripcion());
			facesContext.addMessage(null, m);
			loadValuesDefault();
		} catch (Exception e) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "Modificación Incorrecta.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminarPlanCuenta() {
		try {
			log.info("Ingreso a eliminarPlanCuenta: "
					+ newPlanCuenta.getId());
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cuenta Borrada!", newPlanCuenta.getDescripcion());
			facesContext.addMessage(null, m);
			loadValuesDefault();

		} catch (Exception e) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "Borrado Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void actualizarFormReg(){
		stateInputTextCodigo = true; 
		stateInputTextCuenta = true;
		//		stateOneMenuClase = false;
		//		stateOnMenuMoneda = false;
		stateButtonRegistrar = true;
		stateButtonAgregar = false;
		stateButtonCancelar = false;
		stateButtonModificar = false;
		newPlanCuenta = new PlanCuenta();
	}

	private PlanCuenta obtenerPlanCuentaByLocal(String descripcion){
		log.info("listPlanCuentaGeneral.size() : "+listPlanCuentaGeneral.size()); 
		for(PlanCuenta pc: listPlanCuentaGeneral){
			if(pc.getDescripcion().equals(descripcion)){
				return pc;
			}
		}
		return null;
	}

	public void onNodeSelect(NodeSelectEvent event) {
		String descripcion =((EDPlanCuenta) event.getTreeNode().getData()).getCuenta().toString();
		selectedPlanCuenta = obtenerPlanCuentaByLocal(descripcion);//planCuentaRepository.findByDescripcionAndEmpresa2(descripcion, empresaLogin);

		//cargando parametros de codigo y nivel de cuenta
		nivelSeleccionado = selectedPlanCuenta.getNivel().getNivel() ;// cuenta nivel =1  nivel_maximo = 2
		log.info("nivelSeleccionado = "+nivelSeleccionado);


		nuevo = false;
		stateButtonGroupHeaderEliminar =  nivelSeleccionado!=1?true:false;
		stateButtonGroupHeaderCancelar = true;
		stateButtonGroupHeaderModificar = true ;
		stateButtonGroupHeader = true;

		stateOneMenuClase = false;
		stateOnMenuMoneda = false;
		stateInputTextCodigo = false;
		stateInputTextCuenta = true;
		stateButtonAgregar = false;
		stateButtonRegistrar = false;
		stateButtonCancelar = true;
		stateButtonModificar = false;
		//verificar que cuando este en la ultima cuenta auxiliar ya no le permita agregar mas cuentas
		if( nivelSeleccionado >= parametroEmpresa.getNivelMaximo()){
			stateButtonGroupHeader = false;
			selectedNode = null;
		}else{
			tamanioDigito = listTamanio.get(nivelSeleccionado);
		}
		//--
		nombreClase = selectedPlanCuenta.getClase();
		if(selectedPlanCuenta.getClase().equals("AUXILIAR")){
			nombreMoneda = selectedPlanCuenta.getMonedaEmpresa().getMoneda().getNombre();
			monedaEmpresa = buscarMonedaEmpresaByLocal(nombreMoneda);
			stateOnMenuMoneda = true;
			stateOneMenuClase = true;
			permitirUfv = selectedPlanCuenta.getUfv().equals("SI")?true:false;
		}
	}

	public void addNode() { 
		tituloHeaderDialog2 = "AGREGAR SUBCUENTA";
		newPlanCuenta = new PlanCuenta();
		stateInputTextCodigo = false; 
		stateInputTextCuenta = true;
		stateOneMenuClase = true;
		stateButtonAgregar = true;
		stateButtonRegistrar = false;
		stateButtonCancelar = true;
		stateButtonModificar = false;
		//		stateOnMenuMoneda = false;
	}

	public void updateNode() {
		tituloHeaderDialog2 = "MODIFICAR CUENTA";
		newPlanCuenta = selectedPlanCuenta;
		stateInputTextCodigo = false; 
		stateInputTextCuenta = true;
		stateButtonAgregar = false;
		stateButtonRegistrar = false;
		stateButtonCancelar = true;
		stateButtonModificar = true;
	}

	public void deleteNode() {
		tituloHeaderDialog2 = "ELIMINAR CUENTA";
		EDPlanCuenta aux = (EDPlanCuenta) selectedNode.getData();
		selectedNode.getChildren().clear();
		selectedNode.getParent().getChildren().remove(selectedNode);
		selectedNode.setParent(null);
		selectedNode = null;
		PlanCuenta pc = new PlanCuenta(); 
		//preguntar:
		//si es una clase de control -> lanzarle un dialogo que se borrara todos sus cuentas asociadas
		if(aux.getClase().equals("CONTROL")){
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgEliminarCuentaControl').show();");
		}else{//AUXILIAR
			try{
				pc = aux.getPc();
				pc.setEstado("RM");
				planCuentaRegistration.update(pc);
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Cuenta eliminada!", pc.getDescripcion());
				facesContext.addMessage(null, m);
			}catch(Exception e){

			}
		}
		loadValuesDefault();
	}

	public void displaySelectedSingle() {
		if(selectedNode != null) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected", selectedNode.getData().toString());
			FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}

	public void buscarCuentasByFilter(){
		log.info("cuenta: "+filterByCuenta);
		buscarNodos(filterByCuenta);
		mostrarTableBusqueda = false;
		resetearFitrosTabla("form:dataTableQuery");
	}
	
	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void buttonCancelar(){
		nuevo = true;
		stateButtonGroupHeaderEliminar = false;
		stateButtonGroupHeaderCancelar = false;
		stateButtonGroupHeaderModificar = false;
		stateButtonGroupHeader = false;
		stateButtonAgregar = false;
		stateButtonRegistrar = false;
		stateButtonCancelar = false;
		stateButtonModificar = false;
		nivelSeleccionado = 1;
		tamanioDigito = 1;
		selectedNode = null;
	}

	private MonedaEmpresa buscarMonedaEmpresaByLocal(String nombreMonedaEmpresa){
		for(MonedaEmpresa me: listMonedaEmpresa){
			if(nombreMonedaEmpresa.equals(me.getMoneda().getNombre())){
				return me;
			}
		}
		return null;
	}
	
	public void cancelarBusqueda(){
		filterByCuenta = "";
		mostrarTableBusqueda = true;
		buttonCancelar();
	}

	// ------------  get and set -------------------
	public String getTituloPanel() {
		return tituloPanel;
	}

	public void setTituloPanel(String tituloPanel) {
		this.tituloPanel = tituloPanel;
	}

	public PlanCuenta getSelectedPlanCuenta() {
		return selectedPlanCuenta;
	}

	public void setSelectedPlanCuenta(PlanCuenta selectedPlanCuenta) {
		this.selectedPlanCuenta = selectedPlanCuenta;
	}

	public PlanCuenta getNewPlanCuenta() {
		return newPlanCuenta;
	}

	public void setNewPlanCuenta(PlanCuenta newPlanCuenta) {
		this.newPlanCuenta = newPlanCuenta;
	}

	public List<Usuario> getListUsuario() {
		return listUsuario;
	}

	public void setListUsuario(List<Usuario> listUsuario) {
		this.listUsuario = listUsuario;
	}

	public String getTest(){
		return "test";
	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public String getNombreMoneda() {
		return nombreMoneda;
	}

	public void setNombreMoneda(String nombreMoneda) {
		this.nombreMoneda = nombreMoneda;
		monedaEmpresa = buscarMonedaEmpresaByLocal(this.nombreMoneda);
	}

	public String getNombreClase() {
		return nombreClase;
	}

	public void setNombreClase(String nombreClase) {
		this.nombreClase = nombreClase;
		stateOnMenuMoneda = nombreClase.equals("AUXILIAR") ?true:false;
	}

	public String[] getArrayClase() {
		return arrayClase;
	}

	public void setArrayClase(String[] arrayClase) {
		this.arrayClase = arrayClase;
	}

	public boolean isStateInputTextCodigo() {
		return stateInputTextCodigo;
	}

	public void setStateInputTextCodigo(boolean stateInputTextCodigo) {
		this.stateInputTextCodigo = stateInputTextCodigo;
	}

	public boolean isStateInputTextCuenta() {
		return stateInputTextCuenta;
	}

	public void setStateInputTextCuenta(boolean stateInputTextCuenta) {
		this.stateInputTextCuenta = stateInputTextCuenta;
	}

	public boolean isStateOneMenuClase() {
		return stateOneMenuClase;
	}

	public void setStateOneMenuClase(boolean stateOneMenuClase) {
		this.stateOneMenuClase = stateOneMenuClase;
	}

	public boolean isStateOnMenuMoneda() {
		return stateOnMenuMoneda;
	}

	public void setStateOnMenuMoneda(boolean stateOnMenuMoneda) {
		this.stateOnMenuMoneda = stateOnMenuMoneda;
	}

	public boolean isStateButtonCancelar() {
		return stateButtonCancelar;
	}

	public void setStateButtonCancelar(boolean stateButtonCancelar) {
		this.stateButtonCancelar = stateButtonCancelar;
	}

	public boolean isStateButtonRegistrar() {
		return stateButtonRegistrar;
	}

	public void setStateButtonRegistrar(boolean stateButtonRegistrar) {
		this.stateButtonRegistrar = stateButtonRegistrar;
	}

	public boolean isStateButtonModificar() {
		return stateButtonModificar;
	}

	public void setStateButtonModificar(boolean stateButtonModificar) {
		this.stateButtonModificar = stateButtonModificar;
	}

	public boolean isStateButtonAgregar() {
		return stateButtonAgregar;
	}

	public void setStateButtonAgregar(boolean stateButtonAgregar) {
		this.stateButtonAgregar = stateButtonAgregar;
	}

	public String getTituloHeaderDialog2() {
		return tituloHeaderDialog2;
	}

	public void setTituloHeaderDialog2(String tituloHeaderDialog2) {
		this.tituloHeaderDialog2 = tituloHeaderDialog2;
	}

	public boolean isStateButtonGroupHeader() {
		return stateButtonGroupHeader;
	}

	public void setStateButtonGroupHeader(boolean stateButtonGroupHeader) {
		this.stateButtonGroupHeader = stateButtonGroupHeader;
	}

	public boolean isStateExpandingPlanCuenta() {
		return stateExpandingPlanCuenta;
	}

	public void setStateExpandingPlanCuenta(boolean stateExpandingPlanCuenta) {
		this.stateExpandingPlanCuenta = stateExpandingPlanCuenta;
	}

	public String getFilterByCuenta() {
		return filterByCuenta;
	}

	public void setFilterByCuenta(String filterByCuenta) {
		this.filterByCuenta = filterByCuenta;
	}

	public boolean isPermitirUfv() {
		return permitirUfv;
	}

	public void setPermitirUfv(boolean permitirUfv) {
		this.permitirUfv = permitirUfv;
	}

	public boolean isNuevo() {
		return nuevo;
	}

	public void setNuevo(boolean nuevo) {
		this.nuevo = nuevo;
	}

	public List<MonedaEmpresa> getListMonedaEmpresa() {
		return listMonedaEmpresa;
	}

	public void setListMonedaEmpresa(List<MonedaEmpresa> listMonedaEmpresa) {
		this.listMonedaEmpresa = listMonedaEmpresa;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public ParametroEmpresa getParametroEmpresa() {
		return parametroEmpresa;
	}

	public void setParametroEmpresa(ParametroEmpresa parametroEmpresa) {
		this.parametroEmpresa = parametroEmpresa;
	}

	public int getTamanioDigito() {
		return tamanioDigito;
	}

	public void setTamanioDigito(int tamanioDigito) {
		this.tamanioDigito = tamanioDigito;
	}

	public int getNivelSeleccionado() {
		return nivelSeleccionado;
	}

	public void setNivelSeleccionado(int nivelSeleccionado) {
		this.nivelSeleccionado = nivelSeleccionado;
	}

	public boolean isStateButtonGroupHeaderModificar() {
		return stateButtonGroupHeaderModificar;
	}

	public void setStateButtonGroupHeaderModificar(
			boolean stateButtonGroupHeaderModificar) {
		this.stateButtonGroupHeaderModificar = stateButtonGroupHeaderModificar;
	}

	public boolean isStateButtonGroupHeaderCancelar() {
		return stateButtonGroupHeaderCancelar;
	}

	public void setStateButtonGroupHeaderCancelar(
			boolean stateButtonGroupHeaderCancelar) {
		this.stateButtonGroupHeaderCancelar = stateButtonGroupHeaderCancelar;
	}

	public boolean isStateButtonGroupHeaderEliminar() {
		return stateButtonGroupHeaderEliminar;
	}

	public void setStateButtonGroupHeaderEliminar(
			boolean stateButtonGroupHeaderEliminar) {
		this.stateButtonGroupHeaderEliminar = stateButtonGroupHeaderEliminar;
	}

	public List<PlanCuenta> getListPlanCuentaCoincidencias() {
		return listPlanCuentaCoincidencias;
	}

	public void setListPlanCuentaCoincidencias(
			List<PlanCuenta> listPlanCuentaCoincidencias) {
		this.listPlanCuentaCoincidencias = listPlanCuentaCoincidencias;
	}

	public boolean isMostrarTableBusqueda() {
		return mostrarTableBusqueda;
	}

	public void setMostrarTableBusqueda(boolean mostrarTableBusqueda) {
		this.mostrarTableBusqueda = mostrarTableBusqueda;
	}

}
