package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
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
import bo.com.erp360.webapp.data.GrupoProductoRepository;
import bo.com.erp360.webapp.data.ProductoRepository;
import bo.com.erp360.webapp.data.TipoProductoRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.GrupoProducto;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.TipoProducto;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.ProductoRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "productoController")
@SuppressWarnings("serial")
@ConversationScoped
public class ProductoController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;
	
	private @Inject ProductoRegistration productoRegistration;

	private @Inject ProductoRepository productoRepository;
	
	private @Inject UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(ProductoController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventProducto;

	//estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean seleccionadaDosificacion;
	private boolean estadoButtonDialog;

	private String nombreEstado="ACTIVO";
	private String tipoColumnTable; //8

	private List<Producto> listProducto;
	private List<Producto> listFilterProducto;
	private String[] listEstado = {"ACTIVO","INACTIVO"};
	private String[] listResolucionNormativa = {"NSF-07","SFV-14"};

	private Producto newProducto;
	private Producto selectedProducto;

	//login
	private Usuario usuario;
	private String nombreUsuario;	
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	private Date fechaMinima;
	
	// tipo de Producto
	private List<TipoProducto> listTipoProducto= new ArrayList<TipoProducto>();
	private @Inject TipoProductoRepository tipoProductoRepository;
	
	// tipo de Producto
		private List<GrupoProducto> listGrupoProducto= new ArrayList<GrupoProducto>();
		private @Inject GrupoProductoRepository   grupoProductoRepository;

	@PostConstruct
	public void initNewProducto() {
		log.info(" init new initNewProducto controller");
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
		newProducto = new Producto();
		
		selectedProducto = new Producto();

		// traer todos las productoes
		listProducto = productoRepository.findAllActivas(empresaLogin);
		
		//traer todos los tipo de productos activos
		listTipoProducto= tipoProductoRepository.findAllActivasByEmpresa(empresaLogin);
		
		listGrupoProducto= grupoProductoRepository.findAllActivasByEmpresa(empresaLogin);
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

	//-----  metodos producto ---------------

	public void registrarProducto(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newProducto.setEstado(estado);
			newProducto.setUsuarioRegistro(nombreUsuario);
			newProducto.setFechaRegistro(new Date());
			newProducto.setEmpresa(empresaLogin);
			newProducto.setUnidadMedida(null);
			newProducto.setGrupoProducto(null);
			newProducto.setProveedor(null);
			
			newProducto = productoRegistration.create(newProducto);
			
			FacesUtil.infoMessage("Producto Registrada!",newProducto.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo registrar la producto.!");
		}
	}

	public void modificarProducto(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newProducto.setEstado(estado);
			newProducto.setEmpresa(empresaLogin);
			newProducto.setUnidadMedida(null);
			productoRegistration.update(newProducto);
			
			FacesUtil.infoMessage("Producto Modificada!",newProducto.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo modificar la producto.!");
		}
	}

	public void eliminarProducto(){
		try{
			newProducto.setEstado("RM");
			productoRegistration.update(newProducto);
			
			FacesUtil.infoMessage("Producto Eliminada!",newProducto.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo eliminar la producto.!");
		}

	}

	public void onRowSelectProducto(SelectEvent event) {
		log.info("onRowSelectProducto -> selectedProducto:"+selectedProducto.getNombre());
		crear = false;
		modificar = true;
		registrar = false ;
		newProducto = selectedProducto;
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

	public Producto getSelectedProducto() {
		return selectedProducto;
	}

	public void setSelectedProducto(Producto selectedProducto) {
		this.selectedProducto = selectedProducto;
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

	public List<Producto> getListFilterProducto() {
		return listFilterProducto;
	}

	public void setListFilterProducto(List<Producto> listFilterProducto) {
		this.listFilterProducto = listFilterProducto;
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

	public Producto getNewProducto() {
		return newProducto;
	}

	public void setNewProducto(Producto newProducto) {
		this.newProducto = newProducto;
	}

	public List<Producto> getListProducto() {
		return listProducto;
	}

	public void setListProducto(List<Producto> listProducto) {
		this.listProducto = listProducto;
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

	public List<TipoProducto> getListTipoProducto() {
		return listTipoProducto;
	}

	public void setListTipoProducto(List<TipoProducto> listTipoProducto) {
		this.listTipoProducto = listTipoProducto;
	}

	public List<GrupoProducto> getListGrupoProducto() {
		return listGrupoProducto;
	}

	public void setListGrupoProducto(List<GrupoProducto> listGrupoProducto) {
		this.listGrupoProducto = listGrupoProducto;
	} 

}
