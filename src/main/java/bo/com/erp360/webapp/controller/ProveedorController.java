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
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.BienServicioRepository;
import bo.com.erp360.webapp.data.CiudadRepository;
import bo.com.erp360.webapp.data.ClienteProveedorRepository;
import bo.com.erp360.webapp.data.ContactoProveedorRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.PaisRepository;
import bo.com.erp360.webapp.data.PlanCuentaRepository;
import bo.com.erp360.webapp.data.ProveedorRepository;
import bo.com.erp360.webapp.model.BienServicio;
import bo.com.erp360.webapp.model.Ciudad;
import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.ClienteProveedor;
import bo.com.erp360.webapp.model.ContactoProveedor;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Pais;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.Proveedor;
import bo.com.erp360.webapp.service.BienServicioRegistration;
import bo.com.erp360.webapp.service.ClienteProveedorRegistration;
import bo.com.erp360.webapp.service.ClienteRegistration;
import bo.com.erp360.webapp.service.ContactoProveedorRegistration;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.ProveedorRegistration;

@Named(value = "proveedorController")
@SuppressWarnings("serial")
@ConversationScoped
public class ProveedorController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private EmpresaRepository empresaRepository;
	
	@Inject
	private GestionRepository gestionRepository;

	@Inject
	private BienServicioRepository bienServicioRepository;

	@Inject
	private BienServicioRegistration bienServicioRegistration;

	@Inject
	private ContactoProveedorRegistration contactoProveedorRegistration;

	@Inject
	private PlanCuentaRepository planCuentaRepository;

	@Inject
	private ContactoProveedorRepository contactoProveedorRepository;

	@Inject
	private ProveedorRegistration proveedorRegistration;

	@Inject
	private ProveedorRepository proveedorRepository;

	@Inject
	private ClienteProveedorRepository clienteProveedorRepository;

	@Inject
	private ClienteProveedorRegistration clienteProveedorRegistration;

	@Inject
	private ClienteRegistration clienteRegistration;

	Logger log = Logger.getLogger(ProveedorController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	//estados
	private boolean crear = true;
	private boolean registrar = false;
	private boolean modificar = false;
	private boolean cliente  =false;
	private boolean permitirCreditoCliente = false;

	private String tituloPanel = "Registrar Proveedor";
	private String nombreEstado="ACTIVO";
	private String textoAutoCompleteCuenta;
	private String textoAutoCompleteCuentaServicio;
	private String textoAutoCompleteCuentaAnticipo;
	private String tipoColumnTable = "col-md-12"; //8

	private List<Proveedor> listProveedor  = new ArrayList<Proveedor>();
	private List<BienServicio> listBienServicio  = new ArrayList<BienServicio>();
	private List<BienServicio> listFilterBienServicio  = new ArrayList<BienServicio>();
	private List<Proveedor> listFilterProveedor  = new ArrayList<Proveedor>();
	private List<PlanCuenta> listCuentasAuxiliares = new ArrayList<PlanCuenta>();
	private String[] listEstado = {"ACTIVO","INACTIVO"};
	private String[] listTipoProveedor = {"NACIONAL","EXTRANJERA"};

	@Produces
	@Named
	private Proveedor newProveedor;
	private Proveedor selectedProveedor;
	private PlanCuenta busquedaCuenta;
	private PlanCuenta busquedaCuentaAnticipo;
	private PlanCuenta busquedaCuentaServicio;
	private BienServicio selectedBienServicio;
	private BienServicio newBienServicio;
	private ContactoProveedor newContactoProveedor;
	private Cliente newCliente;
	private ClienteProveedor newClienteProveedor;
	
	

	private @Inject PaisRepository paisRepository;
	private List<Pais> listPais= new ArrayList<Pais>();
	private Pais pais;

	private @Inject CiudadRepository ciudadRepository;
	private List<Ciudad> listCiudad= new ArrayList<Ciudad>();

	//login
	private String nombreUsuario;	
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Produces
	@Named
	public List<Proveedor> getListProveedor() {
		return listProveedor;
	}

	@PostConstruct
	public void initNewProveedor() {
		log.info(" init new initNewSucursal");
		beginConversation();		
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		nombreUsuario =  estadoUsuarioLogin.getNombreUsuarioSession();
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);
		gestionLogin = estadoUsuarioLogin.getGestionSession(empresaRepository, gestionRepository);
		cargarListCuentasAuxiliares();
		loadValuesDefaul();
	}

	private void loadValuesDefaul(){
		newProveedor = new Proveedor();
		busquedaCuenta = new PlanCuenta();
		newBienServicio = new BienServicio();
		selectedBienServicio = new BienServicio();
		newContactoProveedor = new ContactoProveedor(); 
		selectedProveedor = new Proveedor();
		newCliente = new Cliente();
		textoAutoCompleteCuenta = "";
		textoAutoCompleteCuentaAnticipo = "";
		cliente = false;
		pais = new Pais();
		setPermitirCreditoCliente(false);
		// tituloPanel
		tituloPanel = "Registrar Sucursal";
		// traer todos por Empresa ordenados por ID Desc
		listProveedor = proveedorRepository.findAllByEmpresa(empresaLogin);
		listPais= paisRepository.findAllActivas();
	}
	
	
	public void obtenerCiudadPorPais(){
		log.info("Ingreso a ObtenerCiudadPorPais "+pais.getId());
	
		listCiudad=ciudadRepository.findAllActivasByPais(pais);
	}

	private void cargarListCuentasAuxiliares(){
		try{
			listCuentasAuxiliares = planCuentaRepository.findAllAuxiliarByEmpresa(empresaLogin,gestionLogin);
		}catch(Exception e){
			log.info("Error en cargarListCuentasAuxiliares : "+e.getMessage());
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

	public void registrar() {
		try {
			//proveedor
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newProveedor.setEstado(estado);
			newProveedor.setPlanCuenta(busquedaCuenta);
			newProveedor.setPlanCuentaAnticipo(busquedaCuentaAnticipo);
			newProveedor.setEmpresa(empresaLogin);
			newProveedor.setUsuarioRegistro(nombreUsuario);
			newProveedor.setFechaRegistro(new Date());
			newProveedor = proveedorRegistration.create(newProveedor);
			//Bien-Servicio
			for(BienServicio bs : listBienServicio){
				bs.setProveedor(newProveedor);
				bs.setCuenta(busquedaCuentaServicio);
				bienServicioRegistration.create(bs);
			}
			//Contacto
			if( ! newContactoProveedor.getNombre().isEmpty()){
				newContactoProveedor.setFechaRegistro(new Date());
				newContactoProveedor.setEstado("AC");
				newContactoProveedor.setUsuarioRegistro(nombreUsuario);
				newContactoProveedor.setProveedor(newProveedor);
				contactoProveedorRegistration.create(newContactoProveedor);
			}
			//Cliente
			if(cliente){
				newCliente.setEstado("AC");
				newCliente.setFechaRegistro(new Date());
				newCliente.setUsuarioRegistro(nombreUsuario);
				String credito = permitirCreditoCliente?"SI":"NO";
				newCliente.setPermitirCredito(credito);
				newCliente.setEmpresa(empresaLogin);
				newCliente = clienteRegistration.create(newCliente);
				//ClienteProveedor
				newClienteProveedor.setEstado("AC");
				newClienteProveedor.setUsuarioRegistro(nombreUsuario);
				newClienteProveedor.setFechaRegistro(new Date());
				newClienteProveedor.setProveedor(newProveedor);
				newClienteProveedor.setCliente(newCliente);
			}

			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Proveedor Registrado!", newProveedor.getNombre()+"!");
			facesContext.addMessage(null, m);

			FacesMessage m2 = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Servicios Registrados!", listBienServicio.size()+" items");
			facesContext.addMessage(null, m2);

			loadValuesDefaul();
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
			log.info("registrar() ERROR: "+errorMessage); 
		}
	}

	public void modificar() {
		try {
			//proveedor
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newProveedor.setEstado(estado);
			newProveedor.setPlanCuenta(busquedaCuenta!=null?busquedaCuenta:null);
			newProveedor.setPlanCuentaAnticipo(busquedaCuentaAnticipo!=null?busquedaCuentaAnticipo:null);
			proveedorRegistration.update(newProveedor);
			newProveedor.setUsuarioRegistro(nombreUsuario);
			newProveedor.setFechaRegistro(new Date());
			//contacto
			if( ! newContactoProveedor.getNombre().isEmpty()){
				if(newContactoProveedor.getId()==0){
					newContactoProveedor.setFechaRegistro(new Date());
					newContactoProveedor.setProveedor(newProveedor);
					newContactoProveedor.setEstado("AC");
					newContactoProveedor.setUsuarioRegistro(nombreUsuario);
					contactoProveedorRegistration.create(newContactoProveedor);
				}else{
					newContactoProveedor.setFechaModificacion(new Date());
					newContactoProveedor.setEstado("AC");
					newContactoProveedor.setUsuarioRegistro(nombreUsuario);
					contactoProveedorRegistration.update(newContactoProveedor);
				}

			}
			//cliente Proveedor
			if(cliente){
				if(newCliente.getId() == 0){
					String credito = permitirCreditoCliente?"SI":"NO";
					newCliente.setPermitirCredito(credito);
					newCliente.setEmpresa(empresaLogin);
					newCliente.setFechaRegistro(new Date());
					newCliente.setUsuarioRegistro(nombreUsuario);
					newCliente.setEstado("AC");
					newCliente = clienteRegistration.create(newCliente);
					newClienteProveedor.setEstado("AC");
					newClienteProveedor.setFechaRegistro(new Date());
					newClienteProveedor.setUsuarioRegistro(nombreUsuario);
					newClienteProveedor.setProveedor(newProveedor);
					newClienteProveedor.setCliente(newCliente);
					clienteProveedorRegistration.create(newClienteProveedor);
				}else{
					String credito = permitirCreditoCliente?"SI":"NO";
					newCliente.setPermitirCredito(credito);
					newCliente.setFechaModificacion(new Date());
					clienteRegistration.update(newCliente);
				}
			}
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Proveedor Modificado!", newProveedor.getNombre()+"!");
			facesContext.addMessage(null, m);
			crear = true;
			registrar = false;
			modificar = false;
			tipoColumnTable = "col-md-8";
			resetearFitrosTabla("formTableProveedor:dataTableProveedor");
			loadValuesDefaul();
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Modificado Incorrecto.");
			facesContext.addMessage(null, m);
			log.info("modificar() ERROR: "+errorMessage); 
		}
	}

	public void eliminar() {
		try {
			//proveedor
			newProveedor.setEstado("RM");
			proveedorRegistration.update(newProveedor);
			//Contacto
			if(! newContactoProveedor.getNombre().isEmpty()){
				newContactoProveedor.setEstado("RM");
				newContactoProveedor.setFechaModificacion(new Date());
				contactoProveedorRegistration.update(newContactoProveedor);
			}
			//limpiar lista bien-servicio
			listBienServicio = new ArrayList<BienServicio>();

			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Proveedor Eliminado!", newProveedor.getNombre()+"!");
			facesContext.addMessage(null, m);
			crear = false;
			registrar = true;
			modificar = false;
			tipoColumnTable = "col-md-8";
			resetearFitrosTabla("formTableProveedor:dataTableProveedor");
			resetearFitrosTabla("formTableBienServicio:dataTableBienServicio");
			loadValuesDefaul();
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Borrado Incorrecto.");
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

	public void actualizarForm(){
		crear = true;
		registrar = false;
		modificar = false;
		tipoColumnTable = "col-md-12";
		newProveedor = new Proveedor();
		resetearFitrosTabla("formTableProveedor:dataTableProveedor");
		selectedProveedor = new Proveedor();
	}

	public void onRowSelect(SelectEvent event) {
		newProveedor = new Proveedor();
		newProveedor = selectedProveedor;
		nombreEstado = newProveedor.getEstado().equals("AC")?"ACTIVO":"INACTIVO";
		
		busquedaCuenta = selectedProveedor.getPlanCuenta()!=null?selectedProveedor.getPlanCuenta():null;
		textoAutoCompleteCuenta = selectedProveedor.getPlanCuenta()!=null?selectedProveedor.getPlanCuenta().getDescripcion():"";
		
		busquedaCuentaAnticipo = selectedProveedor.getPlanCuentaAnticipo()!=null?selectedProveedor.getPlanCuentaAnticipo():null;
		textoAutoCompleteCuentaAnticipo = selectedProveedor.getPlanCuentaAnticipo()!=null?selectedProveedor.getPlanCuentaAnticipo().getDescripcion():"";		
		
		newContactoProveedor = contactoProveedorRepository.findByProveedor(selectedProveedor);
		newClienteProveedor = clienteProveedorRepository.findByProveedor(selectedProveedor);
		pais= newProveedor.getCiudad().getPais();
		obtenerCiudadPorPais();
		if(newClienteProveedor.getId()!=0){
			newCliente = newClienteProveedor.getCliente();
			cliente = true;
			permitirCreditoCliente = newCliente.getPermitirCredito().equals("SI")?true:false;
		}
		cargarListBienSevicio(selectedProveedor);
		crear = false;
		registrar = false;
		modificar = true;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTableProveedor:dataTableProveedor");
		resetearFitrosTabla("formTableBienServicio:dataTableBienServicio");
	}

	public void onRowSelectBienServicio(SelectEvent event) {

		resetearFitrosTabla("formTableBienServicio:dataTableBienServicio");
	}

	private void cargarListBienSevicio(Proveedor proveedor){
		listBienServicio = bienServicioRepository.findAllByProveedor(proveedor);
	}

	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTableProveedor:dataTableProveedor");
		resetearFitrosTabla("formTableBienServicio:dataTableBienServicio");
	}

	public void actualizarFormReg(){
		crear = true;
		registrar = false;
		modificar = false;
		setTipoColumnTable("col-md-12");
		resetearFitrosTabla("formTableProveedor:dataTableProveedor");
		newProveedor = new Proveedor();
		busquedaCuenta = new PlanCuenta();
		busquedaCuentaServicio  = new PlanCuenta();
		textoAutoCompleteCuenta = "";
		textoAutoCompleteCuentaServicio = "";
		textoAutoCompleteCuentaAnticipo = "";
		cliente = false;
		selectedProveedor = new Proveedor();
		listBienServicio = new ArrayList<BienServicio>();
		newContactoProveedor = new ContactoProveedor();
		newCliente = new Cliente();
	}

	public void onItemSelectCuenta(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(PlanCuenta s : listCuentasAuxiliares){
			if(s.getDescripcion().equals(nombre)){
				setBusquedaCuenta(s);
			}
		}
	}

	public void onItemSelectCuentaAnticipo(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(PlanCuenta s : listCuentasAuxiliares){
			if(s.getDescripcion().equals(nombre)){
				setBusquedaCuentaAnticipo(s);
			}
		}
	}
	
	public void onItemSelectCuentaServicio(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(PlanCuenta s : listCuentasAuxiliares){
			if(s.getDescripcion().equals(nombre)){
				setBusquedaCuentaServicio(s);
			}
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

	/**
	 * guarda servicio en la base de datos y en la lista si el parametro registar es TRUE
	 * cuando se selecciono un proveedor de la lista de Proveedor, caso contrario
	 * solo lo almacena el servicio en una lista
	 */
	public void agregarBienServicio(boolean registrar){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			newBienServicio.setEstado(estado);
			newBienServicio.setFechaRegistro(new Date());
			newBienServicio.setUsuarioRegistro(nombreUsuario);
			if(registrar){
				newBienServicio.setProveedor(selectedProveedor);
				newBienServicio.setCuenta(busquedaCuentaServicio);
				newBienServicio = bienServicioRegistration.create(newBienServicio);
			}
			listBienServicio.add(newBienServicio);
			newBienServicio = new BienServicio();
			resetearFitrosTabla("formTableBienServicio:dataTableBienServicio");
			RequestContext context = RequestContext.getCurrentInstance();
			context.update("formTableBienServicio");
			context.update("formDlgBienServicio");
			FacesMessage m2 = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Bien/Servicio Registrado!", newBienServicio.getNombre());
			facesContext.addMessage(null, m2);
		}catch(Exception e){
			log.info("agregarBienServicio() ERROR "+e.getMessage());
		}
	}

	public void agregarCliente(){
		try{
			cliente = true ;

		}catch(Exception e){
			log.info("agregarCliente() ERROR "+e.getMessage());
		}
	}

	//  ---- get and set -----
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

	public Proveedor getSelectedProveedor() {
		return selectedProveedor;
	}

	public void setSelectedProveedor(Proveedor selectedProveedor) {
		this.selectedProveedor = selectedProveedor;
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

	public List<Proveedor> getListFilterProveedor() {
		return listFilterProveedor;
	}

	public void setListFilterProveedor(List<Proveedor> listFilterProveedor) {
		this.listFilterProveedor = listFilterProveedor;
	}

	public String getTextoAutoCompleteCuenta() {
		return textoAutoCompleteCuenta;
	}

	public void setTextoAutoCompleteCuenta(String textoAutoCompleteCuenta) {
		this.textoAutoCompleteCuenta = textoAutoCompleteCuenta;
	}

	public List<PlanCuenta> getListCuentasAuxiliares() {
		return listCuentasAuxiliares;
	}

	public void setListCuentasAuxiliares(List<PlanCuenta> listCuentasAuxiliares) {
		this.listCuentasAuxiliares = listCuentasAuxiliares;
	}

	public PlanCuenta getBusquedaCuenta() {
		return busquedaCuenta;
	}

	public void setBusquedaCuenta(PlanCuenta busquedaCuenta) {
		this.busquedaCuenta = busquedaCuenta;
	}

	public List<BienServicio> getListBienServicio() {
		return listBienServicio;
	}

	public void setListBienServicio(List<BienServicio> listBienServicio) {
		this.listBienServicio = listBienServicio;
	}

	public BienServicio getSelectedBienServicio() {
		return selectedBienServicio;
	}

	public void setSelectedBienServicio(BienServicio selectedBienServicio) {
		this.selectedBienServicio = selectedBienServicio;
	}

	public List<BienServicio> getListFilterBienServicio() {
		return listFilterBienServicio;
	}

	public void setListFilterBienServicio(List<BienServicio> listFilterBienServicio) {
		this.listFilterBienServicio = listFilterBienServicio;
	}

	public BienServicio getNewBienServicio() {
		return newBienServicio;
	}

	public void setNewBienServicio(BienServicio newBienServicio) {
		this.newBienServicio = newBienServicio;
	}

	public String[] getListTipoProveedor() {
		return listTipoProveedor;
	}

	public void setListTipoProveedor(String[] listTipoProveedor) {
		this.listTipoProveedor = listTipoProveedor;
	}

	public ContactoProveedor getNewContactoProveedor() {
		return newContactoProveedor;
	}

	public void setNewContactoProveedor(ContactoProveedor newContactoProveedor) {
		this.newContactoProveedor = newContactoProveedor;
	}

	public boolean isCliente() {
		return cliente;
	}

	public void setCliente(boolean cliente) {
		this.cliente = cliente;
	}

	public Cliente getNewCliente() {
		return newCliente;
	}

	public void setNewCliente(Cliente newCliente) {
		this.newCliente = newCliente;
	}

	public ClienteProveedor getNewClienteProveedor() {
		return newClienteProveedor;
	}

	public void setNewClienteProveedor(ClienteProveedor newClienteProveedor) {
		this.newClienteProveedor = newClienteProveedor;
	}

	public boolean isPermitirCreditoCliente() {
		return permitirCreditoCliente;
	}

	public void setPermitirCreditoCliente(boolean permitirCreditoCliente) {
		this.permitirCreditoCliente = permitirCreditoCliente;
	}

	public String getTextoAutoCompleteCuentaAnticipo() {
		return textoAutoCompleteCuentaAnticipo;
	}

	public void setTextoAutoCompleteCuentaAnticipo(
			String textoAutoCompleteCuentaAnticipo) {
		this.textoAutoCompleteCuentaAnticipo = textoAutoCompleteCuentaAnticipo;
	}

	public PlanCuenta getBusquedaCuentaAnticipo() {
		return busquedaCuentaAnticipo;
	}

	public void setBusquedaCuentaAnticipo(PlanCuenta busquedaCuentaAnticipo) {
		this.busquedaCuentaAnticipo = busquedaCuentaAnticipo;
	}

	public String getTextoAutoCompleteCuentaServicio() {
		return textoAutoCompleteCuentaServicio;
	}

	public void setTextoAutoCompleteCuentaServicio(
			String textoAutoCompleteCuentaServicio) {
		this.textoAutoCompleteCuentaServicio = textoAutoCompleteCuentaServicio;
	}

	public PlanCuenta getBusquedaCuentaServicio() {
		return busquedaCuentaServicio;
	}

	public void setBusquedaCuentaServicio(PlanCuenta busquedaCuentaServicio) {
		this.busquedaCuentaServicio = busquedaCuentaServicio;
	}

	public List<Pais> getListPais() {
		return listPais;
	}

	public void setListPais(List<Pais> listPais) {
		this.listPais = listPais;
	}

	public List<Ciudad> getListCiudad() {
		return listCiudad;
	}

	public void setListCiudad(List<Ciudad> listCiudad) {
		this.listCiudad = listCiudad;
	}

	public Pais getPais() {
		return pais;
	}

	public void setPais(Pais idPais) {
		this.pais = idPais;
	}

}
