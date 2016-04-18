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
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.DetalleGrupoImpuestoRepository;
import bo.com.erp360.webapp.data.GrupoImpuestoRepository;
import bo.com.erp360.webapp.data.PlanCuentaRepository;
import bo.com.erp360.webapp.model.DetalleGrupoImpuesto;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.GrupoImpuesto;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.service.DetalleGrupoImpuestoRegistration;
import bo.com.erp360.webapp.service.GrupoImpuestoRegistration;
import bo.com.erp360.webapp.util.EDGrupoImpuesto;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "grupoImpuestoController")
@ConversationScoped
public class GrupoImpuestoController implements Serializable {
	
	private static final long serialVersionUID = -1355573113798368798L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private GrupoImpuestoRegistration grupoImpuestoRegistration;

	@Inject
	private GrupoImpuestoRepository grupoImpuestoRepository;

	@Inject
	private DetalleGrupoImpuestoRegistration detalleGrupoImpuestoRegistration;

	@Inject
	private DetalleGrupoImpuestoRepository detalleGrupoImpuestoRepository;

	@Inject
	private PlanCuentaRepository planCuentaRepository;

	private Logger log = Logger.getLogger(this.getClass());

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	//estados
	private boolean crear = true;
	private boolean registrar = false;
	private boolean modificar = false;
	private boolean permitirCredito = true;
	private boolean stateExpandingPlanCuenta = true;

	private String nombreEstado="ACTIVO";
	private String nombreUsuario; 
	private String nombreTipoGrupoImpuesto;//debe - haber
	private String tituloPanel = "Registrar GrupoImpuesto";
	private String tipoColumnTable = "col-md-12"; //8
	private String nombrePlanCuenta;

	@Produces
	@Named
	private GrupoImpuesto newGrupoImpuesto;
	private GrupoImpuesto selectedGrupoImpuesto;
	private DetalleGrupoImpuesto detalleGrupoImpuesto;
	private PlanCuenta selectedPlanCuenta;

	private List<DetalleGrupoImpuesto> listDetalleGrupoImpuesto = new ArrayList<DetalleGrupoImpuesto>();
	private List<GrupoImpuesto> listGrupoImpuesto = new ArrayList<GrupoImpuesto>();
	private List<PlanCuenta> listPlanCuenta = new ArrayList<PlanCuenta>();
	private String[] listEstado = {"ACTIVO","INACTIVO"};
	private String[] listTipoGrupoImpuesto = {"DEBE","HABER"};

	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	//treeNode
	private TreeNode root;
	private TreeNode selectedNode;


