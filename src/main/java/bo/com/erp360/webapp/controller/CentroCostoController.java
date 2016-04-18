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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.CentroCostoRepository;
import bo.com.erp360.webapp.data.GrupoCentroCostoRepository;
import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.GrupoCentroCosto;
import bo.com.erp360.webapp.service.CentroCostoRegistration;
import bo.com.erp360.webapp.service.GrupoCentroCostoRegistration;
import bo.com.erp360.webapp.util.EDCentroCosto;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "centroCostoController")
@ConversationScoped
public class CentroCostoController implements Serializable {
	
	private static final long serialVersionUID = 2239092379867126636L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private CentroCostoRepository centroCostoRepository;

	@Inject
	private CentroCostoRegistration centroCostoRegistration;

	@Inject
	private GrupoCentroCostoRepository grupoCentroCostoRepository;

	@Inject
	private GrupoCentroCostoRegistration grupoCentroCostoRegistration;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private Logger log = Logger.getLogger(this.getClass());
	
	//estados
	private boolean crear = true;  
	private boolean modificar = false;
	private boolean registrar = false;
	private boolean modificarCentroCosto = false;
	private boolean buttonVerCentroCosto = false;
	private boolean buttonRegistrarCentroCosto = false;
	private boolean verCentroCosto = false;

	private String nombreUsuario; 
	private String tituloPanel = "Registrar Comprobante";
	private String tipoColumnTable = "col-md-12"; //8

	@Produces
	@Named
	private CentroCosto newCentroCosto;
	@Produces
	@Named
	private GrupoCentroCosto newGrupoCentroCosto;
	private CentroCosto selectedCentroCosto;
	private GrupoCentroCosto selectedGrupoCentroCosto;

	private List<CentroCosto> listCentroCosto = new ArrayList<CentroCosto>();
	private List<GrupoCentroCosto> listGrupoCentroCosto = new ArrayList<GrupoCentroCosto>();

	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	//TREE
	private TreeNode root;
	private TreeNode selectedNode;

	@PostConstruct
	public void initNewCentroCosto() {
		log.info(" init new PostConstruct CentroCostoController");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		loadValuesDefault();
	}

	private void loadValuesDefault(){
		modificarCentroCosto = false;
		buttonRegistrarCentroCosto = false;
		tituloPanel = "Centro Costo";	
		newCentroCosto = new CentroCosto();
		newGrupoCentroCosto = new GrupoCentroCosto();
		selectedCentroCosto = new CentroCosto();
		selectedGrupoCentroCosto = new GrupoCentroCosto();

		listGrupoCentroCosto = grupoCentroCostoRepository.findAllByEmpresa(empresaLogin,gestionLogin);
		listCentroCosto = centroCostoRepository.findAllCentroCostoByEmpresa(empresaLogin);

		root = new DefaultTreeNode("Root", null);
		alternativo();
		collapsingORexpanding(root,true);
	}

	private  void collapsingORexpanding(TreeNode n, boolean option) {
		if(n.getChildren().size() == 0) {
			n.setSelected(false);
		}
		else {
			for(TreeNode s: n.getChildren()) {
				collapsingORexpanding(s, option);
			}
			n.setExpanded(option);
			n.setSelected(false);
		}
	}

	@Produces
	@Named
	public List<CentroCosto> getListCentroCosto() {
		return listCentroCosto;
	}

	@Produces
	@Named
	public List<GrupoCentroCosto> getListGrupoCentroCosto() {
		return listGrupoCentroCosto;
	}

