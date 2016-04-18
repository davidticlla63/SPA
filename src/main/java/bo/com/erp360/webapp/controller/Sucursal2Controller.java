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
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.DosificacionRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.DosificacionRegistration;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.SucursalRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "sucursalController")
@SuppressWarnings("serial")
@ConversationScoped
public class Sucursal2Controller implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private EmpresaRepository empresaRepository;

	@Inject
	private SucursalRegistration sucursalRegistration;

	@Inject
	private DosificacionRegistration dosificacionRegistration;

	@Inject
	private SucursalRepository sucursalRepository;

	@Inject
	private DosificacionRepository dosificacionRepository;

	@Inject
	private UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(Sucursal2Controller.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	//estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean seleccionadaDosificacion;
	private boolean estadoButtonDialog;

	private String nombreEstado="ACTIVO";
	private String tipoColumnTable; //8

	private List<Sucursal> listSucursal;
	private List<Sucursal> listFilterSucursal;
	private String[] listEstado = {"ACTIVO","INACTIVO"};
	private String[] listResolucionNormativa = {"NSF-07","SFV-14"};
	private List<Dosificacion> listDosificacion;
	private List<Dosificacion> listDosificacionDelete;

	private Sucursal newSucursal;
	private Sucursal selectedSucursal;
	private Dosificacion newDosificacion;
	private Dosificacion selectedDosificacion;

	//login
	private Usuario usuario;
	private String nombreUsuario;	
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	private Date fechaMinima;


	@PostConstruct
	public void initNewSucursal() {
		log.info(" init new initNewSucursal controller");
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
		newDosificacion = new Dosificacion(); 
		newSucursal = new Sucursal();
		selectedDosificacion = new Dosificacion();
		selectedSucursal = new Sucursal();
		listDosificacion = new ArrayList<Dosificacion>();
		listDosificacionDelete = new ArrayList<Dosificacion>();

		// traer todos las sucursales
		listSucursal = sucursalRepository.findAllByEmpresa(empresaLogin);
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

	//-----  metodos sucursal ---------------

	public void registrarSucursal(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newSucursal.setEstado(estado);
			newSucursal.setUsuarioRegistro(nombreUsuario);
			newSucursal.setFechaRegistro(new Date());
			newSucursal = sucursalRegistration.create(newSucursal);

			for(Dosificacion d : listDosificacion){
				d.setSucursal(newSucursal);
				dosificacionRegistration.create(d);
			}
			FacesUtil.infoMessage("Sucursal Registrada!",newSucursal.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo registrar la sucursal.!");
		}
	}

	public void modificarSucursal(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newSucursal.setEstado(estado);
			sucursalRegistration.update(newSucursal);
			for(Dosificacion d : listDosificacion){
				log.info("id dosificacion "+d.getId());
				if(d.getId()<0){//las sucursales nuevas estan con id negativo
					log.info("newSucursal id= "+ newSucursal.getId());
					d.setId(0);
					d.setSucursal(newSucursal);
					dosificacionRegistration.create(d);
				}else{
					dosificacionRegistration.update(d);
				}
			}
			for(Dosificacion d : listDosificacionDelete){
				if(d.getId()>0){
					dosificacionRegistration.update(d);
				}
			}
			FacesUtil.infoMessage("Sucursal Modificada!",newSucursal.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo modificar la sucursal.!");
		}
	}

	public void eliminarSucursal(){
		try{
			newSucursal.setEstado("RM");
			sucursalRegistration.update(newSucursal);
			for(Dosificacion d : listDosificacion){
				if(d.getId()>0){
					d.setEstado("RM");
					dosificacionRegistration.update(d);
				}
			}
			for(Dosificacion d : listDosificacionDelete){
				if(d.getId()>0){
					dosificacionRegistration.update(d);
				}
			}
			FacesUtil.infoMessage("Sucursal Eliminada!",newSucursal.getNombre());
			loadDefault();
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo eliminar la sucursal.!");
		}

	}

	public void onRowSelectSucursal(SelectEvent event) {
		log.info("onRowSelectSucursal -> selectedSucursal:"+selectedSucursal.getNombre());
		crear = false;
		modificar = true;
		registrar = false ;
		newSucursal = selectedSucursal;
		listDosificacion = dosificacionRepository.findAllActivasBySucursal(selectedSucursal);
		log.info("listDosificacion "+listDosificacion.size());
		FacesUtil.updateComponent("formTableDosificacion:dataTableDosificacion");
	}

	//-------  metodos dosificacion -----------

	public void agregarDosificacion(){
		try{
			log.info("agregarDosificacion()");
			log.info("getLlaveControl() "+newDosificacion.getLlaveControl());
			int neg = (listDosificacion.size()+1) * (-1);
			log.info("neg : "+neg);
			newDosificacion.setId(neg);
			newDosificacion.setEstado("AC");
			newDosificacion.setActivo(false);
			newDosificacion.setUsuarioRegistro(nombreUsuario);
			///newDosificacion.setSucursal(sucursal);//verificar si es nueva sucursal o esta modificando sucursal
			newDosificacion.setFechaRegistro(new Date());
			//newDosificacion.setLlaveControl(listDosificacion.size()* (-1));
			if(listDosificacion==null){
				listDosificacion=new ArrayList<>();
			}
			boolean existe=false;
			for (Dosificacion dosificacion : listDosificacion) {
				if(dosificacion.getLlaveControl().equals(newDosificacion.getLlaveControl())){
					existe=true;
				}
			}
			if(existe){
				FacesUtil.warnMessage("Llave de dosificacion duplicada.!");
			}else{
				listDosificacion.add(newDosificacion);
				newDosificacion = new Dosificacion();
				FacesUtil.infoMessage("Dosificacion Agregada!","");

				FacesUtil.hideDialog("dlgDosificacion");
			}
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo registrar la dosificacion.!");
		}

	}

	public void modificarDosificacion(){
		log.info("modificarDosificacion");
		try {
			for (Dosificacion dosificacion : listDosificacion) {
				if(dosificacion.getLlaveControl().equals(newDosificacion.getLlaveControl())){
					dosificacion=newDosificacion;
					break;
				}
			}
			newDosificacion=new Dosificacion();
			FacesUtil.infoMessage("Dosificacion Modificada!","");
			FacesUtil.hideDialog("dlgDosificacion");
		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo modificar la dosificacion.!");
		}
	}

	public void eliminarDosificacion(){
		try{

		}catch(Exception e){
			FacesUtil.warnMessage("No se pudo eliminar la dosificacion.!");
		}
	}

	public void onRowSelectDosificacion(SelectEvent event) {
		newDosificacion = selectedDosificacion;
		seleccionadaDosificacion = true;
		log.info("onRowSelectDosificacion -> selectedDosificacion:"+selectedDosificacion.getLlaveControl());
	}

	//------ acciones dosificacion -----------

	public void buttonCancelarDosificacion(){
		selectedDosificacion = new Dosificacion();
		newDosificacion = new Dosificacion();
		seleccionadaDosificacion = false;
	}

	public void buttonAgregarDosificacion(){
		estadoButtonDialog = true;
		FacesUtil.showDialog("dlgDosificacion");
	}

	public void buttonActivarDosificacion(){
		for (Dosificacion dosificacion : listDosificacion) {
			if(dosificacion.getLlaveControl().equals(selectedDosificacion.getLlaveControl())){
				dosificacion.setActivo(true);
			}else{
				dosificacion.setActivo(false);
			}
		}
		seleccionadaDosificacion = false;
		newDosificacion = new Dosificacion();
		selectedDosificacion = new Dosificacion();
		FacesUtil.updateComponent("formTableDosificacion:dataTableDosificacion");
		FacesUtil.infoMessage("Dosificacion Activada","");
	}

	public void buttonModificarDosificacion(){
		estadoButtonDialog = false;
		FacesUtil.showDialog("dlgDosificacion");
	}

	public void buttonEliminarDosificacion(){
		if(selectedDosificacion!=null){
			for (Dosificacion dosificacion : listDosificacion) {
				if(dosificacion.getLlaveControl().equals(selectedDosificacion.getLlaveControl())){
					listDosificacion.remove(dosificacion);
					if(listDosificacionDelete==null){
						listDosificacionDelete=new ArrayList<>();
					}
					dosificacion.setEstado("RM");
					dosificacion.setActivo(false);
					listDosificacionDelete.add(dosificacion);
					break;
				}
			}
			FacesUtil.updateComponent("formTableDosificacion:dataTableDosificacion");
		}else{
			FacesUtil.warnMessage("Seleccione una dosificacion");
		}
		seleccionadaDosificacion = false;
		newDosificacion = new Dosificacion();
		selectedDosificacion = new Dosificacion();
	}

	// ----- acciones dialog dosificacion ---------

	public void dialogCancelarDosificacion(){
		seleccionadaDosificacion = false;
		selectedDosificacion = new Dosificacion();
		newDosificacion = new Dosificacion();
		FacesUtil.updateComponent("formTableDosificacion:dataTableDosificacion");
		FacesUtil.hideDialog("dlgDosificacion");
	}

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

	public Sucursal getSelectedSucursal() {
		return selectedSucursal;
	}

	public void setSelectedSucursal(Sucursal selectedSucursal) {
		this.selectedSucursal = selectedSucursal;
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

	public List<Sucursal> getListFilterSucursal() {
		return listFilterSucursal;
	}

	public void setListFilterSucursal(List<Sucursal> listFilterSucursal) {
		this.listFilterSucursal = listFilterSucursal;
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

	public List<Dosificacion> getListDosificacion() {
		return listDosificacion;
	}

	public void setListDosificacion(List<Dosificacion> listDosificacion) {
		this.listDosificacion = listDosificacion;
	}

	public Dosificacion getNewDosificacion() {
		return newDosificacion;
	}

	public void setNewDosificacion(Dosificacion newDosificacion) {
		this.newDosificacion = newDosificacion;
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

	public Sucursal getNewSucursal() {
		return newSucursal;
	}

	public void setNewSucursal(Sucursal newSucursal) {
		this.newSucursal = newSucursal;
	}

	public List<Dosificacion> getListDosificacionDelete() {
		return listDosificacionDelete;
	}

	public void setListDosificacionDelete(List<Dosificacion> listDosificacionDelete) {
		this.listDosificacionDelete = listDosificacionDelete;
	}

	public List<Sucursal> getListSucursal() {
		return listSucursal;
	}

	public void setListSucursal(List<Sucursal> listSucursal) {
		this.listSucursal = listSucursal;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public Dosificacion getSelectedDosificacion() {
		return selectedDosificacion;
	}

	public void setSelectedDosificacion(Dosificacion selectedDosificacion) {
		this.selectedDosificacion = selectedDosificacion;
	}

	public boolean isEstadoButtonDialog() {
		return estadoButtonDialog;
	}

	public void setEstadoButtonDialog(boolean estadoButtonDialog) {
		this.estadoButtonDialog = estadoButtonDialog;
	} 

}
