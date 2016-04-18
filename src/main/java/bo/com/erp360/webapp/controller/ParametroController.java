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
import bo.com.erp360.webapp.data.ParametroRepository;
import bo.com.erp360.webapp.data.PaisRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Parametro;
import bo.com.erp360.webapp.model.Parametro;
import bo.com.erp360.webapp.model.Pais;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.ParametroRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "parametroController")
@SuppressWarnings("serial")
@ConversationScoped
public class ParametroController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;

	private @Inject ParametroRegistration parametroRegistration;

	private @Inject ParametroRepository parametroRepository;

	private @Inject UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(ParametroController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventParametro;

	// estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean estadoButtonDialog;

	private String nombreEstado = "ACTIVO";
	private String tipoColumnTable; // 8

	private @Inject PaisRepository paisRepository;
	private List<Pais> listPais;

	private List<Parametro> listParametro;
	private List<Parametro> listFilterParametro;
	private String[] listEstado = { "ACTIVO", "INACTIVO" };
	private String[] listResolucionNormativa = { "NSF-07", "SFV-14" };

	private Parametro newParametro;
	private Parametro selectedParametro;

	// login
	private Usuario usuario;
	private String nombreUsuario;
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;
	
	private @Inject SucursalRepository sucursalRepository;
	private Sucursal sucuersalLogin;

	@PostConstruct
	public void initNewParametro() {
		log.info(" init new initNewParametro controller");
		beginConversation();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		usuario = estadoUsuarioLogin.getUsuarioSession(usuarioRepository);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);
		sucuersalLogin = estadoUsuarioLogin.getSucursalSession(empresaRepository, sucursalRepository);
		loadDefault();
	}

	public void loadDefault() {
		crear = true;
		registrar = false;
		modificar = false;
		estadoButtonDialog = true;
		tipoColumnTable = "col-md-12";
		newParametro = new Parametro();
		selectedParametro = new Parametro();
		listPais = paisRepository.findAllActivas();
		listParametro= parametroRepository.findAllActivasByEmpresaForSucursal(empresaLogin, sucuersalLogin);

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

	
	
	// ----- metodos parametro ---------------

	public void registrarParametro() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			newParametro.setEstado(estado);
			newParametro.setUsuarioRegistro(nombreUsuario);
			newParametro.setFechaRegistro(new Date());
			newParametro = parametroRegistration.create(newParametro);

			FacesUtil.infoMessage("Parametro Registrada!", newParametro.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo registrar la parametro.!");
		}
	}

	public void modificarParametro() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			newParametro.setEstado(estado);
			parametroRegistration.update(newParametro);

			FacesUtil.infoMessage("Parametro Modificada!", newParametro.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo modificar la parametro.!");
		}
	}

	public void eliminarParametro() {
		try {
			newParametro.setEstado("RM");
			parametroRegistration.update(newParametro);

			FacesUtil.infoMessage("Parametro Eliminada!", newParametro.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo eliminar la parametro.!");
		}

	}

	public void onRowSelectParametro(SelectEvent event) {
		log.info("onRowSelectParametro -> selectedParametro:"
				+ selectedParametro.getNombre());
		crear = false;
		modificar = true;
		registrar = false;
		newParametro = selectedParametro;
		FacesUtil
				.updateComponent("formTableDosificacion:dataTableDosificacion");
	}


	// -------- acciones para la vista----------

	public void cambiarAspecto() {
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

	public Parametro getSelectedParametro() {
		return selectedParametro;
	}

	public void setSelectedParametro(Parametro selectedParametro) {
		this.selectedParametro = selectedParametro;
	}

	public String getTest() {
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

	public List<Parametro> getListFilterParametro() {
		return listFilterParametro;
	}

	public void setListFilterParametro(List<Parametro> listFilterParametro) {
		this.listFilterParametro = listFilterParametro;
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

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Parametro getNewParametro() {
		return newParametro;
	}

	public void setNewParametro(Parametro newParametro) {
		this.newParametro = newParametro;
	}

	public List<Parametro> getListParametro() {
		return listParametro;
	}

	public void setListParametro(List<Parametro> listParametro) {
		this.listParametro = listParametro;
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

	public List<Pais> getListPais() {
		return listPais;
	}

	public void setListPais(List<Pais> listPais) {
		this.listPais = listPais;
	}

	

}
