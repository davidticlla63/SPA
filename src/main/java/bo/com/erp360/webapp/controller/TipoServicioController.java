package bo.com.erp360.webapp.controller;

import java.io.Serializable;
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
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.TipoServicioRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.TipoServicio;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.TipoServicioRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "tipoServicioController")
@SuppressWarnings("serial")
@ConversationScoped
public class TipoServicioController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;
	
	private @Inject TipoServicioRegistration tipoServicioRegistration;

	private @Inject TipoServicioRepository tipoServicioRepository;
	
	private @Inject UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(TipoServicioController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventTipoServicio;

	//estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean seleccionadaDosificacion;
	private boolean estadoButtonDialog;

	private String nombreEstado="ACTIVO";
	private String tipoColumnTable; //8

	private List<TipoServicio> listTipoServicio;
	private List<TipoServicio> listFilterTipoServicio;
	private String[] listEstado = {"ACTIVO","INACTIVO"};
	private String[] listResolucionNormativa = {"NSF-07","SFV-14"};

	private TipoServicio newTipoServicio;
	private TipoServicio selectedTipoServicio;

	//login
	private Usuario usuario;
	private String nombreUsuario;	
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	private Date fechaMinima;


	@PostConstruct
	public void initNewTipoServicio() {
		log.info(" init new initNewTipoServicio controller");
		beginConversation();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		usuario = estadoUsuarioLogin.getUsuarioSession(usuarioRepository);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);

		fechaMinima = new Date();
		loadDefault();
	}

	public void loadDefault(){
		crear = true;
		registrar = false; 
		modificar = false;
		seleccionadaDosificacion = false;
		estadoButtonDialog = true;
		tipoColumnTable = "col-md-12";
		newTipoServicio = new TipoServicio();
		selectedTipoServicio = new TipoServicio();

		// traer todos las tipoServicioes
		listTipoServicio = tipoServicioRepository.findAllByEmpresa(empresaLogin);
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

	//-----  metodos tipoServicio ---------------

	public void registrarTipoServicio(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newTipoServicio.setEstado(estado);
			newTipoServicio.setUsuarioRegistro(nombreUsuario);
			newTipoServicio.setFechaRegistro(new Date());
			newTipoServicio.setEmpresa(empresaLogin);
			newTipoServicio = tipoServicioRegistration.create(newTipoServicio);

			
			FacesUtil.infoMessage("TipoServicio Registrada!",newTipoServicio.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo registrar la tipoServicio.!");
		}
	}

	public void modificarTipoServicio(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newTipoServicio.setEstado(estado);
			newTipoServicio.setEmpresa(empresaLogin);
			newTipoServicio.setFechaModificacion(new Date());
			tipoServicioRegistration.update(newTipoServicio);
			
			FacesUtil.infoMessage("TipoServicio Modificada!",newTipoServicio.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo modificar la tipoServicio.!");
		}
	}

	public void eliminarTipoServicio(){
		try{
			newTipoServicio.setEstado("RM");
			tipoServicioRegistration.update(newTipoServicio);
			
			FacesUtil.infoMessage("TipoServicio Eliminada!",newTipoServicio.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo eliminar la tipoServicio.!");
		}

	}

	public void onRowSelectTipoServicio(SelectEvent event) {
		log.info("onRowSelectTipoServicio -> selectedTipoServicio:"+selectedTipoServicio.getNombre());
		crear = false;
		modificar = true;
		registrar = false ;
		newTipoServicio = selectedTipoServicio;
		FacesUtil.updateComponent("formTableDosificacion:dataTableDosificacion");
	}

	//-------  metodos dosificacion -----------

	

	

	//--------  acciones para la vista----------

	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;

	}

	// -------- get and set---------------------

	public boolean isModificar() {
		return modificar;
	}

	public void setModificar(boolean modificar) {
		this.modificar = modificar;
	}

	public TipoServicio getSelectedTipoServicio() {
		return selectedTipoServicio;
	}

	public void setSelectedTipoServicio(TipoServicio selectedTipoServicio) {
		this.selectedTipoServicio = selectedTipoServicio;
	}

	public String getTest(){
		return "test";
	}

	public Empresa getEmpresaLogin() {
		return empresaLogin;
	}

	public void setEmpresaLogin(Empresa empresaLogin) {
		this.empresaLogin = empresaLogin;
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

	public List<TipoServicio> getListFilterTipoServicio() {
		return listFilterTipoServicio;
	}

	public void setListFilterTipoServicio(List<TipoServicio> listFilterTipoServicio) {
		this.listFilterTipoServicio = listFilterTipoServicio;
	}

	public String getTipoColumnTable() {
		return tipoColumnTable;
	}

	public void setTipoColumnTable(String tipoColumnTable) {
		this.tipoColumnTable = tipoColumnTable;
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

	public String[] getListResolucionNormativa() {
		return listResolucionNormativa;
	}

	public void setListResolucionNormativa(String[] listResolucionNormativa) {
		this.listResolucionNormativa = listResolucionNormativa;
	}

	
	public boolean isSeleccionadaDosificacion() {
		return seleccionadaDosificacion;
	}

	public void setSeleccionadaDosificacion(boolean seleccionadaDosificacion) {
		this.seleccionadaDosificacion = seleccionadaDosificacion;
	}

	public Date getFechaMinima() {
		return fechaMinima;
	}

	public void setFechaMinima(Date fechaMinima) {
		this.fechaMinima = fechaMinima;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public TipoServicio getNewTipoServicio() {
		return newTipoServicio;
	}

	public void setNewTipoServicio(TipoServicio newTipoServicio) {
		this.newTipoServicio = newTipoServicio;
	}

	public List<TipoServicio> getListTipoServicio() {
		return listTipoServicio;
	}

	public void setListTipoServicio(List<TipoServicio> listTipoServicio) {
		this.listTipoServicio = listTipoServicio;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	

	public boolean isEstadoButtonDialog() {
		return estadoButtonDialog;
	}

	public void setEstadoButtonDialog(boolean estadoButtonDialog) {
		this.estadoButtonDialog = estadoButtonDialog;
	} 

}
