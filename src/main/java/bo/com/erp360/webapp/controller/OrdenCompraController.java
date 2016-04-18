package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.BienServicioRepository;
import bo.com.erp360.webapp.data.ComprobanteRepository;
import bo.com.erp360.webapp.data.DetalleOrdenCompraRepository;
import bo.com.erp360.webapp.data.MayorRepository;
import bo.com.erp360.webapp.data.OrdenCompraRepository;
import bo.com.erp360.webapp.data.ProveedorRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.TipoComprobanteRepository;
import bo.com.erp360.webapp.model.AsientoContable;
import bo.com.erp360.webapp.model.BienServicio;
import bo.com.erp360.webapp.model.Compra;
import bo.com.erp360.webapp.model.Comprobante;
import bo.com.erp360.webapp.model.DetalleOrdenCompra;
import bo.com.erp360.webapp.model.Egreso;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Mayor;
import bo.com.erp360.webapp.model.OrdenCompra;
import bo.com.erp360.webapp.model.Proveedor;
import bo.com.erp360.webapp.model.Servicio;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TipoComprobante;
import bo.com.erp360.webapp.service.AsientoContableRegistration;
import bo.com.erp360.webapp.service.CompraRegistration;
import bo.com.erp360.webapp.service.ComprobanteRegistration;
import bo.com.erp360.webapp.service.DetalleOrdenCompraRegistration;
import bo.com.erp360.webapp.service.EgresoRegistration;
import bo.com.erp360.webapp.service.MayorRegistration;
import bo.com.erp360.webapp.service.OrdenCompraRegistration;
import bo.com.erp360.webapp.util.EDOrdenCompra;
import bo.com.erp360.webapp.util.Fechas;
import bo.com.erp360.webapp.util.NumerosToLetras;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "ordenCompraController")
@ConversationScoped
public class OrdenCompraController implements Serializable {

	private static final long serialVersionUID = -3866928887474891813L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	private Conversation conversation;
	
	@Inject
	private ComprobanteRepository comprobanteRepository;

	@Inject
	private OrdenCompraRepository ordenCompraRepository;

	@Inject
	private ProveedorRepository proveedorRepository;

	@Inject
	private OrdenCompraRegistration ordenCompraRegistration;

	@Inject
	private TipoComprobanteRepository tipoComprobanteRepository;

	@Inject
	private DetalleOrdenCompraRepository detalleOrdenCompraRepository;

	@Inject
	private DetalleOrdenCompraRegistration detalleOrdenCompraRegistration;

	@Inject
	private BienServicioRepository bienServicioRepository;

	@Inject
	private SucursalRepository sucursalRepository;

	@Inject
	private ComprobanteRegistration comprobanteRegistration;

	@Inject
	private AsientoContableRegistration asientoContableRegistration;

	@Inject
	private EgresoRegistration egresoRegistration;

	@Inject
	private MayorRegistration mayorRegistration;

	@Inject
	private MayorRepository mayorRepository;
	
	@Inject
	private CompraRegistration compraRegistration;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	private Event<String> pushEventSucursal;
	
	private Logger log = Logger.getLogger(this.getClass());

	//estados
	private boolean crear = true;
	private boolean registrar = false;
	private boolean modificar = false;

	//estado orden compra ya registradas
	private boolean ordenCompraSeleccionada ;

	private String tituloPanel = "Registrar Compra";
	private String nombreProveedor;
	private String tipoColumnTable = "col-md-12"; //8
	private String nombreUsuario;
	private String nombreTipoCompra;

	//BienServicio
	private String textoServicio;
	private Integer cantidad = 1;
	private double totalImportePorServicio;
	private double descuento = 0;
	private double totalImporte = 0;

	//Login
	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	//Object Entity
	private BienServicio busquedaBienServicio;
	private OrdenCompra newOrdenCompra;
	private OrdenCompra selectedOrdenCompra;
	private Proveedor selectedProveedor;
	private Servicio servicioXOrdenCompra;
	private EDOrdenCompra selectedEDOrdenCompra;
	private Sucursal selectedSucursal;

	//libro de compra
	private Compra libroCompra;

