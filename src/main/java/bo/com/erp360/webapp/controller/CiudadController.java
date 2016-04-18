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
import bo.com.erp360.webapp.data.CiudadRepository;
import bo.com.erp360.webapp.data.PaisRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Ciudad;
import bo.com.erp360.webapp.model.Pais;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.CiudadRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "ciudadController")
@SuppressWarnings("serial")
@ConversationScoped
public class CiudadController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;

	private @Inject CiudadRegistration ciudadRegistration;

	private @Inject CiudadRepository ciudadRepository;

	private @Inject UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(CiudadController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventCiudad;

	// estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean estadoButtonDialog;

	private String nombreEstado = "ACTIVO";
	private String tipoColumnTable; // 8

	private @Inject PaisRepository paisRepository;
	private List<Pais> listPais;

	private List<Ciudad> listCiudad;
	private List<Ciudad> listFilterCiudad;
	private String[] listEstado = { "ACTIVO", "INACTIVO" };
	private String[] listResolucionNormativa = { "NSF-07", "SFV-14" };

	private Ciudad newCiudad;
	private Ciudad selectedCiudad;

	// login
	private Usuario usuario;
	private String nombreUsuario;
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	@PostConstruct
	public void initNewCiudad() {
		log.info(" init new initNewCiudad controller");
		beginConversation();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		usuario = estadoUsuarioLogin.getUsuarioSession(usuarioRepository);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);
		loadDefault();
	}

	public void loadDefault() {
		crear = true;
		registrar = false;
		modificar = false;
		estadoButtonDialog = true;
		tipoColumnTable = "col-md-12";
		newCiudad = new Ciudad();
		selectedCiudad = new Ciudad();
		listPais = paisRepository.findAllActivas();
		listCiudad= ciudadRepository.findAllActivas();

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

	
	
	// ----- metodos ciudad ---------------

	public void registrarCiudad() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			newCiudad.setState(estado);
			newCiudad.setUsuarioRegistro(nombreUsuario);
			newCiudad.setFechaRegistro(new Date());
			newCiudad = ciudadRegistration.create(newCiudad);

			FacesUtil.infoMessage("Ciudad Registrada!", newCiudad.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo registrar la ciudad.!");
		}
	}

	public void modificarCiudad() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			newCiudad.setState(estado);
			ciudadRegistration.update(newCiudad);

			FacesUtil.infoMessage("Ciudad Modificada!", newCiudad.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo modificar la ciudad.!");
		}
	}

	public void eliminarCiudad() {
		try {
			newCiudad.setState("RM");
			ciudadRegistration.update(newCiudad);

			FacesUtil.infoMessage("Ciudad Eliminada!", newCiudad.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo eliminar la ciudad.!");
		}

	}

	public void onRowSelectCiudad(SelectEvent event) {
		log.info("onRowSelectCiudad -> selectedCiudad:"
				+ selectedCiudad.getNombre());
		crear = false;
		modificar = true;
		registrar = false;
		newCiudad = selectedCiudad;
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

	public Ciudad getSelectedCiudad() {
		return selectedCiudad;
	}

	public void setSelectedCiudad(Ciudad selectedCiudad) {
		this.selectedCiudad = selectedCiudad;
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

	public List<Ciudad> getListFilterCiudad() {
		return listFilterCiudad;
	}

	public void setListFilterCiudad(List<Ciudad> listFilterCiudad) {
		this.listFilterCiudad = listFilterCiudad;
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

	public Ciudad getNewCiudad() {
		return newCiudad;
	}

	public void setNewCiudad(Ciudad newCiudad) {
		this.newCiudad = newCiudad;
	}

	public List<Ciudad> getListCiudad() {
		return listCiudad;
	}

	public void setListCiudad(List<Ciudad> listCiudad) {
		this.listCiudad = listCiudad;
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
