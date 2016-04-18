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
import bo.com.erp360.webapp.data.LineaRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Linea;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.LineaRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "lineaController")
@SuppressWarnings("serial")
@ConversationScoped
public class LineaController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;
	
	private @Inject LineaRegistration lineaRegistration;

	private @Inject LineaRepository lineaRepository;
	
	private @Inject UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(LineaController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventLinea;

	//estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean seleccionadaDosificacion;
	private boolean estadoButtonDialog;

	private String nombreEstado="ACTIVO";
	private String tipoColumnTable; //8

	private List<Linea> listLinea;
	private List<Linea> listFilterLinea;
	private String[] listEstado = {"ACTIVO","INACTIVO"};
	private String[] listResolucionNormativa = {"NSF-07","SFV-14"};

	private Linea newLinea;
	private Linea selectedLinea;

	//login
	private Usuario usuario;
	private String nombreUsuario;	
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	private Date fechaMinima;


	@PostConstruct
	public void initNewLinea() {
		log.info(" init new initNewLinea controller");
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
		newLinea = new Linea();
		selectedLinea = new Linea();

		// traer todos las lineaes
		listLinea = lineaRepository.findAllByEmpresa(empresaLogin);
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

	//-----  metodos linea ---------------

	public void registrarLinea(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newLinea.setState(estado);
			newLinea.setUsuarioRegistro(nombreUsuario);
			newLinea.setFechaRegistro(new Date());
			newLinea.setEmpresa(empresaLogin);
			newLinea = lineaRegistration.create(newLinea);

			
			FacesUtil.infoMessage("Linea Registrada!",newLinea.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo registrar la linea.!");
		}
	}

	public void modificarLinea(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newLinea.setState(estado);
			newLinea.setEmpresa(empresaLogin);
			newLinea.setFechaModificacion(new Date());
			lineaRegistration.update(newLinea);
			
			FacesUtil.infoMessage("Linea Modificada!",newLinea.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo modificar la linea.!");
		}
	}

	public void eliminarLinea(){
		try{
			newLinea.setState("RM");
			lineaRegistration.update(newLinea);
			
			FacesUtil.infoMessage("Linea Eliminada!",newLinea.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo eliminar la linea.!");
		}

	}

	public void onRowSelectLinea(SelectEvent event) {
		log.info("onRowSelectLinea -> selectedLinea:"+selectedLinea.getNombre());
		crear = false;
		modificar = true;
		registrar = false ;
		newLinea = selectedLinea;
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

	public Linea getSelectedLinea() {
		return selectedLinea;
	}

	public void setSelectedLinea(Linea selectedLinea) {
		this.selectedLinea = selectedLinea;
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

	public List<Linea> getListFilterLinea() {
		return listFilterLinea;
	}

	public void setListFilterLinea(List<Linea> listFilterLinea) {
		this.listFilterLinea = listFilterLinea;
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

	public Linea getNewLinea() {
		return newLinea;
	}

	public void setNewLinea(Linea newLinea) {
		this.newLinea = newLinea;
	}

	public List<Linea> getListLinea() {
		return listLinea;
	}

	public void setListLinea(List<Linea> listLinea) {
		this.listLinea = listLinea;
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
