package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.RolesRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Roles;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.RolesRegistration;
import bo.com.erp360.webapp.util.AbstractManagedBean;

@Named(value = "rolesController")
@SuppressWarnings("serial")
@ConversationScoped
public class RolesController extends AbstractManagedBean implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private RolesRegistration rolesRegistration;
	
	@Inject
	private EmpresaRepository empresaRepository;
	
	@Inject
	private GestionRepository gestionRepository;

	@Inject
	private RolesRepository rolesRepository;
	
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private boolean modificar = false;
	
	private String tituloPanel = "Registrar Roles";
	private String nombreUsuario; 
	
	private Roles selectedRoles;
	@Produces
	@Named
	private Roles newRoles;

	private List<Usuario> listUsuario = new ArrayList<Usuario>();
	private List<Roles> listaRoles = new ArrayList<Roles>();
	private List<Roles> listFilterRoles = new ArrayList<Roles>();

	@PostConstruct
	public void initNewRoles() {
		
		System.out.println(" init new initNewRoles");
		beginConversation();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		nombreUsuario =  estadoUsuarioLogin.getNombreUsuarioSession();
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);
		gestionLogin =  estadoUsuarioLogin.getGestionSession(empresaRepository, gestionRepository);

		newRoles = new Roles();

		// tituloPanel
		tituloPanel = "Registrar roles";
		listaRoles = rolesRepository.findAll();
		modificar = false;
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
	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void registrarRoles() {
		try {
			newRoles.setState("AC");
			newRoles.setFechaRegistro(new Date());
			newRoles.setUsuarioRegistro(nombreUsuario);
			if(!newRoles.validate(facesContext, empresaLogin, gestionLogin)){
				resetearFitrosTabla("formTableRoles:dataTableRoles");
				return;
			}
			rolesRegistration.create(newRoles);
			addInfo(getMessage("messages.register.ok"),"Rol "+newRoles.getName());
			resetearFitrosTabla("formTableRoles:dataTableRoles");
			initNewRoles();
		} catch (Exception e) {
			System.out.println("registrarRoles error: "+e.getMessage());
			addError(getMessage("messages.register.incorrect"),"Rol");
		}
	}

	public void modificarRoles() {
		try {
			rolesRegistration.update(newRoles);
			addInfo(getMessage("messages.modify.ok"),"Rol "+newRoles.getName());
			resetearFitrosTabla("formTableRoles:dataTableRoles");
			initNewRoles();
		} catch (Exception e) {
			System.out.println("modificarRoles error: "+e.getMessage());
			addError(getMessage("messages.modify.incorrect"),"Rol"); 
		}
	}

	public void eliminarRoles() {
		try {
			newRoles.setState("RM");
			rolesRegistration.update(newRoles);
			addInfo(getMessage("messages.remove.ok"),"Rol "+newRoles.getName());
			resetearFitrosTabla("formTableRoles:dataTableRoles");
			initNewRoles();
		} catch (Exception e) {
			System.out.println("eliminarRoles error: "+e.getMessage());
			addError(getMessage("messages.remove.incorrect"),"Rol");
		}
	}

	public void actualizarForm(){
		modificar = false;
		newRoles = new Roles();
	}

	public void onRowSelect(SelectEvent event) {
		Roles r = selectedRoles;//(Roles) event.getObject();
		newRoles = r;
		modificar = true;
	}

	// get and set
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

	public Roles getSelectedRoles() {
		return selectedRoles;
	}

	public void setSelectedRoles(Roles selectedRoles) {
		this.selectedRoles = selectedRoles;
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

	@Produces
	@Named
	public List<Roles> getListaRoles() {
		return listaRoles;
	}

	public List<Roles> getListFilterRoles() {
		return listFilterRoles;
	}

	public void setListFilterRoles(List<Roles> listFilterRoles) {
		this.listFilterRoles = listFilterRoles;
	}


}
