package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
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

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.PermisoRepository;
import bo.com.erp360.webapp.data.PrivilegioRepository;
import bo.com.erp360.webapp.data.RolRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Permiso;
import bo.com.erp360.webapp.model.Privilegio;
import bo.com.erp360.webapp.model.Roles;
import bo.com.erp360.webapp.service.PrivilegioRegistration;
import bo.com.erp360.webapp.util.EDPrivilegio;
import bo.com.erp360.webapp.util.SessionMain;


@Named(value = "privilegioController")
@ConversationScoped
public class PrivilegioController implements Serializable {

	private static final long serialVersionUID = -7144100519418489169L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private PrivilegioRegistration privilegioRegistration;

	@Inject
	private PrivilegioRepository privilegioRepository;

	@Inject
	private PermisoRepository permisoRepository;

	@Inject
	private RolRepository rolesRepository;

	private Logger log = Logger.getLogger(this.getClass());
	
	//login
	private String nombreUsuario;
	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private String nombreRol;

	private Privilegio newPrivilegio;
	private Roles rolesSelectec;

	private List<Privilegio> listaPrivilegio = new ArrayList<Privilegio>();
	private List<Permiso> listPermiso = new ArrayList<Permiso>();
	private List<Roles> listaRoles = new ArrayList<Roles>();

	//tree
	private TreeNode root;
	private TreeNode[] selectedNodes2;

	@PostConstruct
	public void initNewPrivilegio() {

		log.info(" init new initNewPrivilegio");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		//cargar por defecto todo los permisos
		listPermiso = permisoRepository.findAllOrderedByID();
		//cagar todos los roles activos
		//listaRoles = rolesRepository.findAllActivos();
		loadDefault();
	}