	private void alternativo(){
		int cont1,cont2;

		List<GrupoCentroCosto> pcNivel1 = listGrupoCentroCosto ;//grupoCentroCostoRepository.findAllByEmpresa(empresaLogin);
		List<CentroCosto> pcNivel2 = listCentroCosto; //centroCostoRepository.findAllByEmpresa(empresaLogin);
		//--
		cont1=0;
		while(cont1 < pcNivel1.size() ){//---1
			GrupoCentroCosto pc1 = pcNivel1.get(cont1);
			cont1++;
			TreeNode tn1 = new DefaultTreeNode(new EDCentroCosto(pc1.getId(), pc1.getNombre(), ""),root);
			cont2=0;
			while(cont2 < pcNivel2.size() ){//---2
				CentroCosto pc2 = pcNivel2.get(cont2);
				cont2++;
				if(pc2.getGrupoCentroCosto().getId() ==pc1.getId()){
					TreeNode tn2 = new DefaultTreeNode(new EDCentroCosto(pc2.getId(), pc2.getGrupoCentroCosto().getNombre(), pc2.getNombre()),tn1);
				}
			}
		}
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
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void registrarGrupoCentroCosto(){
		try{
			newGrupoCentroCosto.setEmpresa(empresaLogin);
			newGrupoCentroCosto.setGestion(gestionLogin);
			newGrupoCentroCosto.setEstado("AC");
			newGrupoCentroCosto.setFechaRegistro(new Date());
			newGrupoCentroCosto.setUsuarioRegistro(nombreUsuario);
			grupoCentroCostoRegistration.create(newGrupoCentroCosto);			
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Centro de Costo Registrado!", newGrupoCentroCosto.getNombre());
			facesContext.addMessage(null, m);
			crear = false; modificar = false; registrar = true; verCentroCosto = false; buttonVerCentroCosto= false;
			loadValuesDefault();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			log.error("registrarGrupoCentroCosto() -> "+errorMessage);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void registrarCentroCosto(){
		try{
			newCentroCosto.setEstado("AC");
			newCentroCosto.setFechaRegistro(new Date());
			newCentroCosto.setUsuarioRegistro(nombreUsuario);
			newCentroCosto.setGrupoCentroCosto(selectedGrupoCentroCosto);
			centroCostoRegistration.create(newCentroCosto);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"SubCentro de Costo Registrado!", newCentroCosto.getNombre());
			facesContext.addMessage(null, m);
			listCentroCosto = centroCostoRepository.findAllByGrupoCentroCosto(selectedGrupoCentroCosto);
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			log.error("registrarCentroCosto() -> "+errorMessage);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void modificarGrupoCentroCosto(){
		try{
			newGrupoCentroCosto.setEstado("AC");
			newGrupoCentroCosto.setFechaRegistro(new Date());
			newGrupoCentroCosto.setUsuarioRegistro(nombreUsuario);
			grupoCentroCostoRegistration.update(newGrupoCentroCosto);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Centro de Costo Modificado!", newCentroCosto.getNombre());
			facesContext.addMessage(null, m);
			crear = false; modificar = false; registrar = true; verCentroCosto = false; buttonVerCentroCosto= false;
			loadValuesDefault();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Modificacion Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void modificarCentroCosto(){
		try{
			newCentroCosto.setEstado("AC");
			newCentroCosto.setFechaRegistro(new Date());
			newCentroCosto.setUsuarioRegistro(nombreUsuario);
			centroCostoRegistration.update(newCentroCosto);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"SubCentro de Costo Modificado!", newCentroCosto.getNombre());
			facesContext.addMessage(null, m);
			listCentroCosto = centroCostoRepository.findAllByGrupoCentroCosto(selectedGrupoCentroCosto);
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			log.error("modificarCentroCosto() -> "+errorMessage);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Error al Eliminar.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminarGrupoCentroCosto(){
		try{
			newGrupoCentroCosto.setEstado("RM");
			newGrupoCentroCosto = grupoCentroCostoRegistration.update(newGrupoCentroCosto);
			centroCostoRegistration.deleteCentGrupoCentroCosto(newGrupoCentroCosto);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Centro de Costo Eliminado!", newGrupoCentroCosto.getNombre());
			facesContext.addMessage(null, m);
			crear = false; modificar = false; registrar = true; verCentroCosto = false; buttonVerCentroCosto= false;
			loadValuesDefault();			
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			log.error("eliminarGrupoCentroCosto() -> "+errorMessage);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Error al Eliminar.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminarCentroCosto(){
		try{
			newCentroCosto.setEstado("RM");
			centroCostoRegistration.update(newCentroCosto);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"SubCentro de Costo Eliminado!", newCentroCosto.getNombre());
			facesContext.addMessage(null, m);
			listCentroCosto = centroCostoRepository.findAllByGrupoCentroCosto(selectedGrupoCentroCosto);
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			log.error("eliminarCentroCosto() -> "+errorMessage);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Error al Eliminar.");
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

	public void onRowSelectCC(SelectEvent event){
		newCentroCosto = new CentroCosto();
		newCentroCosto = selectedCentroCosto;
		modificarCentroCosto = true;
		setButtonRegistrarCentroCosto(false);
	}

	public void onRowSelectGCC(SelectEvent event){
		newGrupoCentroCosto = new GrupoCentroCosto();
		newGrupoCentroCosto =selectedGrupoCentroCosto;
		verCentroCosto = false;
		buttonVerCentroCosto = true;

		crear = false;
		registrar = false;
		modificar = true;
	}

	public void actualizarFormReg(){
		crear = true;
		registrar = false;
		modificar = false;
		newGrupoCentroCosto = new GrupoCentroCosto();
		selectedGrupoCentroCosto = new GrupoCentroCosto();
		selectedCentroCosto = new CentroCosto();
	}

	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;
	}

	// -------------   get and set --------------------
	public String getTituloPanel() {
		return tituloPanel;
	}

	public void setTituloPanel(String tituloPanel) {
		this.tituloPanel = tituloPanel;
	}

	public CentroCosto getSelectedCentroCosto() {
		return selectedCentroCosto;
	}

	public void setSelectedCentroCosto(CentroCosto selectedCentroCosto) {
		this.selectedCentroCosto = selectedCentroCosto;
	}

	public GrupoCentroCosto getSelectedGrupoCentroCosto() {
		return selectedGrupoCentroCosto;
	}

	public void setSelectedGrupoCentroCosto(GrupoCentroCosto selectedGrupoCentroCosto) {
		this.selectedGrupoCentroCosto = selectedGrupoCentroCosto;
	}

	public boolean isModificar() {
		return modificar;
	}

	public void setModificar(boolean modificar) {
		this.modificar = modificar;
	}

	public boolean isVerCentroCosto() {
		return verCentroCosto;
	}

	public void setVerCentroCosto(boolean verCentroCosto) {
		this.verCentroCosto = verCentroCosto;
		this.buttonVerCentroCosto = false;
		setModificarCentroCosto(false);
		buttonRegistrarCentroCosto = true;
		listCentroCosto = centroCostoRepository.findAllByGrupoCentroCosto(selectedGrupoCentroCosto);
	}

	public void setVerCentroCosto2(boolean verCentroCosto) {
		this.verCentroCosto = verCentroCosto;
		this.buttonVerCentroCosto = true;
		setModificarCentroCosto(false);
		buttonRegistrarCentroCosto = false;
	}

	public void setVerCentroCosto3(boolean verCentroCosto) {
		this.verCentroCosto = verCentroCosto;
		this.buttonVerCentroCosto = false;
		setModificarCentroCosto(false);
		buttonRegistrarCentroCosto = true;
	}
	
	public boolean isModificarCentroCosto() {
		return modificarCentroCosto;
	}

	public void setModificarCentroCosto(boolean modificarCentroCosto) {
		this.modificarCentroCosto = modificarCentroCosto;
		newCentroCosto = new CentroCosto();
		setButtonRegistrarCentroCosto(true);
	}

	public boolean isButtonVerCentroCosto() {
		return buttonVerCentroCosto;
	}

	public void setButtonVerCentroCosto(boolean buttonVerCentroCosto) {
		this.buttonVerCentroCosto = buttonVerCentroCosto;
		setButtonRegistrarCentroCosto(true);
	}

	public boolean isButtonRegistrarCentroCosto() {
		return buttonRegistrarCentroCosto;
	}

	public void setButtonRegistrarCentroCosto(boolean buttonRegistrarCentroCosto) {
		this.buttonRegistrarCentroCosto = buttonRegistrarCentroCosto;
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
}
