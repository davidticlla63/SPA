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

import bo.com.erp360.webapp.data.CargoRepository;
import bo.com.erp360.webapp.data.EmpleadoRepository;
import bo.com.erp360.webapp.model.Cargo;
import bo.com.erp360.webapp.model.Empleado;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.TipoServicio;
import bo.com.erp360.webapp.service.CargoRegistration;
import bo.com.erp360.webapp.service.EmpleadoRegistration;
import bo.com.erp360.webapp.util.SessionMain;


@Named(value = "empleadoController")
@ConversationScoped
public class EmpleadoController implements Serializable {



	/**
	 * 
	 */
	private static final long serialVersionUID = 3639883301090254084L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;
	
	@Inject
	private EmpleadoRepository empleadoRepository;

	@Inject
	private EmpleadoRegistration empleadoRegistration;
	
	private Logger log = Logger.getLogger(this.getClass());

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	//estados
	private boolean crear = true;
	private boolean registrar = false;
	private boolean modificar = false;
	private boolean permitirCredito = false;

	private String nombreEstado="ACTIVO";
	private String nombreUsuario; 
	private String tituloPanel = "Registrar Empleado";
	private String tipoColumnTable = "col-md-12"; //8

	@Produces
	@Named
	private Empleado newEmpleado;
	private Empleado selectedEmpleado;

	private List<Empleado> listEmpleado = new ArrayList<Empleado>();
	private List<Empleado> listFilterEmpleado = new ArrayList<Empleado>();
	private String[] listEstado = {"ACTIVO","INACTIVO"};

	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;
	
	private List<Cargo> listCargo= new ArrayList<Cargo>();
	
	private @Inject CargoRepository cargoRepository;
	
	
	private String textoAutoCompleteCargo;
	
	private @Inject CargoRegistration cargoRegistration;
	
	private Cargo cargo;