	@PostConstruct
	public void initNewGrupoImpuesto() {
		log.info(" init new initNewPlanCuentaBancaria");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		tituloPanel = "Plan de Cuenta Bancaria";
		newGrupoImpuesto = new GrupoImpuesto();
		selectedGrupoImpuesto = new GrupoImpuesto();
		detalleGrupoImpuesto = new DetalleGrupoImpuesto();
		root = new DefaultTreeNode("Root", null);
		cargarAlternativo();
		collapsarORexpandir(root,true);
		listPlanCuenta = planCuentaRepository.findAllAuxiliarByEmpresa(empresaLogin,gestionLogin);
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

	public void expanding(){
		collapsarORexpandir(root, stateExpandingPlanCuenta);
	}

	private void cargarAlternativo(){
		listGrupoImpuesto = grupoImpuestoRepository.findActivosByEmpresa(empresaLogin);
		listDetalleGrupoImpuesto = new ArrayList<DetalleGrupoImpuesto>();
		for(GrupoImpuesto gi: listGrupoImpuesto){
			TreeNode tn1 = new DefaultTreeNode(new EDGrupoImpuesto(String.valueOf(gi.getId()), gi.getNombre(), "GRUPO",0,""),root);
			listDetalleGrupoImpuesto = new ArrayList<DetalleGrupoImpuesto>();
			listDetalleGrupoImpuesto = detalleGrupoImpuestoRepository.findAllByGrupoIpuesto(gi);
			for(DetalleGrupoImpuesto dgi : listDetalleGrupoImpuesto){
				TreeNode tn2 = new DefaultTreeNode(new EDGrupoImpuesto(String.valueOf(dgi.getId()), dgi.getPlanCuenta().getDescripcion(), "CUENTA",dgi.getPorcentaje(),gi.getNombre()),tn1);
			}
			if(listDetalleGrupoImpuesto.size()<3){
				TreeNode tn3 = new DefaultTreeNode(new EDGrupoImpuesto("", "AGREGAR NUEVA CUENTA", "NUEVA",0,gi.getNombre()),tn1);
			}
		}
	}

	private PlanCuenta obtenerPlanCuentaByLocal(String nombre){
		for(PlanCuenta pc : listPlanCuenta){
			if(pc.getDescripcion().equals(nombre)){
				return pc;
			}
		}
		return null;
	}

	@Produces
	@Named
	public List<DetalleGrupoImpuesto> getListDetalleGrupoImpuesto() {
		return listDetalleGrupoImpuesto;
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

	public void registrar(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newGrupoImpuesto.setEstado(estado);
			newGrupoImpuesto.setFechaRegistro(new Date());
			newGrupoImpuesto.setUsuarioRegistro(nombreUsuario);
			newGrupoImpuesto.setEmpresa(empresaLogin);
			grupoImpuestoRegistration.create(newGrupoImpuesto);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Grupo Impuesto Registrado!", "");
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			initNewGrupoImpuesto();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void modificar(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newGrupoImpuesto.setEstado(estado);
			newGrupoImpuesto.setFechaRegistro(new Date());
			newGrupoImpuesto.setUsuarioRegistro(nombreUsuario);
			grupoImpuestoRegistration.update(newGrupoImpuesto);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Grupo Impuesto Modificado!", "");
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			initNewGrupoImpuesto();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminar(){
		try{
			newGrupoImpuesto.setEstado("RM");
			grupoImpuestoRegistration.update(newGrupoImpuesto);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Grupo Impuesto Eliminado!", "");
			facesContext.addMessage(null, m);
			initNewGrupoImpuesto();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void actualizarFormReg(){
		crear = true;
		registrar = false;
		modificar = false;
		setTipoColumnTable("col-md-12");
		selectedNode = new DefaultTreeNode();
		newGrupoImpuesto = new GrupoImpuesto();
		detalleGrupoImpuesto = new DetalleGrupoImpuesto();
	}

	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;
		tipoColumnTable = "col-md-8";
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

	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	private GrupoImpuesto obtenerGrupoImpuestoByLocal(String nombre){
		for(GrupoImpuesto gi: listGrupoImpuesto){
			if(gi.getNombre().equals(nombre)){
				return gi;
			}
		}
		return null;
	}


	public void onNodeSelect(NodeSelectEvent event) {
		log.info("onNodeSelect()");
		EDGrupoImpuesto ed = (EDGrupoImpuesto) event.getTreeNode().getData();

		selectedGrupoImpuesto = obtenerGrupoImpuestoByLocal(ed.getPadre());
		cambiarAspecto();
		if(ed.getTipo().equals("CUENTA")){

		}
		if(ed.getTipo().equals("NUEVA")){
			log.info("NUEVA");
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgGrupoImpuesto').show();");
		}


	}

	public void agregarCuentaAGrupoImpuesto(){
		try{
			detalleGrupoImpuesto.setGrupoImpuesto(selectedGrupoImpuesto);
			detalleGrupoImpuesto.setPlanCuenta(selectedPlanCuenta);
			detalleGrupoImpuesto.setEstado("AC");
			detalleGrupoImpuesto.setUsuarioRegistro(nombreUsuario);
			detalleGrupoImpuesto.setFechaRegistro(new Date());
			detalleGrupoImpuesto.setTipo(nombreTipoGrupoImpuesto);
			detalleGrupoImpuesto = detalleGrupoImpuestoRegistration.create(detalleGrupoImpuesto);

			listDetalleGrupoImpuesto.add(detalleGrupoImpuesto);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cuenta Agregada!", selectedPlanCuenta.getDescripcion());
			facesContext.addMessage(null, m);
			initNewGrupoImpuesto();
		}catch(Exception e){
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"ERROR", "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	// --------------   get and set  ---------------
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

	public GrupoImpuesto getSelectedGrupoImpuesto() {
		return selectedGrupoImpuesto;
	}

	public void setSelectedGrupoImpuesto(GrupoImpuesto selectedGrupoImpuesto) {
		this.selectedGrupoImpuesto = selectedGrupoImpuesto;
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

	public boolean isPermitirCredito() {
		return permitirCredito;
	}

	public void setPermitirCredito(boolean permitirCredito) {
		this.permitirCredito = permitirCredito;
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

	public String getTipoColumnTable() {
		return tipoColumnTable;
	}

	public void setTipoColumnTable(String tipoColumnTable) {
		this.tipoColumnTable = tipoColumnTable;
	}

	public GrupoImpuesto getNewGrupoImpuesto(){
		return newGrupoImpuesto;
	}

	public void setNewGrupoImpuesto(GrupoImpuesto newGrupoImpuesto){
		this.newGrupoImpuesto = newGrupoImpuesto;
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

	public List<GrupoImpuesto> getListGrupoImpuesto() {
		return listGrupoImpuesto;
	}

	public void setListGrupoImpuesto(List<GrupoImpuesto> listGrupoImpuesto) {
		this.listGrupoImpuesto = listGrupoImpuesto;
	}

	public boolean isStateExpandingPlanCuenta() {
		return stateExpandingPlanCuenta;
	}

	public void setStateExpandingPlanCuenta(boolean stateExpandingPlanCuenta) {
		this.stateExpandingPlanCuenta = stateExpandingPlanCuenta;
	}

	public DetalleGrupoImpuesto getDetalleGrupoImpuesto() {
		return detalleGrupoImpuesto;
	}

	public void setDetalleGrupoImpuesto(DetalleGrupoImpuesto detalleGrupoImpuesto) {
		this.detalleGrupoImpuesto = detalleGrupoImpuesto;
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

	public String getNombrePlanCuenta() {
		return nombrePlanCuenta;
	}

	public void setNombrePlanCuenta(String nombrePlanCuenta) {
		this.nombrePlanCuenta = nombrePlanCuenta;
		this.selectedPlanCuenta = obtenerPlanCuentaByLocal(nombrePlanCuenta);
	}

	public String getNombreTipoGrupoImpuesto() {
		return nombreTipoGrupoImpuesto;
	}

	public void setNombreTipoGrupoImpuesto(String nombreTipoGrupoImpuesto) {
		this.nombreTipoGrupoImpuesto = nombreTipoGrupoImpuesto;
	}

	public String[] getListTipoGrupoImpuesto() {
		return listTipoGrupoImpuesto;
	}

	public void setListTipoGrupoImpuesto(String[] listTipoGrupoImpuesto) {
		this.listTipoGrupoImpuesto = listTipoGrupoImpuesto;
	}
}
