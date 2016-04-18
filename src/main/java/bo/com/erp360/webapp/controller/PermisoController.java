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
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.DetallePaginaRepository;
import bo.com.erp360.webapp.data.PermisoRepository;
import bo.com.erp360.webapp.data.RolRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.security.Accion;
import bo.com.erp360.webapp.model.security.DetallePagina;
import bo.com.erp360.webapp.model.security.Modulo;
import bo.com.erp360.webapp.model.security.Pagina;
import bo.com.erp360.webapp.model.security.PermisoV1;
import bo.com.erp360.webapp.model.security.Rol;
import bo.com.erp360.webapp.service.PermisoRegistration;
import bo.com.erp360.webapp.util.EDAccion;
import bo.com.erp360.webapp.util.EDPermisoV1;
import bo.com.erp360.webapp.util.FacesUtil;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "permisoController")
@ConversationScoped
public class PermisoController implements Serializable {

	private static final long serialVersionUID = -7144100519418489169L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	Conversation conversation;

	
	private @Inject PermisoRepository permisoRepository;
	private @Inject RolRepository rolesRepository;
	private @Inject DetallePaginaRepository detallePaginaRepository;

	private @Inject PermisoRegistration permisoRegistration;
	
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

	//------------------------------------------------------------------
	private List<PermisoV1> listPermisoV1 = new ArrayList<PermisoV1>();
	private Rol selectedRol;
	private List<Rol> listRol = new ArrayList<Rol>();
	private List<DetallePagina> listDetallePagina = new ArrayList<DetallePagina>();
	//------------------------------------------------------------------
	private List<Modulo> listModulo = new ArrayList<Modulo>();
	private List<Pagina> listPagina = new ArrayList<Pagina>();
	private List<EDAccion> listAccion = new ArrayList<EDAccion>();

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

