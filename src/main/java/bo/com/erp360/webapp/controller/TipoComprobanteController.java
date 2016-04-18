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
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.PlanCuentaRepository;
import bo.com.erp360.webapp.data.TemplateTipoComprobanteRepository;
import bo.com.erp360.webapp.data.TipoComprobanteRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.TemplateTipoComprobante;
import bo.com.erp360.webapp.model.TipoComprobante;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.TemplateTipoComprobanteRegistration;
import bo.com.erp360.webapp.service.TipoComprobanteRegistration;
import bo.com.erp360.webapp.util.EDPlanCuenta;
import bo.com.erp360.webapp.util.EDTemplatePlanCuenta;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "tipoComprobanteController")
@ConversationScoped
public class TipoComprobanteController implements Serializable {

	private static final long serialVersionUID = -7819149623543804669L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private PlanCuentaRepository planCuentaRepository;

	@Inject
	private TipoComprobanteRepository tipoComprobanteRepository;

	@Inject
	private TipoComprobanteRegistration tipoComprobanteRegistration;

	@Inject
	private TemplateTipoComprobanteRegistration templateTipoComprobanteRegistration;

	@Inject
	private TemplateTipoComprobanteRepository templateTipoComprobanteRepository;

	private Logger log = Logger.getLogger(this.getClass());

	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;
	private Usuario usuarioSession;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private String tituloPanel = "Registrar Tipo de Comprobante";
	private String nombreTipoCuenta = "DEBE";//DEBE - HABER
	private String nombreUsuario;

	@Produces
	@Named
	private TipoComprobante newTipoComprobante;
	private TipoComprobante selectedTipoComprobante;
	private PlanCuenta selectedPlanCuenta;
	private EDTemplatePlanCuenta selectedEDTemplatePlanCuenta;
	private TemplateTipoComprobante selectedTemplateTipoComprobante;

	private List<TipoComprobante> listTipoComprobante=  new ArrayList<TipoComprobante>();
	private List<TipoComprobante> listFilterTipoComprobante=  new ArrayList<TipoComprobante>();
	private List<PlanCuenta> listPlanCuenta = new ArrayList<PlanCuenta>();
	private List<EDTemplatePlanCuenta> listEDTemplatePlanCuenta = new ArrayList<EDTemplatePlanCuenta>();
	private List<TemplateTipoComprobante> listTemplateTipoComprobante = new ArrayList<TemplateTipoComprobante>();
	private List<PlanCuenta> listFilterPlanCuenta = new ArrayList<PlanCuenta>();
	private List<Usuario> listUsuario = new ArrayList<Usuario>();
	private String[] arrayTipoCuenta = {"DEBE","HABER"};

	//treenode
	private TreeNode selectedNode;

	//estados
	private boolean cuentaSeleccionada = false;
	private boolean modificar = false;
	private boolean modificarCuentaSeleccionada = false;
	private boolean agregarCuentas = false;

	@PostConstruct
	public void initNewTipoComprobante() {

		log.info("init new Tipo de Comprobante");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		usuarioSession = sessionMain.getUsuarioLoggin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		selectedTipoComprobante = new TipoComprobante();
		tituloPanel = "Registrar Tipo de Comprobante";

		newTipoComprobante = new TipoComprobante();

		listTipoComprobante = tipoComprobanteRepository.findAllByEmpresa(empresaLogin);

		modificar = false;
	}

