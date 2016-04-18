package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.RolRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.model.security.Rol;
import bo.com.erp360.webapp.service.RolRegistration;
import bo.com.erp360.webapp.util.FacesUtil;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "rolController")
@ConversationScoped
public class RolController implements Serializable {

	private static final long serialVersionUID = 1730442750062837853L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private RolRegistration rolRegistration;

	@Inject
	private RolRepository rolesRepository;

	private Logger log = Logger.getLogger(this.getClass());

	private @Inject SessionMain sessionMain; //variable del login
	private String nombreUsuario;	
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private boolean crear = true;
	private boolean registrar = false;
	private boolean modificar = false;

	private String tituloPanel = "Registrar Roles";
	private String tipoColumnTable; //8
	private String nombreEstado="ACTIVO";

	private Rol newRol;
	private Rol selectedRol;
	private List<Rol> listRol = new ArrayList<Rol>();
	private List<Rol> listFilterRol = new ArrayList<Rol>();

	private List<Usuario> listUsuario = new ArrayList<Usuario>();
	private String[] listEstado = {"ACTIVO","INACTIVO"};

	@PostConstruct
	public void initNewRoles() {

		log.info(" init new initNewRoles");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		loadDefault();
	}

	private void loadDefault(){
		tipoColumnTable = "col-md-12";
		newRol = new Rol();
		listRol = rolesRepository.findAllOrderByAsc();
		modificar = false;
		crear = true;
		registrar = false;
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

	public void registrarRol() {
		try {
			newRol.setEstado(nombreEstado.equals("ACTIVO")?"AC":"IN");
			newRol.setFechaRegistro(new Date());
			newRol.setUsuarioRegistro(nombreUsuario);
			if(!newRol.validate(facesContext, empresaLogin, gestionLogin)){
				resetearFitrosTabla("formTableRoles:dataTableRoles");
				return;
			}
			rolRegistration.create(newRol);
			FacesUtil.showDialog("Rol registrado "+newRol.getNombre());
			resetearFitrosTabla("formTableRoles:dataTableRoles");
			loadDefault();
		} catch (Exception e) {
			log.error("registrarRoles error: "+e.getMessage());
			FacesUtil.errorMessage("Error al registrar Rol");
		}
	}

	public void modificarRol() {
		try {
			newRol.setEstado(nombreEstado.equals("ACTIVO")?"AC":"IN");
			rolRegistration.update(newRol);			
			FacesUtil.showDialog("Rol modificado "+newRol.getNombre());
			resetearFitrosTabla("formTableRoles:dataTableRoles");
			loadDefault();
		} catch (Exception e) {
			log.error("modificarRoles error: "+e.getMessage());
			FacesUtil.errorMessage("Error al modificar Rol"); 
		}
	}

	public void eliminarRol() {
		try {
			newRol.setEstado("RM");
			rolRegistration.update(newRol);
			FacesUtil.showDialog("Rol Eliminado"+newRol.getNombre());
			resetearFitrosTabla("formTableRoles:dataTableRoles");
			loadDefault();
		} catch (Exception e) {
			log.error("eliminarRoles error: "+e.getMessage());
			FacesUtil.errorMessage("Error al eliminar Rol");
		}
	}

	public void actualizarForm(){
		crear = true;
		registrar = false;
		modificar = false;
		tipoColumnTable = "col-md-12";
		selectedRol= new Rol();
		newRol = new Rol();
		resetearFitrosTabla("formTableRoles:dataTableRoles");
	}

	public void onRowSelect(SelectEvent event) {
		newRol = selectedRol;
		crear = false;
		registrar = false;
		modificar = true;
		tipoColumnTable = "col-md-8";
		nombreEstado = newRol.getEstado().equals("AC")?"ACTIVO":"INACTIVO";
		resetearFitrosTabla("formTableRoles:dataTableRoles");
	}

	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;
		tipoColumnTable = "col-md-8";
		selectedRol= new Rol();
		newRol = new Rol();
	}

	// -------------------- get and set -------------------
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

	public List<Usuario> getListUsuario() {
		return listUsuario;
	}

	public void setListUsuario(List<Usuario> listUsuario) {
		this.listUsuario = listUsuario;
	}

	public String getTest(){
		return "test";
	}

	public Rol getNewRol() {
		return newRol;
	}

	public void setNewRol(Rol newRol) {
		this.newRol = newRol;
	}

	public Rol getSelectedRol() {
		return selectedRol;
	}

	public void setSelectedRol(Rol selectedRol) {
		this.selectedRol = selectedRol;
	}

	public List<Rol> getListRol() {
		return listRol;
	}

	public void setListRol(List<Rol> listRol) {
		this.listRol = listRol;
	}

	public List<Rol> getListFilterRol() {
		return listFilterRol;
	}

	public void setListFilterRol(List<Rol> listFilterRol) {
		this.listFilterRol = listFilterRol;
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

	public String[] getListEstado() {
		return listEstado;
	}

	public void setListEstado(String[] listEstado) {
		this.listEstado = listEstado;
	}

	public String getNombreEstado() {
		return nombreEstado;
	}

	public void setNombreEstado(String nombreEstado) {
		this.nombreEstado = nombreEstado;
	}


}
