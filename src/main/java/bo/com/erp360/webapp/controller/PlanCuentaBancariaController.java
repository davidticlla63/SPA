package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.data.PlanCuentaBancariaRepository;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.PlanCuentaBancaria;
import bo.com.erp360.webapp.service.PlanCuentaBancariaRegistration;
import bo.com.erp360.webapp.util.SessionMain;


@ManagedBean
@Named(value = "planCuentaBancariaController")
@ConversationScoped
public class PlanCuentaBancariaController implements Serializable {

	
	private static final long serialVersionUID = -8267273214927559473L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private PlanCuentaBancariaRepository planCuentaBancariaRepository;

	@Inject
	private PlanCuentaBancariaRegistration planCuentaBancariaRegistration;
	
	@Inject
	private MonedaRepository monedaRepository;

	private Logger log = Logger.getLogger(this.getClass());

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	//estados
	private boolean crear = true;
	private boolean registrar = false;
	private boolean modificar = false;
	private boolean permitirCredito = true;

	private String nombreEstado="ACTIVO";
	private String nombreUsuario; 
	private String tituloPanel = "Registrar Clientes";
	private String tipoColumnTable = "col-md-12"; //8
	private String nombreMonedaEmpresa = "";

	@Produces
	@Named
	private PlanCuentaBancaria newPlanCuentaBancaria;
	private PlanCuentaBancaria selectedPlanCuentaBancaria;
	private MonedaEmpresa monedaEmpresa;

	private List<PlanCuentaBancaria> listPlanCuentaBancaria = new ArrayList<PlanCuentaBancaria>();
	private List<PlanCuentaBancaria> listFilterPlanCuentaBancaria = new ArrayList<PlanCuentaBancaria>();
	private List<MonedaEmpresa> listMonedaEmpresa = new ArrayList<MonedaEmpresa>();
	private String[] listEstado = {"ACTIVO","INACTIVO"};

	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@PostConstruct
	public void initNewPlanCuentaBancaria() {
		log.info(" init new initNewPlanCuentaBancaria");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();
		
		listMonedaEmpresa = monedaRepository.findMonedaEmpresaAllActivasByEmpresa(empresaLogin);
		monedaEmpresa = listMonedaEmpresa.size()>0? listMonedaEmpresa.get(0): new MonedaEmpresa();
		nombreMonedaEmpresa = listMonedaEmpresa.size()>0?monedaEmpresa.getMoneda().getNombre():"";
		
		tituloPanel = "Plan de Cuenta Bancaria";
		newPlanCuentaBancaria = new PlanCuentaBancaria();
		selectedPlanCuentaBancaria = new PlanCuentaBancaria();
		listPlanCuentaBancaria = planCuentaBancariaRepository.findAllByEmpresa(empresaLogin);
	}

	@Produces
	@Named
	public List<PlanCuentaBancaria> getListPlanCuentaBancaria() {
		return listPlanCuentaBancaria;
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
			newPlanCuentaBancaria.setEstado(estado);
			newPlanCuentaBancaria.setFechaRegistro(new Date());
			newPlanCuentaBancaria.setUsuarioRegistro(nombreUsuario);
			newPlanCuentaBancaria.setEmpresa(empresaLogin);
			newPlanCuentaBancaria.setMonedaEmpresa(monedaEmpresa);
			planCuentaBancariaRegistration.create(newPlanCuentaBancaria);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"PlanCuentaBancaria Registrada!", newPlanCuentaBancaria.getCodigo());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTablePlanCuentaBancaria:dataTablePlanCuentaBancaria");
			initNewPlanCuentaBancaria();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void actualizarForm(){
		crear = true;
		registrar = false;
		modificar = false;
		tipoColumnTable = "col-md-12";
		newPlanCuentaBancaria = new PlanCuentaBancaria();
		resetearFitrosTabla("formTablePlanCuentaBancaria:dataTablePlanCuentaBancaria");
		selectedPlanCuentaBancaria = new PlanCuentaBancaria();
	}
	
