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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.primefaces.component.api.UIData;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.CentroCostoRepository;
import bo.com.erp360.webapp.data.ClienteRepository;
import bo.com.erp360.webapp.data.CotizacionRepository;
import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.data.PlanCuentaRepository;
import bo.com.erp360.webapp.data.ServicioRepository;
import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.Cotizacion;
import bo.com.erp360.webapp.model.CotizacionServicio;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.Servicio;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.ClienteRegistration;
import bo.com.erp360.webapp.service.CotizacionRegistration;
import bo.com.erp360.webapp.service.CotizacionServicioRegistration;
import bo.com.erp360.webapp.service.ServicioRegistration;
import bo.com.erp360.webapp.util.EDCotizacion;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "cotizacionController")
@ConversationScoped
public class CotizacionController implements Serializable {
	
	private static final long serialVersionUID = 4806310095164780692L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private CotizacionRepository cotizacionRepository;

	@Inject
	private CotizacionRegistration cotizacionRegistration;

	@Inject
	private PlanCuentaRepository planCuentaRepository;

	@Inject
	private CentroCostoRepository centroCostoRepository;

	@Inject
	private CotizacionServicioRegistration cotizacionServicioRegistration;

	@Inject
	private ServicioRepository servicioRepository;

	@Inject
	private ClienteRepository clientesRepository;

	@Inject
	private ClienteRegistration clienteRegistration;

	@Inject
	private ServicioRegistration servicioRegistration;
	
	@Inject
	private MonedaRepository monedaRepository;
	
	private Logger log = Logger.getLogger(this.getClass());

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	@Produces
	@Named
	private Cotizacion newCotizacion;
	private EDCotizacion selectedEDCotizacion;
	private Servicio selectedServicio;
	private Servicio newServicio;
	private Cliente busquedaCliente;
	private PlanCuenta busquedaCuenta;
	private CentroCosto busquedaCentroCosto;
	private Cliente newClientes;
	private Servicio busquedaServicio;
	private MonedaEmpresa monedaEmpresa;

	private List<EDCotizacion> listEDCotizacion = new ArrayList<>();
	private List<Usuario> listUsuario = new ArrayList<Usuario>();
	private List<Servicio> selectedListServicio  = new ArrayList<Servicio>();
	private List<Cliente> listCliente  = new ArrayList<Cliente>();
	private List<Cotizacion> listCotizacion  = new ArrayList<Cotizacion>();
	private List<Servicio> listServicio = new ArrayList<Servicio>();
	private List<MonedaEmpresa> listMonedaEmpresa;
	private List<CentroCosto> listCentroCosto = new ArrayList<CentroCosto>();
	private List<PlanCuenta> listCuentasAuxiliares = new ArrayList<PlanCuenta>();
	private List<EDCotizacion> listTestEDCotizacion = new ArrayList<EDCotizacion>();

	private String tituloPanel = "Registrar Cotizacion";
	private String nombreCliente;
	private String textoAutoCompleteCliente;
	private String textoAutoCompleteCuenta;
	private String textoAutoCompleteCentroCosto;
	private String nombreEstado="ACTIVO";
	private int selectedIdEDCotizacion;
	private Integer numeroCotizacion;
	private String nombreMonedaEmpresa;

	//login
	private @Inject SessionMain sessionMain; //variable del login
	private String nombreUsuario;
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	//estados
	private boolean modificar = false;
	private boolean selectedImporte;
	private boolean selectedCantidad;
	private boolean selectedPrecioUnitario;
	private boolean selectedPorcentaje1;
	private boolean agregarServicio = false;
	private boolean permitirCredito = true;
	private boolean error = false;

	//autoComplete
	private String texto;
	private Integer cantidad = 1;
	private double totalImportePorServicio;
	private double descuento;
	private double totalImporte;
	
	private UIData usersDataTable;

	@PostConstruct
	public void initNewCotizacion() {

		log.info(" init new initNewCotizacion");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();
		
		listMonedaEmpresa = monedaRepository.findMonedaEmpresaAllByEmpresa(empresaLogin);
		monedaEmpresa = listMonedaEmpresa.get(0);
		nombreMonedaEmpresa = monedaEmpresa.getMoneda().getNombre();
		
		loadValuesDefaul();
	}

