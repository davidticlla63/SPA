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
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.CentroCostoRepository;
import bo.com.erp360.webapp.data.PlanCuentaRepository;
import bo.com.erp360.webapp.data.ServicioRepository;
import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.PrecioServicio;
import bo.com.erp360.webapp.model.Servicio;
import bo.com.erp360.webapp.service.ServicioRegistration;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "servicioController")
@ConversationScoped
public class ServicioController implements Serializable {

	private static final long serialVersionUID = -5800385585606111239L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private PlanCuentaRepository planCuentaRepository;

	@Inject
	private CentroCostoRepository centroCostoRepository;

	@Inject
	private ServicioRepository servicioRepository;

	@Inject
	private ServicioRegistration servicioRegistration;
	
	private Logger log = Logger.getLogger(this.getClass());

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	//estados
	private boolean crear = true;
	private boolean registrar = false;
	private boolean modificar = false;
	private boolean masPrecio = false;

	private String tituloPanel = "Registrar Servicio";
	private String nombreEstado = "ACTIVO";
	private String tipoColumnTable = "col-md-12"; //8
	private String nombreUsuario;
	private String textoAutoCompleteCuenta;
	private String textoAutoCompleteCentroCosto;

	private List<Servicio> listServicio = new ArrayList<Servicio>();
	private List<Servicio> listFilterServicio = new ArrayList<Servicio>();
	private List<PlanCuenta> listCuentasAuxiliares = new ArrayList<PlanCuenta>();
	private List<CentroCosto> listCentroCosto = new ArrayList<CentroCosto>();
	private List<PrecioServicio> listPrecioServicio = new ArrayList<PrecioServicio>();
	private String[] listEstados = {"ACTIVO","INACTIVO"};	

	@Produces
	@Named
	private Servicio newServicio;
	private Servicio selectedServicio;
	private PlanCuenta busquedaCuenta;
	private CentroCosto busquedaCentroCosto;

	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@PostConstruct
	public void initNewServicio() {

		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		// tituloPanel
		tituloPanel = "Servicio";
		masPrecio = false;
		listServicio = servicioRepository.findAllByEmpresa(empresaLogin);		
		newServicio = new Servicio();
		busquedaCentroCosto = new CentroCosto();
		busquedaCuenta = new PlanCuenta();
		textoAutoCompleteCentroCosto = "";
		textoAutoCompleteCuenta = "";
		nombreEstado = "ACTIVO";
		selectedServicio = new Servicio();
		cargarListCuentasAuxiliares();
		cargarListCentroCosto();
	}

	private void cargarListCuentasAuxiliares(){
		try{
			listCuentasAuxiliares = planCuentaRepository.findAllAuxiliarByEmpresa(empresaLogin,gestionLogin);
		}catch(Exception e){
			log.error("Error en cargarListCuentasAuxiliares : "+e.getMessage());
			e.getStackTrace();
		}
	}

	private void cargarListCentroCosto(){
		try{
			listCentroCosto = centroCostoRepository.findAllCentroCostoByEmpresa(empresaLogin);
		}catch(Exception e){
			System.err.println("Error en cargarListCentroCosto : "+e.getMessage());
			e.getStackTrace();
		}
	}

//	private void unselectedRowDataTable(){
//		DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form:cablePathTable");
//		//dataTable.setRowIndex(2);
//		dataTable.setSelection(null);
//	}

	@Produces
	@Named
	public List<Servicio> getListServicio() {
		return listServicio;
	}

