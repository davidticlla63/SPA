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

import bo.com.erp360.webapp.data.ClienteRepository;
import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.NitCliente;
import bo.com.erp360.webapp.service.ClienteRegistration;
import bo.com.erp360.webapp.service.NitClienteRegistration;
import bo.com.erp360.webapp.util.SessionMain;


@Named(value = "clientesController")
@ConversationScoped
public class ClienteController implements Serializable {

	private static final long serialVersionUID = -7148739425514986109L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;
	
	@Inject
	private ClienteRepository clientesRepository;

	@Inject
	private ClienteRegistration clientesRegistration;
	
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
	private String tituloPanel = "Registrar Clientes";
	private String tipoColumnTable = "col-md-12"; //8

	@Produces
	@Named
	private Cliente newClientes;
	private Cliente selectedClientes;

	private List<Cliente> listClientes = new ArrayList<Cliente>();
	private List<Cliente> listFilterClientes = new ArrayList<Cliente>();
	private String[] listEstado = {"ACTIVO","INACTIVO"};

	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@PostConstruct
	public void initNewClientes() {
		log.info(" init new initNewCliente");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();
		
		tituloPanel = "Centro Costo";
		newClientes = new Cliente();
		selectedClientes = new Cliente();
		listClientes = clientesRepository.findByEmpresa(empresaLogin);
	}

	@Produces
	@Named
	public List<Cliente> getListClientes() {
		return listClientes;
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

	private @Inject NitClienteRegistration nitClienteRegistration;
	public void registrar(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			String credito = permitirCredito?"SI":"NO";
			newClientes.setPermitirCredito(credito);
			newClientes.setEstado(estado);
			newClientes.setFechaRegistro(new Date());
			newClientes.setUsuarioRegistro(nombreUsuario);
			newClientes.setEmpresa(empresaLogin);
			if(!newClientes.validateSpaDate(facesContext, empresaLogin, gestionLogin)){
				resetearFitrosTabla("formTableClientes:dataTableCliente");
				return;
			}
			newClientes=clientesRegistration.create(newClientes);
			if (newClientes.getCi().trim().length()>0) {
				NitCliente nitCliente= new NitCliente();
				nitCliente.setCliente(newClientes);
				nitCliente.setNit(newClientes.getCi());
				nitCliente.setUsuarioRegistro(nombreUsuario);
				nitClienteRegistration.create(nitCliente);
				
			}
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cliente Registrado!", newClientes.getNombre());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTableClientes:dataTableCliente");
			initNewClientes();
		}catch(Exception e){
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void modificar(){
		try{
			String credito = permitirCredito?"SI":"NO";
			newClientes.setPermitirCredito(credito);
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newClientes.setEstado(estado);
			newClientes.setFechaRegistro(new Date());
			newClientes.setUsuarioRegistro(nombreUsuario);
			newClientes=clientesRegistration.update(newClientes);
			
		
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cliente Modificado!", newClientes.getNombre());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTableClientes:dataTableCliente");
			initNewClientes();
		}catch(Exception e){
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					e.getMessage(), "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminar(){
		try{
			newClientes.setEstado("RM");
			clientesRegistration.update(newClientes);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cliente Eliminado!", newClientes.getNombre());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTableClientes:dataTableCliente");
			initNewClientes();
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
		resetearFitrosTabla("formTableClientes:dataTableCliente");
		newClientes = new Cliente();	
		selectedClientes = new Cliente();
	}
	
	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTableClientes:dataTableCliente");
	}

	public void onRowSelect(SelectEvent event) {
		newClientes = new Cliente();
		newClientes = selectedClientes;
		nombreEstado = newClientes.getEstado().equals("AC")?"ACTIVO":"INACTIVO";
		permitirCredito = newClientes.getPermitirCredito().equals("SI")?true:false;
		crear = false;
		registrar = false;
		modificar = true;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTableClientes:dataTableCliente");
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

	public List<Cliente> getListFilterClientes() {
		return listFilterClientes;
	}

	public void setListFilterClientes(List<Cliente> listFilterClientes) {
		this.listFilterClientes = listFilterClientes;
	}

	public Cliente getSelectedClientes() {
		return selectedClientes;
	}

	public void setSelectedClientes(Cliente selectedClientes) {
		this.selectedClientes = selectedClientes;
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
}