	@PostConstruct
	public void initNewEmpleado() {
		log.info(" init new initNewEmpleado");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();
		
		tituloPanel = "Centro Costo";
		newEmpleado = new Empleado();
		selectedEmpleado = new Empleado();
		listEmpleado = empleadoRepository.findAllActive(empresaLogin);
		listCargo= cargoRepository.findAllActive(empresaLogin);
		textoAutoCompleteCargo="";
	}

	
	public List<Empleado> getListEmpleado() {
		return listEmpleado;
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

	public void registrar(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			if (textoAutoCompleteCargo.trim().length() == 0) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Revisar y llenar", "Cargo!");
				facesContext.addMessage(null, m);
				return;
			}
			if (!existeCargo()) {
				cargo = new Cargo();
				cargo.setNombre(textoAutoCompleteCargo);
				cargo.setEmpresa(empresaLogin);
				cargo.setEstado("AC");
				cargo.setUsuarioRegistro(nombreUsuario);
				cargo.setFechaRegistro(new Date());
				cargo = cargoRegistration.create(cargo);
			}
			
			newEmpleado.setCargo(cargo);
			newEmpleado.setState(estado);
			newEmpleado.setFechaRegistro(new Date());
			newEmpleado.setUsuarioRegistro(nombreUsuario);
			newEmpleado.setEmpresa(empresaLogin);
			if(!newEmpleado.validate2(facesContext, empresaLogin, gestionLogin)){
				resetearFitrosTabla("formTableEmpleado:dataTableEmpleado");
				return;
			}
			empleadoRegistration.create(newEmpleado);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Empleado Registrado!", newEmpleado.getNombre());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTableEmpleado:dataTableEmpleado");
			initNewEmpleado();
		}catch(Exception e){
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void modificar(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			
			if (textoAutoCompleteCargo.trim().length() == 0) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Revisar y llenar", "Cargo!");
				facesContext.addMessage(null, m);
				return;
			}
			if (!existeCargo()) {
				cargo = new Cargo();
				cargo.setNombre(textoAutoCompleteCargo);
				cargo.setEmpresa(empresaLogin);
				cargo.setEstado("AC");
				cargo.setUsuarioRegistro(nombreUsuario);
				cargo.setFechaRegistro(new Date());
				cargo = cargoRegistration.create(cargo);
			}
			
			newEmpleado.setCargo(cargo);			
			newEmpleado.setState(estado);
			newEmpleado.setFechaRegistro(new Date());
			newEmpleado.setUsuarioRegistro(nombreUsuario);
			empleadoRegistration.update(newEmpleado);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Empleado Modificado!", newEmpleado.getNombre());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTableEmpleado:dataTableEmpleado");
			initNewEmpleado();
		}catch(Exception e){
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminar(){
		try{
			newEmpleado.setState("RM");
			empleadoRegistration.update(newEmpleado);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Empleado Eliminado!", newEmpleado.getNombre());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTableEmpleado:dataTableEmpleado");
			initNewEmpleado();
		}catch(Exception e){
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void actualizarFormReg(){
		crear = true;
		registrar = false;
		modificar = false;
		setTipoColumnTable("col-md-12");
		resetearFitrosTabla("formTableEmpleado:dataTableEmpleado");
		newEmpleado = new Empleado();	
		selectedEmpleado = new Empleado();
	}
	
	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTableEmpleado:dataTableEmpleado");
	}

	public void onRowSelect(SelectEvent event) {
		newEmpleado = new Empleado();
		newEmpleado = selectedEmpleado;
		nombreEstado = newEmpleado.getState().equals("AC")?"ACTIVO":"INACTIVO";
		crear = false;
		registrar = false;
		modificar = true;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTableEmpleado:dataTableEmpleado");
	}
	
	
	//autocomplete Cargo
	
	public List<String> completeTextCargo(String query) {
		List<String> results = new ArrayList<String>();
		log.info("size : " + listCargo.size());
		for (Cargo i : listCargo) {
			if (i.getNombre().startsWith(query.toUpperCase())) {
				results.add(i.getNombre());
			}
		}
		return results;
	}

	public void onItemSelectCargo(SelectEvent event) {
		String nits = event.getObject().toString();
		for (Cargo i : listCargo) {
			if (i.getNombre().equals(nits.toUpperCase())) {
				setTextoAutoCompleteCargo(i.getNombre());
			}
		}
	}
	

	
	private boolean existeCargo() {
		try {
			log.info("Ingreso a existeCargo");

			List<Cargo> list = cargoRepository
					.findAllActiveForNomrbre(empresaLogin,textoAutoCompleteCargo
							.toUpperCase());
			if (list.size() > 0) {
				setCargo(list.get(0));
			}
			return list.size() > 0;
		} catch (Exception e) {
			log.error("Error en existeCargo : " + e.getMessage());
		}
		return false;
	}

	// --------------   get and set  ---------------
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

	public List<Empleado> getListFilterEmpleado() {
		return listFilterEmpleado;
	}

	public void setListFilterEmpleado(List<Empleado> listFilterEmpleado) {
		this.listFilterEmpleado = listFilterEmpleado;
	}

	public Empleado getSelectedEmpleado() {
		return selectedEmpleado;
	}

	public void setSelectedEmpleado(Empleado selectedEmpleado) {
		this.selectedEmpleado = selectedEmpleado;
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

	public boolean isPermitirCredito() {
		return permitirCredito;
	}

	public void setPermitirCredito(boolean permitirCredito) {
		this.permitirCredito = permitirCredito;
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


	public List<Cargo> getListCargo() {
		return listCargo;
	}


	public void setListCargo(List<Cargo> listCargo) {
		this.listCargo = listCargo;
	}


	public String getTextoAutoCompleteCargo() {
		return textoAutoCompleteCargo;
	}


	public void setTextoAutoCompleteCargo(String textoAutoCompleteCargo) {
		this.textoAutoCompleteCargo = textoAutoCompleteCargo;
	}


	public Cargo getCargo() {
		return cargo;
	}


	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}
}
