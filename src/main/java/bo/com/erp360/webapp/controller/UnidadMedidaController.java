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
import bo.com.erp360.webapp.data.UnidadMedidaRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.UnidadMedida;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.UnidadMedidaRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "unidadMedidaController")
@SuppressWarnings("serial")
@ConversationScoped
public class UnidadMedidaController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;
	
	private @Inject UnidadMedidaRegistration unidadMedidaRegistration;

	private @Inject UnidadMedidaRepository unidadMedidaRepository;
	
	private @Inject UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(UnidadMedidaController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventUnidadMedida;

	//estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean seleccionadaDosificacion;
	private boolean estadoButtonDialog;

	private String nombreEstado="ACTIVO";
	private String tipoColumnTable; //8

	private List<UnidadMedida> listUnidadMedida;
	private List<UnidadMedida> listFilterUnidadMedida;
	private String[] listEstado = {"ACTIVO","INACTIVO"};
	private String[] listResolucionNormativa = {"NSF-07","SFV-14"};

	private UnidadMedida newUnidadMedida;
	private UnidadMedida selectedUnidadMedida;

	//login
	private Usuario usuario;
	private String nombreUsuario;	
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	private Date fechaMinima;


	@PostConstruct
	public void initNewUnidadMedida() {
		log.info(" init new initNewUnidadMedida controller");
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
		newUnidadMedida = new UnidadMedida();
		selectedUnidadMedida = new UnidadMedida();

		// traer todos las unidadMedidaes
		listUnidadMedida = unidadMedidaRepository.findAllByEmpresa(empresaLogin);
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

	//-----  metodos unidadMedida ---------------

	public void registrarUnidadMedida(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newUnidadMedida.setState(estado);
			newUnidadMedida.setUsuarioRegistro(nombreUsuario);
			newUnidadMedida.setFechaRegistro(new Date());
			newUnidadMedida.setEmpresa(empresaLogin);
			newUnidadMedida = unidadMedidaRegistration.create(newUnidadMedida);

			
			FacesUtil.infoMessage("UnidadMedida Registrada!",newUnidadMedida.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo registrar la unidadMedida.!");
		}
	}

	public void modificarUnidadMedida(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newUnidadMedida.setState(estado);
			newUnidadMedida.setEmpresa(empresaLogin);
			newUnidadMedida.setFechaModificacion(new Date());
			unidadMedidaRegistration.update(newUnidadMedida);
			
			FacesUtil.infoMessage("UnidadMedida Modificada!",newUnidadMedida.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo modificar la unidadMedida.!");
		}
	}

	public void eliminarUnidadMedida(){
		try{
			newUnidadMedida.setState("RM");
			unidadMedidaRegistration.update(newUnidadMedida);
			
			FacesUtil.infoMessage("UnidadMedida Eliminada!",newUnidadMedida.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo eliminar la unidadMedida.!");
		}

	}

	public void onRowSelectUnidadMedida(SelectEvent event) {
		log.info("onRowSelectUnidadMedida -> selectedUnidadMedida:"+selectedUnidadMedida.getNombre());
		crear = false;
		modificar = true;
		registrar = false ;
		newUnidadMedida = selectedUnidadMedida;
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

	public UnidadMedida getSelectedUnidadMedida() {
		return selectedUnidadMedida;
	}

	public void setSelectedUnidadMedida(UnidadMedida selectedUnidadMedida) {
		this.selectedUnidadMedida = selectedUnidadMedida;
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

	public List<UnidadMedida> getListFilterUnidadMedida() {
		return listFilterUnidadMedida;
	}

	public void setListFilterUnidadMedida(List<UnidadMedida> listFilterUnidadMedida) {
		this.listFilterUnidadMedida = listFilterUnidadMedida;
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

	public UnidadMedida getNewUnidadMedida() {
		return newUnidadMedida;
	}

	public void setNewUnidadMedida(UnidadMedida newUnidadMedida) {
		this.newUnidadMedida = newUnidadMedida;
	}

	public List<UnidadMedida> getListUnidadMedida() {
		return listUnidadMedida;
	}

	public void setListUnidadMedida(List<UnidadMedida> listUnidadMedida) {
		this.listUnidadMedida = listUnidadMedida;
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