	private void loadDefault(){
		nombreRol = listaRoles.get(0).getName();
		rolesSelectec = listaRoles.get(0);
		listaPrivilegio = privilegioRepository.findAllByRoles(rolesSelectec);
		log.info("listaPrivilegio "+listaPrivilegio.size());
		cargarTree();
		//collapsingORexpanding(root, true);
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

	public void test(){
		listPrivilegioCargados = new ArrayList<Privilegio>();
		for(TreeNode t: selectedNodes2){
			EDPrivilegio e = (EDPrivilegio)t.getData();
			if(e.getNombre().equals("LECTURA")){
				Permiso pL = obtenerPermisoLocal(e.getPadre());
				cargarPrivilegio2(pL,"LECTURA","AC");
			}else
				if(e.getNombre().equals("ESCRITURA")){
					Permiso pE = obtenerPermisoLocal(e.getPadre());
					cargarPrivilegio2(pE,"ESCRITURA","AC");
				}else{
					Permiso p = obtenerPermisoLocal(e.getNombre());
					cargarPrivilegio1(p);
				}
		}
		log.info("listPrivilegioCargados: "+listPrivilegioCargados.size());
	}

	private void cargarPrivilegio1(Permiso permiso){
		Privilegio p = new Privilegio();
		p.setRoles(rolesSelectec);
		p.setPermiso(permiso);
		p.setLectura("IN");
		p.setEscritura("IN");
		listPrivilegioCargados.add(p);
	}

	private void cargarPrivilegio2(Permiso permiso,String tipo,String estado){
		if( ! existePrivilegio(permiso)){
			Privilegio p = new Privilegio();
			p.setRoles(rolesSelectec);
			p.setPermiso(permiso);
			p.setLectura("IN");
			p.setEscritura("IN");
			if(tipo.equals("LECTURA")){
				p.setLectura(estado);
			}
			if(tipo.equals("ESCRITURA")){
				p.setEscritura(estado);
			}
			listPrivilegioCargados.add(p);
			return ;
		}
		Privilegio privilegio = obtenerPrivilegioCargados(permiso);
		int index = obtenerIndexPrivilegioCargados(permiso);
		if(tipo.equals("LECTURA")){
			privilegio.setLectura(estado);
		}
		if(tipo.equals("ESCRITURA")){
			privilegio.setEscritura(estado);
		}
		listPrivilegioCargados.set(index,privilegio);
	}

	private Privilegio obtenerPrivilegioCargados(Permiso permiso){
		for(Privilegio p: listPrivilegioCargados){
			if(p.getPermiso().getName().equals(permiso.getName())){
				return p;
			}
		}
		return null;
	}

	private int obtenerIndexPrivilegioCargados(Permiso permiso){
		for(int i=0 ; i< listPrivilegioCargados.size();i++){
			Privilegio p = listPrivilegioCargados.get(i);
			if(p.getPermiso().getName().equals(permiso.getName())){
				return i;
			}
		}
		return -1;
	}

	private List<Privilegio> listPrivilegioCargados = new ArrayList<Privilegio>();

	private boolean existePrivilegio(Permiso permiso){
		for(Privilegio p: listPrivilegioCargados){
			if(p.getPermiso().getName().equals(permiso.getName())){
				return true;
			}
		}
		return false;
	}

	private void cargarTree(){
		root = new DefaultTreeNode(new EDPrivilegio("root", "", "", ""), null);
		List<Permiso> listPermisoNivel1 = obtenerPermisoPorNivel("1");
		List<Permiso> listPermisoNivel2 = obtenerPermisoPorNivel("2");
		List<Permiso> listPermisoNivel3 = obtenerPermisoPorNivel("3");
		for (Permiso p1 : listPermisoNivel1) {
			TreeNode tn1 = new DefaultTreeNode( new EDPrivilegio(p1.getName(), p1.getTipo(), "", "") ,root);
			tn1.setExpanded(true);
			tn1.setSelected(isCheckPermiso(p1.getName()));
			for(Permiso p2 : listPermisoNivel2){
				if(p2.getPadre().equals(p1.getName())){
					TreeNode tn2 = new DefaultTreeNode("2",new EDPrivilegio(p2.getName(), p2.getTipo(), "", p2.getPadre()) ,tn1);
					tn2.setExpanded(true);
					tn2.setSelected(isCheckPermiso(p1.getName()));
					if(! p2.getName().equals("Libros")){
						TreeNode tn21 = new DefaultTreeNode("3",new EDPrivilegio("LECTURA", "3", "", p2.getName()) ,tn2);
						tn21.setExpanded(true);
						tn21.setSelected(isCheckPermiso(p2.getName(), "LECTURA"));
						TreeNode tn22 = new DefaultTreeNode("3",new EDPrivilegio("ESCRITURA", "3", "", p2.getName()),tn2);
						tn22.setExpanded(true);
						tn22.setSelected(isCheckPermiso(p2.getName(), "ESCRITURA"));
					}
					for(Permiso p3 : listPermisoNivel3){
						if(p3.getPadre().equals(p2.getName())){
							TreeNode tn3 = new DefaultTreeNode("2",new EDPrivilegio(p3.getName(), p3.getTipo(), "", p3.getPadre()) ,tn2);
							tn3.setExpanded(true);
							tn3.setSelected(isCheckPermiso(p3.getName()));
							TreeNode tn31 = new DefaultTreeNode("3",new EDPrivilegio("LECTURA", "3", "", p3.getName()),tn3);
							tn31.setExpanded(true);
							tn31.setSelected(isCheckPermiso(p3.getName(), "LECTURA"));
							TreeNode tn32 = new DefaultTreeNode("3",new EDPrivilegio("ESCRITURA", "3", "", p3.getName()),tn3);
							tn32.setExpanded(true);
							tn32.setSelected(isCheckPermiso(p3.getName(), "ESCRITURA"));
						}
					}
				}
			}
		}
	}

	public void limpiarTodo(){

	}

	private List<Permiso> obtenerPermisoPorNivel(String nivel){
		List<Permiso> listAux = new ArrayList<Permiso>();
		for(Permiso p : listPermiso){
			if(p.getTipo().equals(nivel)){
				listAux.add(p);
			}
		}
		return listAux;
	}

	private boolean isCheckPermiso(String permiso,String tipo){
		for(Privilegio p : listaPrivilegio){
			if(p.getPermiso().getName().equals(permiso)){
				if(tipo.equals("LECTURA") && p.getLectura().equals("AC")){
					return true;
				}
				if(tipo.equals("ESCRITURA") && p.getEscritura().equals("AC")){
					return true;
				}
			}
		}
		return false;
	}

	private boolean isCheckPermiso(String permiso){
		for(Privilegio p : listaPrivilegio){
			if(p.getPermiso().getName().equals(permiso)){
				return true;
			}
		}
		return false;
	}

	private Permiso obtenerPermisoLocal(String permiso){
		for(Permiso p: listPermiso){
			if(p.getName().equals(permiso)){
				return p;
			}
		}
		return null;
	}

	private Roles obtenerRolesLocal(String roles){
		for(Roles r: listaRoles){
			if(r.getName().equals(roles)){
				return r;
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

	public void registrarPrivilegio() {
		try {
			log.info("Ingreso a registrarPrivilegio: ");
			Roles roles = obtenerRolesLocal(nombreRol);
			privilegioRegistration.removeByRoles(roles);

			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Privilegios Registrados!", "");
			facesContext.addMessage(null, m);
			initNewPrivilegio();
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
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

	public boolean verificarSuperUsuario(){
		//Roles roles = rolesRepository.findByName(nombreRol);
//		if(roles.getState().equals("SUPER")){
//			return true;
//		}
		return false;
	}

	// -----------  get and set ------------

	public String getNombreRol() {
		return nombreRol;
	}

	public void setNombreRol(String nombreRol) {
		log.info("setNombreRol("+nombreRol);
		this.nombreRol = nombreRol;
		rolesSelectec = obtenerRolesLocal(nombreRol);
		listaPrivilegio = privilegioRepository.findAllByRoles(rolesSelectec);
		cargarTree();
	}

	public boolean isSuperUsuario(){
		HttpServletRequest request = (HttpServletRequest) facesContext
				.getExternalContext().getRequest();
		return request.isUserInRole(nombreRol);
	}

	public Roles getRolesSelectec() { return rolesSelectec; }
	public void setRolesSelectec(Roles rolesSelectec) { this.rolesSelectec = rolesSelectec; }

	public List<Roles> getListaRoles() {
		return listaRoles;
	}

	public void setListaRoles(List<Roles> listaRoles) {
		this.listaRoles = listaRoles;
	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public TreeNode[] getSelectedNodes2() {
		return selectedNodes2;
	}

	public void setSelectedNodes2(TreeNode[] selectedNodes2) {
		this.selectedNodes2 = selectedNodes2;
	}

}
