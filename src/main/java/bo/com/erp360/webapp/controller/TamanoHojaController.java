package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.New;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.TamanoHojaRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.TamanoHoja;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.TamanoHojaRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "tamanoHojaController")
@SuppressWarnings("serial")
@ConversationScoped
public class TamanoHojaController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;

	private @Inject TamanoHojaRegistration tamanoHojaRegistration;

	private @Inject TamanoHojaRepository tamanoHojaRepository;

	private @Inject UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(TamanoHojaController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventTamanoHoja;

	// estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean seleccionadaDosificacion;
	private boolean estadoButtonDialog;

	private String nombreEstado = "ACTIVO";
	private String tipoColumnTable; // 8

	private List<TamanoHoja> listTamanoHoja;
	private List<TamanoHoja> listFilterTamanoHoja;
	private String[] listEstado = { "ACTIVO", "INACTIVO" };
	private String[] listResolucionNormativa = { "NSF-07", "SFV-14" };

	private TamanoHoja newTamanoHoja;
	private TamanoHoja selectedTamanoHoja;

	// login
	private Usuario usuario;
	private String nombreUsuario;
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	private Date fechaMinima;

	@PostConstruct
	public void initNewTamanoHoja() {
		log.info(" init new initNewTamanoHoja controller");
		beginConversation();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		usuario = estadoUsuarioLogin.getUsuarioSession(usuarioRepository);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);

		fechaMinima = new Date();
		loadDefault();
	}

	public void loadDefault() {
		crear = true;
		registrar = false;
		modificar = false;
		seleccionadaDosificacion = false;
		estadoButtonDialog = true;
		tipoColumnTable = "col-md-12";
		newTamanoHoja = new TamanoHoja();
		selectedTamanoHoja = new TamanoHoja();

		// traer todos las tamanoHojaes
		listTamanoHoja = tamanoHojaRepository.findAllActivas();
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

	// ----- metodos tamanoHoja ---------------

	public void registrarTamanoHoja() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			newTamanoHoja.setEstado(estado);
			newTamanoHoja.setUsuarioRegistro(nombreUsuario);
			newTamanoHoja.setFechaRegistro(new Date());
			if (newTamanoHoja.getEstado().equals("AC")) {
				for (TamanoHoja tamanoHoja : listTamanoHoja) {
					if (tamanoHoja.getEstado().equals("AC")) {
						tamanoHoja.setEstado("IN");
						tamanoHojaRegistration.update(tamanoHoja);
					}
				}
			}
			newTamanoHoja.setEstado(estado);
			newTamanoHoja = tamanoHojaRegistration.create(newTamanoHoja);

			FacesUtil.infoMessage("TamanoHoja Registrada!",
					newTamanoHoja.getTamano());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo registrar la tamanoHoja.!");
		}
	}

	public void modificarTamanoHoja() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			newTamanoHoja.setEstado(estado);
			if (newTamanoHoja.getEstado().equals("AC")) {
				for (TamanoHoja tamanoHoja : listTamanoHoja) {
					if (tamanoHoja.getEstado().equals("AC")) {
						tamanoHoja.setEstado("IN");
						tamanoHojaRegistration.update(tamanoHoja);
					}
				}
			}
			newTamanoHoja.setEstado(estado);
			tamanoHojaRegistration.update(newTamanoHoja);

			FacesUtil.infoMessage("TamanoHoja Modificada!",
					newTamanoHoja.getTamano());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo modificar la tamanoHoja.!");
		}
	}

	public void eliminarTamanoHoja() {
		try {
			newTamanoHoja.setEstado("RM");

			tamanoHojaRegistration.update(newTamanoHoja);

			FacesUtil.infoMessage("TamanoHoja Eliminada!",
					newTamanoHoja.getTamano());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo eliminar la tamanoHoja.!");
		}

	}

	public void onRowSelectTamanoHoja(SelectEvent event) {
		log.info("onRowSelectTamanoHoja -> selectedTamanoHoja:"
				+ selectedTamanoHoja.getTamano());
		crear = false;
		modificar = true;
		registrar = false;
		newTamanoHoja = selectedTamanoHoja;
		FacesUtil
				.updateComponent("formTableDosificacion:dataTableDosificacion");
	}

	// ------- metodos dosificacion -----------

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

	public TamanoHoja getSelectedTamanoHoja() {
		return selectedTamanoHoja;
	}

	public void setSelectedTamanoHoja(TamanoHoja selectedTamanoHoja) {
		this.selectedTamanoHoja = selectedTamanoHoja;
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

	public List<TamanoHoja> getListFilterTamanoHoja() {
		return listFilterTamanoHoja;
	}

	public void setListFilterTamanoHoja(List<TamanoHoja> listFilterTamanoHoja) {
		this.listFilterTamanoHoja = listFilterTamanoHoja;
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

	public TamanoHoja getNewTamanoHoja() {
		return newTamanoHoja;
	}

	public void setNewTamanoHoja(TamanoHoja newTamanoHoja) {
		this.newTamanoHoja = newTamanoHoja;
	}

	public List<TamanoHoja> getListTamanoHoja() {
		return listTamanoHoja;
	}

	public void setListTamanoHoja(List<TamanoHoja> listTamanoHoja) {
		this.listTamanoHoja = listTamanoHoja;
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
