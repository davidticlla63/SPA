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
import bo.com.erp360.webapp.data.TipoProductoRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.TipoProducto;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.TipoProductoRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "tipoProductoController")
@SuppressWarnings("serial")
@ConversationScoped
public class TipoProductoController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;
	
	private @Inject TipoProductoRegistration tipoProductoRegistration;

	private @Inject TipoProductoRepository tipoProductoRepository;
	
	private @Inject UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(TipoProductoController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventTipoProducto;

	//estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean seleccionadaDosificacion;
	private boolean estadoButtonDialog;

	private String nombreEstado="ACTIVO";
	private String tipoColumnTable; //8

	private List<TipoProducto> listTipoProducto;
	private List<TipoProducto> listFilterTipoProducto;
	private String[] listEstado = {"ACTIVO","INACTIVO"};
	private String[] listResolucionNormativa = {"NSF-07","SFV-14"};

	private TipoProducto newTipoProducto;
	private TipoProducto selectedTipoProducto;

	//login
	private Usuario usuario;
	private String nombreUsuario;	
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	private Date fechaMinima;


	@PostConstruct
	public void initNewTipoProducto() {
		log.info(" init new initNewTipoProducto controller");
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
		newTipoProducto = new TipoProducto();
		selectedTipoProducto = new TipoProducto();

		// traer todos las tipoProductoes
		listTipoProducto = tipoProductoRepository.findAllByEmpresa(empresaLogin);
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

	//-----  metodos tipoProducto ---------------

	public void registrarTipoProducto(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newTipoProducto.setState(estado);
			newTipoProducto.setUsuarioRegistro(nombreUsuario);
			newTipoProducto.setFechaRegistro(new Date());
			newTipoProducto.setEmpresa(empresaLogin);
			newTipoProducto = tipoProductoRegistration.create(newTipoProducto);

			
			FacesUtil.infoMessage("TipoProducto Registrada!",newTipoProducto.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo registrar la tipoProducto.!");
		}
	}

	public void modificarTipoProducto(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newTipoProducto.setState(estado);
			newTipoProducto.setEmpresa(empresaLogin);
			newTipoProducto.setFechaModificacion(new Date());
			tipoProductoRegistration.update(newTipoProducto);
			
			FacesUtil.infoMessage("TipoProducto Modificada!",newTipoProducto.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo modificar la tipoProducto.!");
		}
	}

	public void eliminarTipoProducto(){
		try{
			newTipoProducto.setState("RM");
			tipoProductoRegistration.update(newTipoProducto);
			
			FacesUtil.infoMessage("TipoProducto Eliminada!",newTipoProducto.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo eliminar la tipoProducto.!");
		}

	}

	public void onRowSelectTipoProducto(SelectEvent event) {
		log.info("onRowSelectTipoProducto -> selectedTipoProducto:"+selectedTipoProducto.getNombre());
		crear = false;
		modificar = true;
		registrar = false ;
		newTipoProducto = selectedTipoProducto;
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

	public TipoProducto getSelectedTipoProducto() {
		return selectedTipoProducto;
	}

	public void setSelectedTipoProducto(TipoProducto selectedTipoProducto) {
		this.selectedTipoProducto = selectedTipoProducto;
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

	public List<TipoProducto> getListFilterTipoProducto() {
		return listFilterTipoProducto;
	}

	public void setListFilterTipoProducto(List<TipoProducto> listFilterTipoProducto) {
		this.listFilterTipoProducto = listFilterTipoProducto;
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

	public TipoProducto getNewTipoProducto() {
		return newTipoProducto;
	}

	public void setNewTipoProducto(TipoProducto newTipoProducto) {
		this.newTipoProducto = newTipoProducto;
	}

	public List<TipoProducto> getListTipoProducto() {
		return listTipoProducto;
	}

	public void setListTipoProducto(List<TipoProducto> listTipoProducto) {
		this.listTipoProducto = listTipoProducto;
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