	private List<Sucursal> listSucursal;
	private List<Proveedor> listProveedor = new ArrayList<Proveedor>();
	private List<OrdenCompra> listOrdenCompra = new ArrayList<OrdenCompra>();
	private List<OrdenCompra> listFilterOrdenCompra = new ArrayList<OrdenCompra>();
	private List<BienServicio> listBienServicio = new ArrayList<BienServicio>();
	private List<Servicio> listServicioXOrdenCompra = new ArrayList<Servicio>();
	private String[] listEstado = {"ACTIVO","INACTIVO"};
	private List<EDOrdenCompra> listEDOrdenCompra = new ArrayList<EDOrdenCompra>();


	@PostConstruct
	public void initNewOrdenCompra() {

		log.info(" init new OrdenCompraController");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();
		
		// tituloPanel
		tituloPanel = "Orden de Compra";
		listSucursal = sucursalRepository.findAllActivasByEmpresa(empresaLogin);

		modificar = false;
		loadDefault();
	}

	private void loadDefault(){
		crear = true;
		registrar = false;
		modificar = false;

		selectedSucursal = new Sucursal();
		ordenCompraSeleccionada = false;

		libroCompra = new Compra();
		newOrdenCompra = new OrdenCompra();
		selectedOrdenCompra = new OrdenCompra();
		selectedProveedor = new Proveedor();
		servicioXOrdenCompra = new Servicio();
		selectedEDOrdenCompra = new EDOrdenCompra();
		listOrdenCompra = ordenCompraRepository.findAllByEmpresa(empresaLogin);
		listProveedor = proveedorRepository.findAllActivasByEmpresa(empresaLogin);
		selectedProveedor = listProveedor.size()>0?listProveedor.get(0):new Proveedor();
		if(selectedProveedor.getId()!=0){
			nombreProveedor = selectedProveedor.getDescripcion();
			listBienServicio = bienServicioRepository.findAllByProveedor(selectedProveedor);
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

	/**
	 * @method registrar() , registra una nueva orden de compra, con sus
	 * respectivos bieServicios cargados
	 */
	public void registrar(){
		try{
			newOrdenCompra.setEmpresa(empresaLogin);
			newOrdenCompra.setEstado("AC");
			newOrdenCompra.setFechaRegistro(new Date());
			newOrdenCompra.setProveedor(selectedProveedor);
			newOrdenCompra.setPermitirCredito("NO");
			newOrdenCompra.setDiasCredito(0);
			newOrdenCompra.setSucursal(selectedSucursal);
			newOrdenCompra.setUsuarioRegistro(nombreUsuario);
			newOrdenCompra.setTotal(totalImporte);
			newOrdenCompra.setGestion(gestionLogin);
			newOrdenCompra = ordenCompraRegistration.create(newOrdenCompra);
			for(EDOrdenCompra eDOC : listEDOrdenCompra){
				DetalleOrdenCompra dOC = new DetalleOrdenCompra();
				dOC.setBienServicio(eDOC.getObjectBienServicio());
				dOC.setCantidad(eDOC.getCantidad());
				dOC.setEstado("AC");
				dOC.setFechaRegistro(new Date());
				dOC.setOrdenCompra(newOrdenCompra);
				dOC.setTotal(eDOC.getSubTotal());
				dOC.setCantidad(eDOC.getCantidad());
				dOC.setDescuento(eDOC.getDescuento());
				dOC.setUsuarioRegistro(nombreUsuario);
				detalleOrdenCompraRegistration.create(dOC);
			}
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Orden Compra Registrada", selectedProveedor.getNombre());
			facesContext.addMessage(null, m);
			resetearFitrosTabla("formTableOrdenCompra:dataTableOrdenCompra");
			loadDefault();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void modificarVista(){

	}

	public void modificar(){
		try{
			newOrdenCompra.setProveedor(selectedProveedor);
			newOrdenCompra.setUsuarioRegistro(nombreUsuario);

			ordenCompraRegistration.update(newOrdenCompra);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Orden Compra Modificada!", selectedProveedor.getNombre());
			facesContext.addMessage(null, m);
			loadDefault();
		}catch(Exception e){
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void eliminar(){
		try{
			newOrdenCompra.setEstado("RM");
			ordenCompraRegistration.update(newOrdenCompra);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Orden Compra Eliminada!", selectedProveedor.getNombre());
			facesContext.addMessage(null, m);
			loadDefault();
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

	public void actualizarFormReg(){
		log.info("actualizarFormReg");
		ordenCompraSeleccionada = false;
		crear = true;
		registrar = false;
		modificar = false;
		setTipoColumnTable("col-md-12");
		resetearFitrosTabla("formTableOrdenCompra:dataTableOrdenCompra");
		resetearFitrosTabla("formTableBienServicio:dataTableBienServicio");

		newOrdenCompra = new OrdenCompra();	
		selectedOrdenCompra = new OrdenCompra();
	}

	private Proveedor obtenerProveedorByLocal(String nombre){
		for(Proveedor pro : listProveedor){
			if(pro.getNombre().equals(nombre)){
				return pro;
			}
		}
		return null;
	}

	public void cambiarAspecto(){
		crear = false;
		registrar = true;
		modificar = false;
		setTipoColumnTable("col-md-8");
	}


	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}

	public void onRowSelect(SelectEvent event) {
		ordenCompraSeleccionada = true;
		//newOrdenCompra = new OrdenCompra();
		//newOrdenCompra = selectedOrdenCompra;
		crear = false;
		//registrar = false;
		//modificar = true;
		//tipoColumnTable = "col-md-8";
		//resetearFitrosTabla("formTableOrdenCompra:dataTableOrdenCompra");
		if(selectedOrdenCompra.getEstado().equals("AC")){
			nombreTipoCompra = selectedOrdenCompra.getTipo();//INTENA - BOLETO
			libroCompra.setFechaFactura(new Date());
			libroCompra.setRazonSocial(selectedOrdenCompra.getProveedor().getNombre());
			libroCompra.setNitProveedor(selectedOrdenCompra.getProveedor().getNit());
			libroCompra.setImporteTotal(selectedOrdenCompra.getTotal());
			double exento = 0;
			double ice = 0;
			double creditoFiscal = 0.13;

			libroCompra.setImporteExcentos(exento);
			libroCompra.setImporteICE(ice);
			libroCompra.setImporteNoSujetoCreditoFiscal(selectedOrdenCompra.getTotal()-ice-exento);
			libroCompra.setCreditoFiscal(libroCompra.getImporteNoSujetoCreditoFiscal()*creditoFiscal);
		}
	}

	public void onRowSelectServicio(SelectEvent event) {
		newOrdenCompra = new OrdenCompra();
		newOrdenCompra = selectedOrdenCompra;
		crear = false;
		registrar = false;
		modificar = true;
		tipoColumnTable = "col-md-8";
		resetearFitrosTabla("formTableBienServicio:dataTableBienServicio");
	}

	public void onItemSelect(SelectEvent event) {
		String nombre =  event.getObject().toString();
		for(BienServicio s : listBienServicio){
			if(s.getNombre().equals(nombre)){
				busquedaBienServicio = s;
				totalImportePorServicio = s.getPrecioReferencial();				
			}
		}
	}

	public List<BienServicio> completeText(String query) {
		String upperQuery = query.toUpperCase();
		List<BienServicio> results = new ArrayList<BienServicio>();
		for(BienServicio i : listBienServicio) {
			if((i.getNombre().toUpperCase().startsWith(upperQuery)) && !(i.getEstado().equals("RM"))){
				results.add(i);
			}
		}         
		return results;
	}

	public void cargarBienServicio(){
		EDOrdenCompra oc = new EDOrdenCompra(busquedaBienServicio.getId(),busquedaBienServicio.getNombre(), busquedaBienServicio.getPrecioReferencial(), cantidad, totalImportePorServicio,busquedaBienServicio,descuento);
		totalImporte  = totalImporte + totalImportePorServicio;
		//newOrdenCompra.setTotal(totalImporte);
		listEDOrdenCompra.add(oc);
		resetearFitrosTabla("formTableBienServicio:dataTableBienServicio");
		cancelarAgregarServicio();
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("formReg");
	}

	public void modificarDetalleBienServicio(){
		totalImportePorServicio = ((busquedaBienServicio.getPrecioReferencial() - (busquedaBienServicio.getPrecioReferencial()*descuento)/100) * cantidad);	
	}

	public void cancelarAgregarServicio(){
		busquedaBienServicio = new BienServicio();
		textoServicio = "";
		totalImportePorServicio = 0;
		cantidad = 1;
		descuento = 0;
	}

	public void registrarCompra(){
		log.info("registrarCompra()");
		//numeroFactura = newCompra.getNumeroFactura();
		//log.info("numeroFactura: "+numeroFactura);

		//registrar comprobante de egreso

		//Comprobante
		Comprobante comprobante = new Comprobante();
		comprobante.setEstado("AC");
		comprobante.setFechaRegistro(new Date());
		comprobante.setUsuarioRegistro(nombreUsuario);

		//tipo de comprobante
		TipoComprobante tc = tipoComprobanteRepository.findByNombreAndEmpresa("EGRESO", empresaLogin);
		comprobante.setTipoComprobante(tc);
		comprobante.setFecha(libroCompra.getFechaFactura());

		// MonedaEmpresa
		//null
		comprobante.setMonedaEmpresa(null);

		//sucursal
		comprobante.setSucursal(selectedOrdenCompra.getSucursal());

		//TipoCambio
		///null
		comprobante.setTipoCambio(null);

		//Empresa
		comprobante.setEmpresa(empresaLogin);

		//Gestion
		comprobante.setGestion(gestionLogin);
		
		comprobante.setImporteTotalDebeNacional(selectedOrdenCompra.getTotal());
		Date fechaComprobante = Fechas.cambiarYearDate(Integer.valueOf(gestionLogin.getGestion()));
		//insettar numero correlativo de comprobante
		int numeroComprobante = obtnerNumeroComprobante(fechaComprobante, tc);
		comprobante.setNumero(numeroComprobante);
		
		comprobante.setCorrelativo(obtenerCorrelativo(numeroComprobante));
		//insertar importe literal		
		comprobante.setImporteLiteralNacional(obtenerMontoLiteral(selectedOrdenCompra.getTotal()));
		
		comprobante.setGlosa(selectedOrdenCompra.getConcepto());
		comprobante.setNombre(selectedOrdenCompra.getProveedor().getNombre());

		comprobante = comprobanteRegistration.create(comprobante);

		//--------------------------------------
		//AsientoComtable
		List<DetalleOrdenCompra> listDetalleOrdenCompra = detalleOrdenCompraRepository.findAllByOrdenCompra(selectedOrdenCompra);

		for(DetalleOrdenCompra doc : listDetalleOrdenCompra){
			AsientoContable asientoContable = new AsientoContable();
			asientoContable.setCentroCosto(null);
			asientoContable.setDebeExtranjero(0);
			asientoContable.setDebeNacional(doc.getTotal());
			asientoContable.setHaberExtranjero(0);
			asientoContable.setHaberNacional(0);
			asientoContable.setComprobante(comprobante);
			asientoContable.setGlosa(doc.getOrdenCompra().getConcepto());
			asientoContable.setPlanCuenta(doc.getBienServicio().getCuenta());
			asientoContable = asientoContableRegistration.create(asientoContable);
			registrarMayor(asientoContable);
		}

		//--------------------------------------
		//EGRESO
		Egreso egreso = new Egreso();
		egreso.setEstado("AC");
		egreso.setFechaRegistro(new Date());
		egreso.setPlanCuentaBancaria(null);
		egreso.setCodigo("");
		egreso.setNroDocumento("");
		egreso.setUsuarioRegistro(nombreUsuario);
		egreso.setComprobante(comprobante);
		egresoRegistration.create(egreso);

		//registrar comprobante de obligacion

		//modificar Orden Compra
		//estado = procesado
		selectedOrdenCompra.setEstado("PR");
		ordenCompraRegistration.update(selectedOrdenCompra);
		
		//registrar compra
		libroCompra.setEstado("AC");
		libroCompra.setEmpresa(empresaLogin);
		libroCompra.setFechaRegistro(new Date());
		libroCompra.setComprobante(comprobante);
		compraRegistration.create(libroCompra);

		//cargar datos por defecto
		loadDefault();

		//actualizar tabla
		resetearFitrosTabla("formTableOrdenCompra:dataTableOrdenCompra");


		//mostrar dialog orden compra

		//RequestContext context = RequestContext.getCurrentInstance();
		//context.execute("PF('dlgCotizacionVistaPrevia').show();");

	}
	
	public String obtenerMontoLiteral(double totalFactura) {
		log.info("Total Entero Factura >>>>> " + totalFactura);
		NumerosToLetras convert = new NumerosToLetras();
		String totalLiteral;
		try {
			totalLiteral = convert.convertNumberToLetter(totalFactura);
			return totalLiteral;
		} catch (Exception e) {
			log.info("Error en obtenerMontoLiteral: "
					+ e.getMessage());
			return "Error Literal";
		}
	}
	
	private String obtenerCorrelativo(int comprobante){
		// pather = "1508-000001";
		Date fecha = new Date(); 
		String year = new SimpleDateFormat("yy").format(fecha);
		String mes = new SimpleDateFormat("MM").format(fecha);
		return year+mes+"-"+String.format("%06d", comprobante);
	}
	
	private int obtnerNumeroComprobante(Date fechaComprobante, TipoComprobante selectedTipoComprobante){
		return comprobanteRepository.obtenerNumeroComprobante(fechaComprobante,empresaLogin, selectedSucursal,selectedTipoComprobante);
	}

	private void registrarMayor(AsientoContable asientoContable){
		try{
			double debitoNacional = asientoContable.getDebeNacional();
			double creditoNacional = asientoContable.getHaberNacional();
			double debitoExtranjero = asientoContable.getDebeExtranjero();
			double creditoExtranjero = asientoContable.getHaberExtranjero();
			//obtener saldo anterior
			//AsientoContable asientoAnterior = asientoContableRepository.findByCuenta(asientoContable.getPlanCuenta(), gestionLogin);
			Mayor mayorAnterior = mayorRepository.findNumeroByPlanCuenta(asientoContable.getPlanCuenta(),gestionLogin);
			double saldoAnteriorNacional = mayorAnterior!=null? mayorAnterior.getSaldoNacional():0;
			log.info("saldoAnteriorNacional : "+saldoAnteriorNacional);
			double saldoAnteriorExtranjero = mayorAnterior!=null? mayorAnterior.getSaldoExtranjero():0;
			log.info("saldoAnteriorExtranjero : "+saldoAnteriorExtranjero);
			Mayor mayor = new Mayor();
			mayor.setEstado("AC");
			mayor.setUsuarioRegistro(nombreUsuario);
			mayor.setFechaRegistro(new Date());
			mayor.setAsientoContable(asientoContable);
			mayor.setDebitoNacional(debitoNacional);
			mayor.setCreditoNacional(creditoNacional);
			mayor.setDebitoExtranjero(debitoExtranjero);
			mayor.setCreditoExtranjero(creditoExtranjero);
			//condicion del debe = debito y haber = credito
			if(debitoNacional > creditoNacional){
				mayor.setSaldoNacional(saldoAnteriorNacional + debitoNacional);
				mayor.setSaldoExtranjero(saldoAnteriorExtranjero + debitoExtranjero);
			}else{
				mayor.setSaldoNacional(saldoAnteriorNacional - creditoNacional);
				mayor.setSaldoExtranjero(saldoAnteriorExtranjero - creditoExtranjero);
			}
			mayorRegistration.create(mayor);
		}catch(Exception e){
			log.info("registrarMayor ERROR "+e.getMessage());
		}
	}

	// ---------    get and set  -------

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

	public OrdenCompra getNewOrdenCompra() {
		return newOrdenCompra;
	}

	public void setNewOrdenCompra(OrdenCompra newOrdenCompra) {
		this.newOrdenCompra = newOrdenCompra;
	}

	public List<Proveedor> getListProveedor() {
		return listProveedor;
	}

	public void setListProveedor(List<Proveedor> listProveedor) {
		this.listProveedor = listProveedor;
	}

	public Proveedor getSelectedProveedor() {
		return selectedProveedor;
	}

	public void setSelectedProveedor(Proveedor selectedProveedor) {
		this.selectedProveedor = selectedProveedor;
	}

	public String getNombreProveedor() {
		return nombreProveedor;
	}

	public void setNombreProveedor(String nombreProveedor) {
		if(listEDOrdenCompra.size()>0){
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgInfoProveedor').show();");
		}else{
			this.nombreProveedor = nombreProveedor;
			selectedProveedor = obtenerProveedorByLocal(nombreProveedor);
		}
	}

	public String[] getListEstado() {
		return listEstado;
	}

	public void setListEstado(String[] listEstado) {
		this.listEstado = listEstado;
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

	public List<OrdenCompra> getListOrdenCompra() {
		return listOrdenCompra;
	}

	public void setListOrdenCompra(List<OrdenCompra> listOrdenCompra) {
		this.listOrdenCompra = listOrdenCompra;
	}

	public OrdenCompra getSelectedOrdenCompra() {
		return selectedOrdenCompra;
	}

	public void setSelectedOrdenCompra(OrdenCompra selectedOrdenCompra) {
		this.selectedOrdenCompra = selectedOrdenCompra;
	}

	public List<OrdenCompra> getListFilterOrdenCompra() {
		return listFilterOrdenCompra;
	}

	public void setListFilterOrdenCompra(List<OrdenCompra> listFilterOrdenCompra) {
		this.listFilterOrdenCompra = listFilterOrdenCompra;
	}

	public String getTextoServicio() {
		return textoServicio;
	}

	public void setTextoServicio(String textoServicio) {
		this.textoServicio = textoServicio;
	}

	public BienServicio getBusquedaBienServicio() {
		return busquedaBienServicio;
	}

	public void setBusquedaBienServicio(BienServicio busquedaBienServicio) {
		this.busquedaBienServicio = busquedaBienServicio;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public double getTotalImportePorServicio() {
		return totalImportePorServicio;
	}

	public void setTotalImportePorServicio(double totalImportePorServicio) {
		this.totalImportePorServicio = totalImportePorServicio;
	}

	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}

	public double getTotalImporte() {
		return totalImporte;
	}

	public void setTotalImporte(double totalImporte) {
		this.totalImporte = totalImporte;
	}

	public List<Servicio> getListServicioXOrdenCompra() {
		return listServicioXOrdenCompra;
	}

	public void setListServicioXOrdenCompra(List<Servicio> listServicioXOrdenCompra) {
		this.listServicioXOrdenCompra = listServicioXOrdenCompra;
	}

	public Servicio getServicioXOrdenCompra() {
		return servicioXOrdenCompra;
	}

	public void setServicioXOrdenCompra(Servicio servicioXOrdenCompra) {
		this.servicioXOrdenCompra = servicioXOrdenCompra;
	}

	public List<EDOrdenCompra> getListEDOrdenCompra() {
		return listEDOrdenCompra;
	}

	public void setListEDOrdenCompra(List<EDOrdenCompra> listEDOrdenCompra) {
		this.listEDOrdenCompra = listEDOrdenCompra;
	}

	public List<BienServicio> getListBienServicio() {
		return listBienServicio;
	}

	public void setListBienServicio(List<BienServicio> listBienServicio) {
		this.listBienServicio = listBienServicio;
	}

	public EDOrdenCompra getSelectedEDOrdenCompra() {
		return selectedEDOrdenCompra;
	}

	public void setSelectedEDOrdenCompra(EDOrdenCompra selectedEDOrdenCompra) {
		this.selectedEDOrdenCompra = selectedEDOrdenCompra;
	}

	public boolean isOrdenCompraSeleccionada() {
		return ordenCompraSeleccionada;
	}

	public void setOrdenCompraSeleccionada(boolean ordenCompraSeleccionada) {
		this.ordenCompraSeleccionada = ordenCompraSeleccionada;
	}

	public Compra getLibroCompra() {
		return libroCompra;
	}

	public void setLibroCompra(Compra libroCompra) {
		this.libroCompra = libroCompra;
	}

	public String getNombreTipoCompra() {
		return nombreTipoCompra;
	}

	public void setNombreTipoCompra(String nombreTipoCompra) {
		this.nombreTipoCompra = nombreTipoCompra;
	}

	public Sucursal getSelectedSucursal() {
		return selectedSucursal;
	}

	public void setSelectedSucursal(Sucursal selectedSucursal) {
		this.selectedSucursal = selectedSucursal;
	}

	public List<Sucursal> getListSucursal() {
		return listSucursal;
	}

	public void setListSucursal(List<Sucursal> listSucursal) {
		this.listSucursal = listSucursal;
	}

}
