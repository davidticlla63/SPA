package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.GrupoProductoRepository;
import bo.com.erp360.webapp.data.LineaRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.GrupoProducto;
import bo.com.erp360.webapp.model.Linea;
import bo.com.erp360.webapp.model.TipoServicio;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.GrupoProductoRegistration;
import bo.com.erp360.webapp.service.LineaRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "grupoProductoController")
@SuppressWarnings("serial")
@ConversationScoped
public class GrupoProductoController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;

	private @Inject GrupoProductoRegistration grupoProductoRegistration;

	private @Inject GrupoProductoRepository grupoProductoRepository;

	private @Inject UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(GrupoProductoController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventGrupoProducto;

	// estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean seleccionadaDosificacion;
	private boolean estadoButtonDialog;

	private String nombreEstado = "ACTIVO";
	private String tipoColumnTable; // 8

	private List<GrupoProducto> listGrupoProducto;
	private List<GrupoProducto> listFilterGrupoProducto;
	private String[] listEstado = { "ACTIVO", "INACTIVO" };
	private String[] listResolucionNormativa = { "NSF-07", "SFV-14" };

	private GrupoProducto newGrupoProducto;
	private GrupoProducto selectedGrupoProducto;

	// login
	private Usuario usuario;
	private String nombreUsuario;
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	private Date fechaMinima;
	
	
	//linea de Producto
	
	private List<Linea> listLinea = new ArrayList<Linea>();

	private @Inject LineaRegistration lineaRegistration;

	private @Inject LineaRepository lineaRepository;

	private String textoAutoCompleteLinea = "";

	private Linea linea;

	@PostConstruct
	public void initNewGrupoProducto() {
		log.info(" init new initNewGrupoProducto controller");
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
		newGrupoProducto = new GrupoProducto();
		selectedGrupoProducto = new GrupoProducto();
		
		listLinea= lineaRepository.findAllActivasByEmpresa(empresaLogin);

		// traer todos las grupoProductoes
		listGrupoProducto = grupoProductoRepository
				.findAllByEmpresa(empresaLogin);
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

	// ----- metodos grupoProducto ---------------

	public void registrarGrupoProducto() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";

			if (textoAutoCompleteLinea.trim().length() == 0) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Revisar y llenar", "Linea de Producto!");
				facesContext.addMessage(null, m);
				return;
			}
			if (!existeLinea()) {
				linea = new Linea();
				linea.setNombre(textoAutoCompleteLinea);
				linea.setEmpresa(empresaLogin);
				linea.setState("AC");
				linea.setUsuarioRegistro(nombreUsuario);
				linea.setFechaRegistro(new Date());
				linea = lineaRegistration.create(linea);
			}
			newGrupoProducto.setLinea(linea);
			newGrupoProducto.setState(estado);
			newGrupoProducto.setUsuarioRegistro(nombreUsuario);
			newGrupoProducto.setFechaRegistro(new Date());
			newGrupoProducto.setEmpresa(empresaLogin);
			newGrupoProducto = grupoProductoRegistration
					.create(newGrupoProducto);

			FacesUtil.infoMessage("GrupoProducto Registrada!",
					newGrupoProducto.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo registrar la grupoProducto.!");
		}
	}

	public void modificarGrupoProducto() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";

			if (textoAutoCompleteLinea.trim().length() == 0) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Revisar y llenar", "Linea de Producto!");
				facesContext.addMessage(null, m);
				return;
			}
			if (!existeLinea()) {
				linea = new Linea();
				linea.setNombre(textoAutoCompleteLinea);
				linea.setEmpresa(empresaLogin);
				linea.setState("AC");
				linea.setUsuarioRegistro(nombreUsuario);
				linea.setFechaRegistro(new Date());
				linea = lineaRegistration.create(linea);
			}
			newGrupoProducto.setLinea(linea);
			newGrupoProducto.setState(estado);
			newGrupoProducto.setEmpresa(empresaLogin);
			newGrupoProducto.setFechaModificacion(new Date());
			grupoProductoRegistration.update(newGrupoProducto);

			FacesUtil.infoMessage("GrupoProducto Modificada!",
					newGrupoProducto.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo modificar la grupoProducto.!");
		}
	}

	public void eliminarGrupoProducto() {
		try {
			newGrupoProducto.setState("RM");
			grupoProductoRegistration.update(newGrupoProducto);

			FacesUtil.infoMessage("GrupoProducto Eliminada!",
					newGrupoProducto.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo eliminar la grupoProducto.!");
		}

	}

	public void onRowSelectGrupoProducto(SelectEvent event) {
		log.info("onRowSelectGrupoProducto -> selectedGrupoProducto:"
				+ selectedGrupoProducto.getNombre());
		crear = false;
		modificar = true;
		registrar = false;
		newGrupoProducto = selectedGrupoProducto;
		textoAutoCompleteLinea=newGrupoProducto.getLinea().getNombre();
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

	

	// Linea Producto

	public List<String> completeTextLinea(String query) {
		List<String> results = new ArrayList<String>();
		log.info("size : " + listLinea.size());
		for (Linea i : listLinea) {
			if (i.getNombre().startsWith(query.toUpperCase())) {
				results.add(i.getNombre());
			}
		}
		return results;
	}

	public void onItemSelectLinea(SelectEvent event) {
		String nits = event.getObject().toString();
		for (Linea i : listLinea) {
			if (i.getNombre().equals(nits.toUpperCase())) {
				setTextoAutoCompleteLinea(i.getNombre());
			}
		}
	}

	private boolean existeLinea() {
		try {
			log.info("Ingreso a existeTipoProducto");

			List<Linea> list = lineaRepository.findAllActivasByEmpresaName(
					empresaLogin, textoAutoCompleteLinea.toUpperCase());
			if (list.size() > 0) {
				linea = list.get(0);
			}
			return list.size() > 0;
		} catch (Exception e) {
			log.error("Error en existeTipoProducto : " + e.getMessage());
		}
		return false;
	}

	// -------- get and set---------------------

	public boolean isModificar() {
		return modificar;
	}

	public void setModificar(boolean modificar) {
		this.modificar = modificar;
	}

	public GrupoProducto getSelectedGrupoProducto() {
		return selectedGrupoProducto;
	}

	public void setSelectedGrupoProducto(GrupoProducto selectedGrupoProducto) {
		this.selectedGrupoProducto = selectedGrupoProducto;
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

	public List<GrupoProducto> getListFilterGrupoProducto() {
		return listFilterGrupoProducto;
	}

	public void setListFilterGrupoProducto(
			List<GrupoProducto> listFilterGrupoProducto) {
		this.listFilterGrupoProducto = listFilterGrupoProducto;
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

	public GrupoProducto getNewGrupoProducto() {
		return newGrupoProducto;
	}

	public void setNewGrupoProducto(GrupoProducto newGrupoProducto) {
		this.newGrupoProducto = newGrupoProducto;
	}

	public List<GrupoProducto> getListGrupoProducto() {
		return listGrupoProducto;
	}

	public void setListGrupoProducto(List<GrupoProducto> listGrupoProducto) {
		this.listGrupoProducto = listGrupoProducto;
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

	public Linea getLinea() {
		return linea;
	}

	public void setLinea(Linea linea) {
		this.linea = linea;
	}

	public List<Linea> getListLinea() {
		return listLinea;
	}

	public void setListLinea(List<Linea> listLinea) {
		this.listLinea = listLinea;
	}

	public String getTextoAutoCompleteLinea() {
		return textoAutoCompleteLinea;
	}

	public void setTextoAutoCompleteLinea(String textoAutoCompleteLinea) {
		this.textoAutoCompleteLinea = textoAutoCompleteLinea;
	}

}