		listDetallePagina = detallePaginaRepository.findAll();
		listRol = rolesRepository.findAllOrderByAsc();
		loadDefault();
	}

	private void loadDefault(){
		nombreRol = listRol.get(0).getNombre();
		selectedRol = listRol.get(0);
		listPermisoV1 = permisoRepository.findByRol(selectedRol);
		cargarPermiso();
		cargarNodos();
	}

	//------------------------------------------------------------------

	private Rol obtenerRolByLocal(String nombreRol){
		for(Rol r: listRol){
			if(r.getNombre().equals(nombreRol)){
				return r;
			}
		}
		return null;
	}

	/**
	 * 1.-  obtener los modulos
	 * 2.-  obtener los submodulos
	 * 3.-  obtener las paginas
	 * 4.-  obtener las acciones
	 */
	public void cargarNodos(){
		List<Modulo> listModuloRoot = findModuloRootByLocal();
		loadTreeNode( listModuloRoot);
	}

	private void cargarPermiso(){
		listModulo = new ArrayList<Modulo>();
		listPagina = new ArrayList<Pagina>();
		listAccion = new ArrayList<EDAccion>();
		
		listModulo = findModuloByLocal();
		listPagina = findPaginaByLocal();
		listAccion = findEDAccionByLocal();
	}

	private List<Modulo> findModuloRootByLocal(){
		List<Modulo> list = new ArrayList<Modulo>();
		for(DetallePagina dp : listDetallePagina){
			Modulo m = dp.getPagina().getModulo();
			if(! list.contains(m)){
				list.add(m);
			}
		}
		return list;
	}

	private List<Modulo> findModuloByLocal(){
		List<Modulo> list = new ArrayList<Modulo>();
		for(PermisoV1 per : listPermisoV1){
			DetallePagina dp = per.getDetallePagina();
			Modulo m = dp.getPagina().getModulo();
			if(! list.contains(m)){
				list.add(m);
			}
		}
		return list;
	}

	private List<Pagina> findPaginaByLocal(){
		List<Pagina> list = new ArrayList<Pagina>();
		for(PermisoV1 per : listPermisoV1){
			DetallePagina dp = per.getDetallePagina();
			Pagina p = dp.getPagina();
			if(! list.contains(p)){
				list.add(p);
			}
		}
		return list;
	}

	private boolean verificarExisteAccionPagina(Pagina p,Accion ac){
		for(EDAccion eda: listAccion){
			if(eda.getAccion().equals(ac) && eda.getPagina().equals(p)){
				return true;
			}
		}
		return false;
	}

	private List<EDAccion> findEDAccionByLocal(){
		List<EDAccion> list = new ArrayList<EDAccion>();
		for(PermisoV1 per : listPermisoV1){
			DetallePagina dp = per.getDetallePagina();
			Pagina p = dp.getPagina();
			Accion a = dp.getAccion();
			if( ! verificarExisteAccionPagina(p,a)){
				EDAccion eda = new EDAccion();
				eda.setAccion(a);
				eda.setPagina(p);
				list.add(eda);
			}
		}
		return list;
	}

	private void loadTreeNode(List<Modulo> listModulo){
		root = new DefaultTreeNode(new EDPermisoV1("root", "", "",null), null);
		for(Modulo mo : listModulo){
			TreeNode tn = new DefaultTreeNode(new EDPermisoV1(mo.getNombre(),"ICON","MODULO",(Modulo)mo),root);
			tn.setExpanded(true);
			tn.setSelected(tienePermisoModulo(mo));
			List<Pagina> listPagina = new ArrayList<Pagina>();
			listPagina = obtenerPaginasByModulo(mo);
			for(Pagina pa: listPagina){
				TreeNode tn2 = new DefaultTreeNode("2",new EDPermisoV1(pa.getNombre(),"ICON","PAGINA",(Pagina)pa),tn);
				tn2.setExpanded(true);
				tn2.setSelected(tienePermisoPagina(pa));
				List<EDAccion> listAccion = obtenerEDAccionByPagina(pa);
				for(EDAccion ac: listAccion ){
					TreeNode tn3 = new DefaultTreeNode("3",new EDPermisoV1(ac.getAccion().getNombre(),"ICON","ACCION",(EDAccion)ac),tn2);
					tn3.setSelected(tienePermisoAccion(ac));
				}
			}
		}
	}

	private boolean tienePermisoModulo(Modulo m){
		for(Modulo modAux : listModulo){
			if(modAux.equals(m)){
				return true;
			}
		}
		return false;
	}

	private boolean tienePermisoPagina(Pagina p){
		for(Pagina pagAux : listPagina){
			if(pagAux.equals(p)){
				return true;
			}
		}
		return false;
	}

	private boolean tienePermisoAccion(EDAccion eda){
		Pagina p = eda.getPagina();
		Accion a = eda.getAccion();
		for(EDAccion per : listAccion ){
			Pagina pagAux = per.getPagina();
			Accion accAux = per.getAccion();
			if(pagAux.equals(p) && accAux.equals(a)){
				return true;
			}
		}
		return false;
	}

	private List<Pagina> obtenerPaginasByModulo(Modulo modulo){
		List<Pagina> list = new ArrayList<Pagina>();
		for(DetallePagina dp : listDetallePagina){
			Pagina pa = dp.getPagina(); 
			if(pa.getModulo().equals(modulo)){
				if( ! list.contains(pa)){
					list.add(pa);
				}
			}
		}
		return list;
	}

	private List<EDAccion> obtenerEDAccionByPagina(Pagina pagina){
		List<EDAccion> list = new ArrayList<EDAccion>();
		for(DetallePagina dp : listDetallePagina){
			if(dp.getPagina().equals(pagina)){
				EDAccion ed = new EDAccion();
				ed.setAccion(dp.getAccion());
				ed.setPagina(dp.getPagina());
				if( ! list.contains(ed)){
					list.add(ed);
				}
			}
		}
		return list;
	}

	public void limpiarPermisos(){
		listModulo = new ArrayList<Modulo>();
		listPagina = new ArrayList<Pagina>();
		listAccion = new ArrayList<EDAccion>();
		cargarNodos();
	}

	public void onNodeSelect(NodeSelectEvent event){
		EDPermisoV1 e = (EDPermisoV1)event.getTreeNode().getData();
		log.info("onNodeSelect : "+e.getNombre() +" tipo : "+e.getTipo());
		switch (e.getTipo()) {
		case "MODULO":
			Modulo m = (Modulo)e.getObject();
			if( ! listModulo.contains(m)){
				listModulo.add(m);
			}
			break;
		case "PAGINA":
			Pagina p = (Pagina)e.getObject();
			agregarPermiso(p);
			break;
		case "ACCION":
			EDAccion eda = (EDAccion)e.getObject();
			if( ! listAccion.contains(eda)){
				listAccion.add(eda);
			}
			agregarPermiso(eda.getPagina());
			break;

		default:
			break;
		}
		cargarNodos();
	}

	private void agregarPermiso(Pagina p){
		if( ! listPagina.contains(p)){
			listPagina.add(p);
		}
		if( ! listModulo.contains(p.getModulo())){
			listModulo.add(p.getModulo());
		}
	}

	//------------------------------------------------------------------

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

	public void registrarPermisos() {
		try {
			Date fechaActual = new Date();
			log.info("Ingreso a registrarPrivilegio 1: ");
			for(PermisoV1 perm : listPermisoV1){
				perm.setFechaModificacion(fechaActual);
				perm.setEstado("RM");
				permisoRegistration.update(perm);
			}
			//obtenerPermisosSeleccionados();
			log.info("Ingreso a registrarPrivilegio 2");
			for(EDAccion eda : listAccion){
				Pagina pag = eda.getPagina();
				log.info("Pagina "+pag.getId());
				Accion ac = eda.getAccion();
				log.info("Accion "+ac.getId());
				DetallePagina detalle = detallePaginaRepository.findAccionByPaginaAndAccion(pag,ac);
				log.info("detalle "+detalle);
				PermisoV1 permiso = new PermisoV1();
				permiso.setDetallePagina(detalle);
				permiso.setEstado("AC");
				permiso.setFechaRegistro(fechaActual);
				permiso.setRol(selectedRol);
				permiso.setUsuarioRegistro(nombreUsuario);
				permisoRegistration.create(permiso);
			}
			FacesUtil.infoMessage("Permisos Registrados!", " ");
		} catch (Exception e) {
			log.error("ERROR "+e.getMessage());
			FacesUtil.errorMessage("Error al registrar!");
		}
	}

	// -----------  get and set ------------

	public String getNombreRol() {
		return nombreRol;
	}

	public void setNombreRol(String nombreRol) {
		this.nombreRol = nombreRol;
		selectedRol = obtenerRolByLocal( nombreRol);
		listPermisoV1 = new ArrayList<PermisoV1>();
		listPermisoV1 = permisoRepository.findByRol(selectedRol);
		cargarPermiso();
		cargarNodos();
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

	public List<PermisoV1> getListPermisoV1() {
		return listPermisoV1;
	}

	public void setListPermisoV1(List<PermisoV1> listPermisoV1) {
		this.listPermisoV1 = listPermisoV1;
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

	public List<DetallePagina> getListDetallePagina() {
		return listDetallePagina;
	}

	public void setListDetallePagina(List<DetallePagina> listDetallePagina) {
		this.listDetallePagina = listDetallePagina;
	}

}