	@Produces
	@Named
	public List<TipoComprobante> getListTipoComprobante() {
		return listTipoComprobante;
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

	public void registrarTipoComprobante() {
		try {
			newTipoComprobante.setEmpresa(empresaLogin);
			tipoComprobanteRegistration.create(newTipoComprobante);

			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"TipoComprobante registrado!", newTipoComprobante.getNombre()+"!");
			facesContext.addMessage(null, m);
			initNewTipoComprobante();

		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Modificado Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void modificarTipoComprobante() {
		try {
			tipoComprobanteRegistration.update(newTipoComprobante);

			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"TipoComprobante Modificada!", newTipoComprobante.getNombre()+"!");
			facesContext.addMessage(null, m);
			initNewTipoComprobante();

		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Modificado Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminarTipoComprobante() {
		try {
			tipoComprobanteRegistration.delete(newTipoComprobante);

			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"TipoComprobante Borrado!", newTipoComprobante.getNombre()+"!");
			facesContext.addMessage(null, m);
			initNewTipoComprobante();

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

	public void onRowSelect(SelectEvent event) {
		newTipoComprobante = selectedTipoComprobante;
		modificar = true;
	}

	//	private void cargarEDTemplatePlanCuenta(List<TemplateTipoComprobante> listTTC ){
	//		listEDTemplatePlanCuenta = new ArrayList<EDTemplatePlanCuenta>();
	//		listPlanCuenta = new ArrayList<PlanCuenta>();
	//		for(TemplateTipoComprobante ttc: listTTC){
	//			EDTemplatePlanCuenta edtpc = new EDTemplatePlanCuenta();
	//			PlanCuenta pcAux = new PlanCuenta();
	//			pcAux = ttc.getPlanCuenta();
	//			edtpc.setClase(pcAux.getClase());
	//			edtpc.setCodigo(pcAux.getCodigo());
	//			edtpc.setCuenta(pcAux.getDescripcion());
	//			edtpc.setTipo(ttc.getTipo());
	//			listEDTemplatePlanCuenta.add(edtpc);
	//			listPlanCuenta.add(pcAux);
	//		}
	//	}

	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void onRowSelectPlanCuenta(SelectEvent event) {
		//FacesMessage msg = new FacesMessage("Empresa Seleccionada", ((Empresa) event.getObject()).getRazonSocial());

		//FacesContext.getCurrentInstance().addMessage(null, msg);
		modificarCuentaSeleccionada = true;

	}

	public void cancelarFormCuentas(){
		setAgregarCuentas(false);
		resetearFitrosTabla("formAgregarCuentas:dataTableEdtTemplatePlanCuenta");
		selectedTipoComprobante = new TipoComprobante();
	}

	public void cancelarEDTemplatePlanCuenta(){
		setModificarCuentaSeleccionada(false);
		//selectedEDTemplatePlanCuenta = new EDTemplatePlanCuenta();
		selectedTemplateTipoComprobante = new TemplateTipoComprobante();
		resetearFitrosTabla("formAgregarCuentas:dataTableEdtTemplatePlanCuenta");
	}

	public void eliminarEDTemplatePlanCuenta(){
		try{
			//TemplateTipoComprobante ttc = templateTipoComprobanteRepository.findById(selectedEDTemplatePlanCuenta.g);
			selectedTemplateTipoComprobante.setEstado("RM");
			selectedTemplateTipoComprobante.setFechaModificacion(new Date());
			templateTipoComprobanteRegistration.update(selectedTemplateTipoComprobante);
			setModificarCuentaSeleccionada(false);
			listTemplateTipoComprobante.remove(selectedTemplateTipoComprobante);
			
			resetearFitrosTabla("formAgregarCuentas:dataTableEdtTemplatePlanCuenta");
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cuenta eliminada!", selectedTemplateTipoComprobante.getPlanCuenta().getDescripcion());
			FacesContext.getCurrentInstance().addMessage(null, message);
			selectedTemplateTipoComprobante = new TemplateTipoComprobante();
			//modificar estados de la cuenta seleccionada en el plan de cuenta
		}catch(Exception e){
			log.info("eliminarEDTemplatePlanCuenta ERROR "+e.getMessage());
		}
	}

	public void agregarCuentaATemplate(){
		try{
			log.info("agregarCuentaATemplate  descripcion : "+selectedPlanCuenta.getDescripcion()+"| selectedTipoComprobante"+selectedTipoComprobante.getNombre());
			TemplateTipoComprobante ttc = new TemplateTipoComprobante();
			ttc.setTipo(nombreTipoCuenta);
			ttc.setPlanCuenta(selectedPlanCuenta);
			ttc.setEstado("AC");
			ttc.setFechaRegistro(new Date());
			ttc.setUsuarioRegistro(nombreUsuario);
			ttc.setTipoComprobante(selectedTipoComprobante);
			ttc = templateTipoComprobanteRegistration.create(ttc);
			listTemplateTipoComprobante.add(ttc);
			resetearFitrosTabla("formAgregarCuentas:dataTableEdtTemplatePlanCuenta");
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cuenta Agregada!", selectedPlanCuenta.getDescripcion());
			FacesContext.getCurrentInstance().addMessage(null, message);
		}catch(Exception e){
			log.info("ERROR "+e.getMessage());
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error al Registrar!", "");
			FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}

	public void agregarCuentasATemplateTipoComprobante(){
		try{
			for(PlanCuenta pc : listPlanCuenta){
				TemplateTipoComprobante tc = new TemplateTipoComprobante();
				tc.setPlanCuenta(pc);
				tc.setTipoComprobante(selectedTipoComprobante);
				templateTipoComprobanteRegistration.create(tc);
			}
			String descripcion = "Cuentas Registradas ";
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro Correcto!", descripcion);
			FacesContext.getCurrentInstance().addMessage(null, message);
		}catch(Exception e){
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro Incorrecto!", "");
			FacesContext.getCurrentInstance().addMessage(null, message);
		}
	}

	public void onNodeSelect(NodeSelectEvent event) {
		String descripcion =((EDPlanCuenta) event.getTreeNode().getData()).getCuenta().toString();
		log.info("descripcion: "+descripcion);
		PlanCuenta pc = (PlanCuenta)planCuentaRepository.findByDescripcionAndEmpresa2(descripcion, empresaLogin);
		selectedPlanCuenta = pc;
		if(selectedPlanCuenta.getClase().equals("AUXILIAR")){
			cuentaSeleccionada = true;
		}else{
			cuentaSeleccionada = false;
		}
	}

	public void agregarOrVerCuentas(){
		setAgregarCuentas(true);
		listTemplateTipoComprobante = new ArrayList<TemplateTipoComprobante>();
		listTemplateTipoComprobante = templateTipoComprobanteRepository.findTemplateTipoComprobanteByTipoComprobanteAndEmpresa(selectedTipoComprobante,empresaLogin);
		modificar = false;
	}

	public void cancelarAgregarCuentas(){
		modificar = false;
		resetearFitrosTabla("formTipoComprobante:dataTableComprobante");
		selectedTipoComprobante = new TipoComprobante();
	}

	// ----------- get and set -------------
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
		System.out.println("cambiarModificar()");
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

	public TipoComprobante getSelectedTipoComprobante() {
		return selectedTipoComprobante;
	}

	public void setSelectedTipoComprobante(TipoComprobante selectedTipoComprobante) {
		this.selectedTipoComprobante = selectedTipoComprobante;
	}

	public boolean isAgregarCuentas() {
		return agregarCuentas;
	}

	public void setAgregarCuentas(boolean agregarCuentas) {
		this.agregarCuentas = agregarCuentas;
	}

	public List<PlanCuenta> getListPlanCuenta() {
		return listPlanCuenta;
	}

	public void setListPlanCuenta(List<PlanCuenta> listPlanCuenta) {
		this.listPlanCuenta = listPlanCuenta;
	}

	public PlanCuenta getSelectedPlanCuenta() {
		return selectedPlanCuenta;
	}

	public void setSelectedPlanCuenta(PlanCuenta selectedPlanCuenta) {
		this.selectedPlanCuenta = selectedPlanCuenta;
	}

	public boolean isCuentaSeleccionada() {
		return cuentaSeleccionada;
	}

	public void setCuentaSeleccionada(boolean cuentaSeleccionada) {
		this.cuentaSeleccionada = cuentaSeleccionada;
		selectedNode = new DefaultTreeNode();
	}

	public boolean isModificarCuentaSeleccionada() {
		return modificarCuentaSeleccionada;
	}

	public void setModificarCuentaSeleccionada(boolean modificarCuentaSeleccionada) {
		this.modificarCuentaSeleccionada = modificarCuentaSeleccionada;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public List<TipoComprobante> getListFilterTipoComprobante() {
		return listFilterTipoComprobante;
	}

	public void setListFilterTipoComprobante(
			List<TipoComprobante> listFilterTipoComprobante) {
		this.listFilterTipoComprobante = listFilterTipoComprobante;
	}

	public List<PlanCuenta> getListFilterPlanCuenta() {
		return listFilterPlanCuenta;
	}

	public void setListFilterPlanCuenta(List<PlanCuenta> listFilterPlanCuenta) {
		this.listFilterPlanCuenta = listFilterPlanCuenta;
	}

	public String[] getArrayTipoCuenta() {
		return arrayTipoCuenta;
	}

	public void setArrayTipoCuenta(String[] arrayTipoCuenta) {
		this.arrayTipoCuenta = arrayTipoCuenta;
	}

	public String getNombreTipoCuenta() {
		return nombreTipoCuenta;
	}

	public void setNombreTipoCuenta(String nombreTipoCuenta) {
		this.nombreTipoCuenta = nombreTipoCuenta;
	}

	public List<EDTemplatePlanCuenta> getListEDTemplatePlanCuenta() {
		return listEDTemplatePlanCuenta;
	}

	public void setListEDTemplatePlanCuenta(List<EDTemplatePlanCuenta> listEDTemplatePlanCuenta) {
		this.listEDTemplatePlanCuenta = listEDTemplatePlanCuenta;
	}

	public EDTemplatePlanCuenta getSelectedEDTemplatePlanCuenta() {
		return selectedEDTemplatePlanCuenta;
	}

	public void setSelectedEDTemplatePlanCuenta(
			EDTemplatePlanCuenta selectedEDTemplatePlanCuenta) {
		this.selectedEDTemplatePlanCuenta = selectedEDTemplatePlanCuenta;
	}

	public List<TemplateTipoComprobante> getListTemplateTipoComprobante() {
		return listTemplateTipoComprobante;
	}

	public void setListTemplateTipoComprobante(
			List<TemplateTipoComprobante> listTemplateTipoComprobante) {
		this.listTemplateTipoComprobante = listTemplateTipoComprobante;
	}

	public TemplateTipoComprobante getSelectedTemplateTipoComprobante() {
		return selectedTemplateTipoComprobante;
	}

	public void setSelectedTemplateTipoComprobante(
			TemplateTipoComprobante selectedTemplateTipoComprobante) {
		this.selectedTemplateTipoComprobante = selectedTemplateTipoComprobante;
	}

}