	private void loadValuesDefaul(){
		newClientes = new Cliente();
		busquedaServicio = new Servicio();
		busquedaCuenta = new PlanCuenta();
		busquedaCentroCosto = new CentroCosto();
		newServicio = new Servicio();
		listEDCotizacion = new ArrayList<>();
		newCotizacion = new Cotizacion();
		newCotizacion.setObservacion("");
		newCotizacion.setFechaVencimiento(new Date());
		
		listCotizacion = cotizacionRepository.findAllByEmpresaGestion(empresaLogin,gestionLogin);
		
		numeroCotizacion = cotizacionRepository.findNumeroCorrelativo(empresaLogin, gestionLogin);
		totalImporte = 0;
		
		textoAutoCompleteCliente = "";
		textoAutoCompleteCuenta = "";
		textoAutoCompleteCentroCosto = "";

		listCliente = clientesRepository.findActivosByEmpresa(empresaLogin);
		nombreCliente = listCliente.size()>0?listCliente.get(0).getNombre():"";
		listServicio = servicioRepository.findAllActivosByEmpresa(empresaLogin);
		
		// tituloPanel
		tituloPanel = "Comprobante";
		modificar = false;
		cagarTestCotizacion();
	}
	
	public List<Servicio> completeText(String query) {
		String upperQuery = query.toUpperCase();
		List<Servicio> results = new ArrayList<Servicio>();
		for(Servicio i : listServicio) {
			if((i.getNombre().toUpperCase().startsWith(upperQuery)) && !(i.getEstado().equals("RM"))){
				results.add(i);
			}
		}         
		return results;
	}

	public void onItemSelect(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(Servicio s : listServicio){
			if(s.getNombre().equals(nombre)){
				busquedaServicio = s;
				totalImportePorServicio = s.getPrecioReferencial();
			}
		}
	}

	public void modificarDetalleServicio(){
		totalImportePorServicio = ((busquedaServicio.getPrecioReferencial() - (busquedaServicio.getPrecioReferencial()*descuento)/100) * cantidad);	
	}	