	public void obtenerUsuarios(){
		try {
			log.info("Ingreso a obtenerUsuarios");
			//listUsuario = usuarioRepository.traerUsuariosPorSucursal(almacenSucursal.getSucursal());
		} catch (Exception e) {
			log.error("Error en obtenerUsuarios : "+e.getMessage());
			e.getStackTrace();
		}
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

	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void registrar(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newServicio.setEstado(estado);
			newServicio.setCuenta(busquedaCuenta);
			newServicio.setCentroCosto(busquedaCentroCosto);
			newServicio.setFechaRegistro(new Date());
			newServicio.setUsuarioRegistro(nombreUsuario);
			newServicio.setEmpresa(empresaLogin);
			if(!newServicio.validate(facesContext, empresaLogin, gestionLogin)){
				resetearFitrosTabla("formTableServicio:dataTableServicio");
				return;
			}
			servicioRegistration.create(newServicio);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Servicio Registrado!", newServicio.getNombre());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTableServicio:dataTableServicio");
			initNewServicio();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void modificar(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newServicio.setEstado(estado);
			newServicio.setCuenta(busquedaCuenta!=null?busquedaCuenta:null);
			newServicio.setCentroCosto(busquedaCentroCosto!=null?busquedaCentroCosto:null);
			newServicio.setFechaRegistro(new Date());
			newServicio.setUsuarioRegistro(nombreUsuario);
			servicioRegistration.update(newServicio);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Servicio Modificado!", newServicio.getNombre());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTableServicio:dataTableServicio");
			initNewServicio();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Modificado Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminar(){
		try{
			newServicio.setEstado("RM");
			servicioRegistration.update(newServicio);
			resetearFitrosTabla("formTableServicio:dataTableServicio");
			initNewServicio();
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Servicio eliminado!", newServicio.getNombre());
			facesContext.addMessage(null, m);
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Eliminacion Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public List<PlanCuenta> completeTextCuenta(String query) {
		String upperQuery = query.toUpperCase();
		List<PlanCuenta> results = new ArrayList<PlanCuenta>();
		for(PlanCuenta i : listCuentasAuxiliares) {
			if(i.getDescripcion().toUpperCase().startsWith(upperQuery)){
				results.add(i);
			}
		}         
		return results;
	}


	public List<CentroCosto> completeTextCentroCosto(String query) {
		String upperQuery = query.toUpperCase();
		List<CentroCosto> results = new ArrayList<CentroCosto>();
		for(CentroCosto i : listCentroCosto) {
			if(i.getNombre().toUpperCase().startsWith(upperQuery)){
				results.add(i);
			}
		}         
		return results;
	}

	public void onItemSelectCuenta(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(PlanCuenta s : listCuentasAuxiliares){
			if(s.getDescripcion().equals(nombre)){
				busquedaCuenta = s;
			}
		}
	}

	public void onItemSelectCentroCosto(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(CentroCosto s : listCentroCosto){
			if(s.getNombre().equals(nombre)){
				busquedaCentroCosto= s;
			}
		}
	}
	
	private String getRootErrorMessage(Exception e) {
		// Default to general error message that registration failed.
		String errorMessage = "Registration failed. See server log for more information";
		if (e == null) {
			// This shouldn't happen, but return the default messages
			return errorMessage;
		}

		// Start with the exception and recurse to find the root cause
		Throwable t = e;
		while (t != null) {
			// Get the message from the Throwable class instance
			errorMessage = t.getLocalizedMessage();
			t = t.getCause();
		}
		// This is the root cause message
		return errorMessage;
	}

	public void actualizarFormReg(){
		crear = true;
		registrar = false;
		modificar = false;
		setTipoColumnTable("col-md-12");
		resetearFitrosTabla("formTableServicio:dataTableServicio");
		newServicio = new Servicio();
		selectedServicio = new Servicio();
	}

	public void onRowSelect(SelectEvent event) {
		newServicio = new Servicio();
		newServicio = selectedServicio;
		busquedaCuenta = new PlanCuenta();
		busquedaCentroCosto = new CentroCosto();
		busquedaCuenta = selectedServicio.getCuenta()!=null?selectedServicio.getCuenta():null;
		textoAutoCompleteCuenta = selectedServicio.getCuenta()!=null?selectedServicio.getCuenta().getDescripcion():"";
		busquedaCentroCosto = selectedServicio.getCentroCosto()!=null? selectedServicio.getCentroCosto():null;
		textoAutoCompleteCentroCosto = selectedServicio.getCentroCosto()!=null?selectedServicio.getCentroCosto().getNombre():"";
		nombreEstado = newServicio.getEstado().equals("AC")?"ACTIVO":"INACTIVO";
		crear = false;
		registrar = false;
		modificar = true;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTableServicio:dataTableServicio");
	}

	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTableServicio:dataTableServicio");
	}

	// get and set
	public String getTituloPanel() {
		return tituloPanel;
	}

	public void setTituloPanel(String tituloPanel) {
		this.tituloPanel = tituloPanel;
	}

	public boolean isModificar() {
		return modificar;
	}

	public void setModificar(boolean modificar) {
		this.modificar = modificar;
	}

	public List<Servicio> getListFilterServicio() {
		return listFilterServicio;
	}

	public void setListFilterServicio(List<Servicio> listFilterServicio) {
		this.listFilterServicio = listFilterServicio;
	}

	public Servicio getSelectedServicio() {
		return selectedServicio;
	}

	public void setSelectedServicio(Servicio selectedServicio) {
		this.selectedServicio = selectedServicio;
	}

	public String getNombreEstado() {
		return nombreEstado;
	}

	public void setNombreEstado(String nombreEstado) {
		this.nombreEstado = nombreEstado;
	}

	public String[] getListEstados() {
		return listEstados;
	}

	public void setListEstados(String[] listEstados) {
		this.listEstados = listEstados;
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

	public String getTipoColumnTable() {
		return tipoColumnTable;
	}

	public void setTipoColumnTable(String tipoColumnTable) {
		this.tipoColumnTable = tipoColumnTable;
	}

	public String getTextoAutoCompleteCuenta() {
		return textoAutoCompleteCuenta;
	}

	public void setTextoAutoCompleteCuenta(String textoAutoCompleteCuenta) {
		this.textoAutoCompleteCuenta = textoAutoCompleteCuenta;
	}

	public String getTextoAutoCompleteCentroCosto() {
		return textoAutoCompleteCentroCosto;
	}

	public void setTextoAutoCompleteCentroCosto(
			String textoAutoCompleteCentroCosto) {
		this.textoAutoCompleteCentroCosto = textoAutoCompleteCentroCosto;
	}

	public List<PlanCuenta> getListCuentasAuxiliares() {
		return listCuentasAuxiliares;
	}

	public void setListCuentasAuxiliares(List<PlanCuenta> listCuentasAuxiliares) {
		this.listCuentasAuxiliares = listCuentasAuxiliares;
	}

	public List<CentroCosto> getListCentroCosto() {
		return listCentroCosto;
	}

	public void setListCentroCosto(List<CentroCosto> listCentroCosto) {
		this.listCentroCosto = listCentroCosto;
	}

	public List<PrecioServicio> getListPrecioServicio() {
		return listPrecioServicio;
	}

	public void setListPrecioServicio(List<PrecioServicio> listPrecioServicio) {
		this.listPrecioServicio = listPrecioServicio;
	}
}