	public void modificar(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newPlanCuentaBancaria.setEstado(estado);
			newPlanCuentaBancaria.setFechaRegistro(new Date());
			newPlanCuentaBancaria.setUsuarioRegistro(nombreUsuario);
			newPlanCuentaBancaria.setMonedaEmpresa(monedaEmpresa);
			planCuentaBancariaRegistration.update(newPlanCuentaBancaria);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cliente Modificado!", newPlanCuentaBancaria.getCodigo());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTablePlanCuentaBancaria:dataTablePlanCuentaBancaria");
			initNewPlanCuentaBancaria();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminar(){
		try{
			newPlanCuentaBancaria.setEstado("RM");
			planCuentaBancariaRegistration.update(newPlanCuentaBancaria);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cliente Eliminado!", newPlanCuentaBancaria.getCodigo());
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			setTipoColumnTable("col-md-8");
			resetearFitrosTabla("formTablePlanCuentaBancaria:dataTablePlanCuentaBancaria");
			initNewPlanCuentaBancaria();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	private String getRootErrorMessage(Exception e) {
		String errorMessage = "Registration failed. See server log for more information";
		if (e == null) {
			return errorMessage;
		}
		Throwable t = e;
		while (t != null) {
			errorMessage = t.getLocalizedMessage();
			t = t.getCause();
		}
		return errorMessage;
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
		resetearFitrosTabla("formTablePlanCuentaBancaria:dataTablePlanCuentaBancaria");
		newPlanCuentaBancaria = new PlanCuentaBancaria();	
		selectedPlanCuentaBancaria = new PlanCuentaBancaria();
	}
	
	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTablePlanCuentaBancaria:dataTablePlanCuentaBancaria");
	}

	public void onRowSelect(SelectEvent event) {
		newPlanCuentaBancaria = new PlanCuentaBancaria();
		newPlanCuentaBancaria = selectedPlanCuentaBancaria;
		nombreEstado = newPlanCuentaBancaria.getEstado().equals("AC")?"ACTIVO":"INACTIVO";
		crear = false;
		registrar = false;
		modificar = true;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTablePlanCuentaBancaria:dataTablePlanCuentaBancaria");
	}

	private MonedaEmpresa buscarMonedaEmpresaByLocal(String nombreMonedaEmpresa){
		for(MonedaEmpresa me: listMonedaEmpresa){
			if(nombreMonedaEmpresa.equals(me.getMoneda().getNombre())){
				return me;
			}
		}
		return null;
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

	public PlanCuentaBancaria getSelectedPlanCuentaBancaria() {
		return selectedPlanCuentaBancaria;
	}

	public void setSelectedPlanCuentaBancaria(PlanCuentaBancaria selectedPlanCuentaBancaria) {
		this.selectedPlanCuentaBancaria = selectedPlanCuentaBancaria;
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

	public List<PlanCuentaBancaria> getListFilterPlanCuentaBancaria() {
		return listFilterPlanCuentaBancaria;
	}

	public void setListFilterPlanCuentaBancaria(
			List<PlanCuentaBancaria> listFilterPlanCuentaBancaria) {
		this.listFilterPlanCuentaBancaria = listFilterPlanCuentaBancaria;
	}
	
	public PlanCuentaBancaria getNewPlanCuentaBancaria(){
		return newPlanCuentaBancaria;
	}
	
	public void setNewPlanCuentaBancaria(PlanCuentaBancaria newPlanCuentaBancaria){
		this.newPlanCuentaBancaria = newPlanCuentaBancaria;
	}

	public List<MonedaEmpresa> getListMonedaEmpresa() {
		return listMonedaEmpresa;
	}

	public void setListMonedaEmpresa(List<MonedaEmpresa> listMonedaEmpresa) {
		this.listMonedaEmpresa = listMonedaEmpresa;
	}

	public String getNombreMonedaEmpresa() {
		return nombreMonedaEmpresa;
	}

	public void setNombreMonedaEmpresa(String nombreMonedaEmpresa) {
		this.nombreMonedaEmpresa = nombreMonedaEmpresa;
		monedaEmpresa = buscarMonedaEmpresaByLocal(this.nombreMonedaEmpresa);
	}
}