	public int getGestionSession(){
		try{
			HttpSession request1 = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			return Integer.parseInt(request1.getAttribute("gestion").toString());
		}catch(Exception e){
			log.info("getEmpresaSession() -> error : "+e.getMessage());
			return 0;
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

	public void registrarEImprimir(){
		registrarCotizacion();
		if(! error){
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgCotizacionVistaPrevia').show();");
		}
	}

	public void registrarCotizacion() {
		try {
			error = false;
			if(listEDCotizacion.size()==0){
				error = true;
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Cotización Sin Servicios!", "");
				facesContext.addMessage(null, m);
				return ;
			}
			//	//id codigo  fecha fechaVencimiento total observacion usuarioRegistro cliente
			Cliente cliente = clientesRepository.findByNombre(nombreCliente);
			Date d = new Date();
			newCotizacion.setFecha(d);
			newCotizacion.setEstado("AC");
			newCotizacion.setMonedaEmpresa(monedaEmpresa);
			newCotizacion.setCliente(cliente);
			newCotizacion.setEmpresa(empresaLogin);
			newCotizacion.setGestion(gestionLogin);
			newCotizacion.setNumero(numeroCotizacion);//+d.getDay()+d.getMonth()+d.getYear()+d.getSeconds()+d.getHours());
			newCotizacion.setTotal(totalImporte);
			newCotizacion.setUsuarioRegistro(nombreUsuario);
			newCotizacion.setFechaRegistro(d);
			newCotizacion = cotizacionRegistration.create(newCotizacion);

			for(EDCotizacion s : listEDCotizacion){
				CotizacionServicio newCotizacionServicio = new CotizacionServicio();
				newCotizacionServicio.setCotizacion(newCotizacion);
				// id codigo nombre precio estado
				Servicio servicio = new Servicio();
				servicio.setId(s.getId());
				servicio.setNombre(s.getDescripcion());
				servicio.setPrecioReferencial(s.getPrecioUnitario());
				servicio.setEstado(s.getEstado());
				newCotizacionServicio.setServicio(servicio);
				newCotizacionServicio.setCantidad(s.getCantidad());
				newCotizacionServicio.setDescuento(s.getDescuento());
				newCotizacionServicio.setSubTotal(s.getImporte());
				cotizacionServicioRegistration.create(newCotizacionServicio);
			}
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cotizacion Guardada!", listEDCotizacion.size() +" servicios");
			facesContext.addMessage(null, m);
			loadValuesDefaul();

		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Guardado Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void cagarTestCotizacion(){
		EDCotizacion edc1 = new EDCotizacion(1,"1","descrpcion",1,10,10,"AC",0);
		listTestEDCotizacion.add(edc1);
		EDCotizacion edc2 = new EDCotizacion(2,"2","descrpcion",1,10,10,"AC",0);
		listTestEDCotizacion.add(edc2);
	}

	public void modificarCotizacion() {
		try {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cotizacion Modificada!", nombreUsuario);
			facesContext.addMessage(null, m);
			loadValuesDefaul();
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Modificado Incorrecto.");
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

	public void agregarNuevaFila(){
		log.info("agregarNuevaFila() ");
		EDCotizacion edc = new EDCotizacion(listEDCotizacion.size()+1, "0","", 1, 0, 0,"",0);
		listEDCotizacion.add(edc);
		FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Fila Agregada !" ,"");
		facesContext.addMessage(null, m);
	}

	public void onRowSelectServicio(SelectEvent event) {
		log.info("onNodeSelectServicio");
		//selectedServicio  = (Servicio) event.getObject();
	}

	public void cargarServicioATabla(){
		double precioAux = 0;
		for(int i=0;i< listEDCotizacion.size();i++){
			if(listEDCotizacion.get(i).getId()==selectedIdEDCotizacion){
				EDCotizacion element = listEDCotizacion.get(i);
				precioAux = selectedServicio.getPrecioReferencial();
				element.setDescripcion(selectedServicio.getNombre());
				element.setCantidad(1);
				element.setPrecioUnitario(selectedServicio.getPrecioReferencial());
				element.setImporte(selectedServicio.getPrecioReferencial());
				listEDCotizacion.set(i, element);
			}
		}
		totalImporte = totalImporte + precioAux;
	}

	public void cargarListServicioATabla(){
		log.info("cargarServicioATabla");
		EDCotizacion element = new EDCotizacion();
		element.setId(busquedaServicio.getId());
		element.setDescripcion(busquedaServicio.getNombre());
		element.setCantidad(cantidad);
		element.setDescuento(descuento);
		element.setPrecioUnitario(busquedaServicio.getPrecioReferencial());
		element.setImporte(totalImportePorServicio);
		element.setEstado(busquedaServicio.getEstado());
		listEDCotizacion.add(element);
		totalImporte = totalImporte + totalImportePorServicio;
		int index = listServicio.indexOf(busquedaServicio);
		busquedaServicio.setEstado("RM");
		listServicio.set(index, busquedaServicio);
		cancelarAgregarServicio();
	}

	public void resetDatosServicio(){
		totalImportePorServicio = 0;
		cantidad = 1;
		descuento = 0 ;
		busquedaServicio = new Servicio();
		texto = "";
	}

	public void verificarSeleccionado(String var, int id){
		setSelectedIdEDCotizacion(id);
		selectedCantidad = true;
		switch (var) {
		case "CANTIDAD":
			selectedCantidad = true;
			selectedPrecioUnitario = false;
			setSelectedImporte(false);
			selectedPorcentaje1 = false;
			break;
		case "PRECIO UNITARIO":
			selectedCantidad = false;
			selectedPrecioUnitario = true;
			setSelectedImporte(false);
			selectedPorcentaje1 = false;
			break;
		case "IMPORTE":
			selectedCantidad = false;
			selectedPrecioUnitario = false;
			setSelectedImporte(true);
			selectedPorcentaje1 = false;
			break;
		case "PORCENTAJE 1":
			selectedCantidad = false;
			selectedPrecioUnitario = false;
			setSelectedImporte(false);
			selectedPorcentaje1 = true;
			break;
		default:
			break;
		}
	}

	public void onRowEdit(RowEditEvent event) {
		EDCotizacion auxEDCotizacion = (EDCotizacion)usersDataTable.getRowData();
		if(selectedCantidad){
			int newValueCantidad = auxEDCotizacion.getCantidad();
			for(int i=0;i< listEDCotizacion.size();i++){
				if(listEDCotizacion.get(i).getId()==auxEDCotizacion.getId()){
					EDCotizacion element = listEDCotizacion.get(i);
					double descuento = element.getDescuento();
					double precioUnitario = element.getPrecioUnitario();
					element.setImporte((precioUnitario - (precioUnitario*descuento)/100)  * newValueCantidad);
					listEDCotizacion.set(i, element);
				}
			}
		}
		if(selectedPrecioUnitario){
			double newValuePrecioUnitario = auxEDCotizacion.getPrecioUnitario();
			for(int i=0; i < listEDCotizacion.size(); i++){
				if(listEDCotizacion.get(i).getId()==auxEDCotizacion.getId()){
					EDCotizacion element = listEDCotizacion.get(i);
					int cantidad = element.getCantidad();
					double descuento = element.getDescuento();
					element.setImporte((newValuePrecioUnitario - (newValuePrecioUnitario*descuento)/100) * cantidad);
					listEDCotizacion.set(i, element);
				}
			}				
		}
		if(selectedPorcentaje1){
			double newValueProcentaje = auxEDCotizacion.getDescuento();
			for(int i=0; i < listEDCotizacion.size(); i++){
				if(listEDCotizacion.get(i).getId()==auxEDCotizacion.getId()){
					EDCotizacion element = listEDCotizacion.get(i);
					int cantidad = element.getCantidad();
					double precio = element.getPrecioUnitario();
					element.setImporte((precio - ((precio*newValueProcentaje)/100) )  * cantidad);
					listEDCotizacion.set(i, element);
				}
			}
		}
		calcularTotalImporte();
	}

	private void calcularTotalImporte(){
		double importeTotalAux = 0;
		for(EDCotizacion element : listEDCotizacion){
			importeTotalAux	= importeTotalAux + element.getImporte();	
		}
		totalImporte = importeTotalAux ;
	}

	public void buttonAgregarServicio(){
		newServicio = new Servicio();
		agregarServicio = true;
	}

	public void registrarNuevoServicio(){
		try{
			newServicio.setEstado("AC");
			newServicio.setUsuarioRegistro(nombreUsuario);
			newServicio.setFechaRegistro(new Date());
			newServicio.setEmpresa(empresaLogin);
			newServicio.setCentroCosto(busquedaCentroCosto);
			newServicio.setCuenta(busquedaCuenta);
			newServicio = servicioRegistration.create(newServicio);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Servicio Registrado!", newServicio.getNombre());
			facesContext.addMessage(null, m);
			agregarServicio = false;
			texto = newServicio.getNombre();
			busquedaServicio = newServicio;
			totalImportePorServicio = newServicio.getPrecioReferencial();
			listServicio.add(newServicio);
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}		
	}

	public void registrarCliente(){
		try{
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			String credito = permitirCredito?"SI":"NO";
			newClientes.setPermitirCredito(credito);
			newClientes.setEstado(estado);
			newClientes.setFechaRegistro(new Date());
			newClientes.setUsuarioRegistro(nombreUsuario);
			newClientes.setEmpresa(empresaLogin);
			clienteRegistration.create(newClientes);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cliente Registrado!", newClientes.getNombre());
			facesContext.addMessage(null, m);
			textoAutoCompleteCliente = newClientes.getNombre();
			busquedaCliente = newClientes;
			listCliente.add(newClientes);
			newClientes = new Cliente();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void cancelarAgregarServicio(){
		busquedaServicio = new Servicio();
		texto = "";
		totalImportePorServicio = 0;
		cantidad = 1;
		descuento = 0;
	}

	public List<Cliente> completeTextCliente(String query) {
		String upperQuery = query.toUpperCase();
		List<Cliente> results = new ArrayList<Cliente>();
		for(Cliente i : listCliente) {
			if(i.getNombre().toUpperCase().startsWith(upperQuery)){
				results.add(i);
			}
		}         
		return results;
	}

	public void onItemSelectCliente(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(Cliente s : listCliente){
			if(s.getNombre().equals(nombre)){
				busquedaCliente= s;
			}
		}
	}

	public String getURLImprimir(){
		try{
			/*
			 * $P{pGestion}
	 		   $P{pNumero} *
	 		   $P{pEmpresa}
			 */
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();  
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
			String urlPDFreporte = urlPath+"ReporteCotizacion?pGestion="+gestionLogin.getId()+"&pEmpresa="+empresaLogin.getId()+"&pNumero="+numeroCotizacion;
			log.info("getURL() -> "+urlPDFreporte);
			return urlPDFreporte;
		}catch(Exception e){
			log.info("getURL error: "+e.getMessage());
			return "error";
		}
	}

	public List<PlanCuenta> completeTextCuenta(String query) {
		String upperQuery = query.toUpperCase();
		listCuentasAuxiliares = planCuentaRepository.findQueryAllAuxiliarByEmpresa(empresaLogin,upperQuery);
		return listCuentasAuxiliares;
	}

	public List<CentroCosto> completeTextCentroCosto(String query) {
		String upperQuery = query.toUpperCase();
		listCentroCosto = centroCostoRepository.findQueryAllCentroCostoByEmpresa(empresaLogin,upperQuery);
		return listCentroCosto;
	}

	public void onItemSelectCuenta(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(PlanCuenta s : listCuentasAuxiliares){
			if(s.getDescripcion().equals(nombre)){
				setBusquedaCuenta(s);
			}
		}
	}

	public void onItemSelectCentroCosto(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(CentroCosto s : listCentroCosto){
			if(s.getNombre().equals(nombre)){
				setBusquedaCentroCosto(s);
			}
		}
	}
	
	private MonedaEmpresa buscarMonedaEmpresaByLocal(String nombreMonedaEmpresa){
		for(MonedaEmpresa me: listMonedaEmpresa){
			if(nombreMonedaEmpresa.equals(me.getMoneda().getNombre())){
				return me;
			}
		}
		return null;
	}

	// --------------------   get and set  -------------------------
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

	public void cambiarModificar(){
		setModificar(false);
	}

	public List<Usuario> getListUsuario() {
		return listUsuario;
	}

	public void setListUsuario(List<Usuario> listUsuario) {
		this.listUsuario = listUsuario;
	}

	public List<EDCotizacion> getListEDCotizacion() {
		return listEDCotizacion;
	}

	public void setListEDCotizacion(List<EDCotizacion> listEDCotizacion) {
		this.listEDCotizacion = listEDCotizacion;
	}

	public EDCotizacion getSelectedEDCotizacion() {
		return selectedEDCotizacion;
	}

	public void setSelectedEDCotizacion(EDCotizacion selectedEDCotizacion) {
		this.selectedEDCotizacion = selectedEDCotizacion;
	}

	public int getSelectedIdEDCotizacion() {
		return selectedIdEDCotizacion;
	}

	public void setSelectedIdEDCotizacion(int selectedIdEDCotizacion) {
		this.selectedIdEDCotizacion = selectedIdEDCotizacion;
	}

	public double getTotalImporte() {
		return totalImporte;
	}

	public void setTotalImporte(double totalImporte) {
		this.totalImporte = totalImporte;
	}

	public Servicio getSelectedServicio() {
		return selectedServicio;
	}

	public void setSelectedServicio(Servicio selectedServicio) {
		this.selectedServicio = selectedServicio;
	}

	public List<Servicio> getSelectedListServicio() {
		return selectedListServicio;
	}

	public void setSelectedListServicio(List<Servicio> selectedListServicio) {
		this.selectedListServicio = selectedListServicio;
	}

	public List<Cliente> getListCliente() {
		return listCliente;
	}

	public void setListCliente(List<Cliente> listCliente) {
		this.listCliente = listCliente;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public List<Servicio> getListServicio() {
		return listServicio;
	}

	public void setListServicio(List<Servicio> listServicio) {
		this.listServicio = listServicio;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public Servicio getBusquedaServicio() {
		return busquedaServicio;
	}

	public void setBusquedaServicio(Servicio busquedaServicio) {
		this.busquedaServicio = busquedaServicio;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}

	public double getTotalImportePorServicio() {
		return totalImportePorServicio;
	}

	public void setTotalImportePorServicio(double totalImportePorServicio) {
		this.totalImportePorServicio = totalImportePorServicio;
	}

	public List<EDCotizacion> getListTestEDCotizacion() {
		return listTestEDCotizacion;
	}

	public void setListTestEDCotizacion(List<EDCotizacion> listTestEDCotizacion) {
		this.listTestEDCotizacion = listTestEDCotizacion;
	}

	public UIData getUsersDataTable() {
		return usersDataTable;
	}

	public void setUsersDataTable(UIData usersDataTable) {
		this.usersDataTable = usersDataTable;
	}

	public boolean isAgregarServicio() {
		return agregarServicio;
	}

	public void setAgregarServicio(boolean agregarServicio) {
		this.agregarServicio = agregarServicio;
	}

	public Servicio getNewServicio() {
		return newServicio;
	}

	public void setNewServicio(Servicio newServicio) {
		this.newServicio = newServicio;
	}

	public Cliente getBusquedaCliente() {
		return busquedaCliente;
	}

	public void setBusquedaCliente(Cliente busquedaCliente) {
		this.busquedaCliente = busquedaCliente;
	}

	public String getTextoAutoCompleteCliente() {
		return textoAutoCompleteCliente;
	}

	public void setTextoAutoCompleteCliente(String textoAutoCompleteCliente) {
		this.textoAutoCompleteCliente = textoAutoCompleteCliente;
	}

	public Integer getNumeroCotizacion() {
		return numeroCotizacion;
	}

	public void setNumeroCotizacion(Integer numeroCotizacion) {
		this.numeroCotizacion = numeroCotizacion;
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

	public PlanCuenta getBusquedaCuenta() {
		return busquedaCuenta;
	}

	public void setBusquedaCuenta(PlanCuenta busquedaCuenta) {
		this.busquedaCuenta = busquedaCuenta;
	}

	public CentroCosto getBusquedaCentroCosto() {
		return busquedaCentroCosto;
	}

	public void setBusquedaCentroCosto(CentroCosto busquedaCentroCosto) {
		this.busquedaCentroCosto = busquedaCentroCosto;
	}

	public boolean isSelectedImporte() {
		return selectedImporte;
	}

	public void setSelectedImporte(boolean selectedImporte) {
		this.selectedImporte = selectedImporte;
	}

	public boolean isPermitirCredito() {
		return permitirCredito;
	}

	public void setPermitirCredito(boolean permitirCredito) {
		this.permitirCredito = permitirCredito;
	}

	public Cliente getNewClientes() {
		return newClientes;
	}

	public void setNewClientes(Cliente newClientes) {
		this.newClientes = newClientes;
	}

	public String getNombreEstado() {
		return nombreEstado;
	}

	public void setNombreEstado(String nombreEstado) {
		this.nombreEstado = nombreEstado;
	}

	public List<Cotizacion> getListCotizacion() {
		return listCotizacion;
	}

	public void setListCotizacion(List<Cotizacion> listCotizacion) {
		this.listCotizacion = listCotizacion;
	}

	public List<MonedaEmpresa> getListMonedaEmpresa() {
		return listMonedaEmpresa;
	}

	public void setListMonedaEmpresa(List<MonedaEmpresa> listMonedaEmpresa) {
		this.listMonedaEmpresa = listMonedaEmpresa;
	}

	public MonedaEmpresa getMonedaEmpresa() {
		return monedaEmpresa;
	}

	public void setMonedaEmpresa(MonedaEmpresa monedaEmpresa) {
		this.monedaEmpresa = monedaEmpresa;
	}

	public String getNombreMonedaEmpresa() {
		return nombreMonedaEmpresa;
	}

	public void setNombreMonedaEmpresa(String nombreMonedaEmpresa) {
		this.nombreMonedaEmpresa = nombreMonedaEmpresa;
		monedaEmpresa = buscarMonedaEmpresaByLocal(nombreMonedaEmpresa);
	}

}
