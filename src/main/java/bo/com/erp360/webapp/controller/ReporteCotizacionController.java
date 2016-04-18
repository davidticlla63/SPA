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
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.ComprobanteRepository;
import bo.com.erp360.webapp.data.CotizacionRepository;
import bo.com.erp360.webapp.data.CotizacionServicioRepository;
import bo.com.erp360.webapp.data.DosificacionRepository;
import bo.com.erp360.webapp.data.MayorRepository;
import bo.com.erp360.webapp.data.TipoComprobanteRepository;
import bo.com.erp360.webapp.model.AsientoContable;
import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.Comprobante;
import bo.com.erp360.webapp.model.Cotizacion;
import bo.com.erp360.webapp.model.CotizacionServicio;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.Mayor;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TipoComprobante;
import bo.com.erp360.webapp.model.Venta;
import bo.com.erp360.webapp.service.AsientoContableRegistration;
import bo.com.erp360.webapp.service.ComprobanteRegistration;
import bo.com.erp360.webapp.service.CotizacionRegistration;
import bo.com.erp360.webapp.service.FacturaRegistration;
import bo.com.erp360.webapp.service.MayorRegistration;
import bo.com.erp360.webapp.service.VentaRegistration;
import bo.com.erp360.webapp.util.CodigoControl7;
import bo.com.erp360.webapp.util.Fechas;
import bo.com.erp360.webapp.util.NumerosToLetras;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "reporteCotizacionController")
@ConversationScoped
public class ReporteCotizacionController implements Serializable {

	private static final long serialVersionUID = 4148570468186296072L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private CotizacionRepository cotizacionRepository;

	@Inject
	private TipoComprobanteRepository tipoComprobanteRepository;

	@Inject
	private CotizacionServicioRepository cotizacionServicioRepository;

	@Inject
	private ComprobanteRepository comprobanteRepository;

	@Inject
	private ComprobanteRegistration comprobanteRegistration;

	@Inject
	private AsientoContableRegistration asientoContableRegistration;

	@Inject
	private MayorRegistration mayorRegistration;

	@Inject
	private MayorRepository mayorRepository;

	@Inject
	private CotizacionRegistration cotizacionRegistration;

	@Inject
	private FacturaRegistration facturaRegistration;

	@Inject
	private DosificacionRepository dosificacionRepository;
	
	@Inject
	private VentaRegistration ventaRegistration;

	private Logger log = Logger.getLogger(this.getClass());

	private String nombreUsuario;
	private @Inject SessionMain sessionMain; //variable del login
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventSucursal;

	private String nombreMes;
	private Integer numeroCotizacion;
	private String urlCotizacion;

	private List<Cotizacion> listCotizacion= new ArrayList<Cotizacion>();
	private List<Cotizacion> listFilterCotizacion = new ArrayList<Cotizacion>();
	private String[] arrayMes = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SEPTIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE","TODO"};

	private Cotizacion selectedCotizacion;

	//estados
	private boolean seleccionado = false;
	private boolean crear = true;
	private boolean seleccionarCotizacion = false;

	@PostConstruct
	public void initNewReporteCotizacion() {

		log.info(" init new initNewReporteCotizacion");
		beginConversation();
		nombreUsuario = sessionMain.getUsuarioLoggin().getLogin();
		empresaLogin = sessionMain.getEmpresaLoggin();
		gestionLogin = sessionMain.getGestionLoggin();

		listCotizacion = cotizacionRepository.findAllByEmpresaGestion(empresaLogin,gestionLogin);

		loadValuesDefaul();
	}

	private void loadValuesDefaul(){
		urlCotizacion = "";
		seleccionado = false;
		nombreMes = "TODO";
		numeroCotizacion = 0;

		selectedCotizacion = new Cotizacion();
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

	public void onRowSelect(SelectEvent event) {
		//seleccionado = true ;
		crear = false;
		seleccionarCotizacion = true;
	}

	public void procesar(){
		listCotizacion = cotizacionRepository.findByNumero(numeroCotizacion);
	}

	// reporte

	public String loadURL(){
		try{
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();  
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
			String urlPDFreporte = urlPath+"ReporteCotizacion?pGestion="+gestionLogin.getId()+"&pEmpresa="+empresaLogin.getId()+"&pNumero="+numeroCotizacion;
			urlCotizacion = urlPDFreporte;
			log.info("getURL() -> "+urlPDFreporte);
			return urlPDFreporte;
		}catch(Exception e){
			log.info("getURL error: "+e.getMessage());
			return "error";
		}
	}

	public void actualizarFormReg(){
		log.info("actualizarFormReg");
		seleccionarCotizacion = false;
		crear = true;
		selectedCotizacion = new Cotizacion();
		resetearFitrosTabla("formTableCotizacion:dataTableCotizacion");
	}

	public void actualizarForm2(){
		//armar URL
		seleccionado = false;
		urlCotizacion = loadURL();
		log.info("cargando......");
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgVistaPreviaCotizacion').show();");

		//resetearFitrosTabla("formTableCotizacion:dataTableCotizacion");
		//loadValuesDefaul();
	}

	//registro automatico de comprobante
	public void procesarCotizacion(){
		log.info("registrarCompra()");

		//----tipo de comprobante
		TipoComprobante tc = tipoComprobanteRepository.findByNombreAndEmpresa("INGRESO", empresaLogin);

		//----Cliente
		Cliente cliente = selectedCotizacion.getCliente();

		//----Obtener dosificacion de sucursal
		Dosificacion dosificacion = dosificacionRepository.findActivaBySucursal(selectedCotizacion.getSucursal());

		//----datos para la factrua
		Date fechaFactura = new Date();
		CodigoControl7 control=new CodigoControl7();
		String CC = control.obtenerCodigoControl(fechaFactura, dosificacion,
				selectedCotizacion.getTotal(), cliente.getNit());

		//---------------------------registrar comprobante de ingreso---------------------
		Comprobante comprobante = new Comprobante();
		comprobante.setEstado("AC");
		comprobante.setFechaRegistro(new Date());
		comprobante.setUsuarioRegistro(nombreUsuario);
		comprobante.setTipoComprobante(tc);
		comprobante.setFecha(new Date());
		comprobante.setMonedaEmpresa(null);// MonedaEmpresa null
		comprobante.setSucursal(selectedCotizacion.getSucursal());//sucursal		
		comprobante.setTipoCambio(null);//Tipo de Cambio null
		comprobante.setEmpresa(empresaLogin);//Empresa
		comprobante.setGestion(gestionLogin);//Gestion
		comprobante.setImporteTotalDebeNacional(selectedCotizacion.getTotal());
		Date fechaComprobante = Fechas.cambiarYearDate(Integer.valueOf(gestionLogin.getGestion()));
		//insertar numero correlativo de comprobante
		int numeroComprobante = obtnerNumeroComprobante(fechaComprobante,selectedCotizacion.getSucursal(), tc);
		comprobante.setNumero(numeroComprobante);
		comprobante.setCorrelativo(obtenerCorrelativo(numeroComprobante));
		comprobante.setImporteLiteralNacional(obtenerMontoLiteral(selectedCotizacion.getTotal()));
		comprobante.setGlosa(selectedCotizacion.getObservacion());
		comprobante.setNombre(cliente.getNombre());
		comprobante = comprobanteRegistration.create(comprobante);

		//--------------------------------AsientoComtable------------------------
		List<CotizacionServicio> listCotizacionServicio = cotizacionServicioRepository.findAllByCotizacion(selectedCotizacion);

		for(CotizacionServicio doc : listCotizacionServicio){
			AsientoContable ac = new AsientoContable();
			ac.setCentroCosto(null);
			ac.setDebeExtranjero(0);
			ac.setDebeNacional(doc.getSubTotal());
			ac.setHaberExtranjero(0);
			ac.setHaberNacional(0);
			ac.setComprobante(comprobante);
			ac.setGlosa(selectedCotizacion.getObservacion());
			ac.setPlanCuenta(doc.getServicio().getCuenta());
			ac = asientoContableRegistration.create(ac);
			//-------------------registro mayor---------------------------
			Mayor mayorAnterior = mayorRepository.findNumeroByPlanCuenta(ac.getPlanCuenta(),gestionLogin);
			mayorRegistration.registrarMayor(mayorAnterior,ac);
		}

		//---------------------------registro de factura------------------------------------
		Factura factura = new Factura();
		factura.setCambio(6.93);
		int numeroFactura = dosificacion.getNumeroSecuencia();// Numero de Factura
		factura.setNumeroFactura(String.valueOf(numeroFactura));
		factura.setNumeroAutorizacion(dosificacion.getNumeroAutorizacion()); //numero de autorizacion
		factura.setFechaLimiteEmision(dosificacion.getFechaLimiteEmision()); // Fecha de Emision
		factura.setTotalFacturado(selectedCotizacion.getTotal()); // Total Bs
		factura.setCodigoControl(CC);// Codigo de Control
		//tipo de cliente
		if(cliente.getTipo().equals("NATURAL")){ // NAURAL o JURIDICO
			factura.setNitCi(cliente.getCi());// CI del Comprador 
		}else{
			factura.setNitCi(cliente.getNit());// NIT del Comprador
		}
		factura.setConcepto("Venta: " + numeroFactura); 
		factura.setEmpresa(empresaLogin);
		factura.setEstado("AC");
		factura.setFechaRegistro(new Date());
		factura.setTipoPago("EFECTIVO");
		factura.setUsuarioRegistro(nombreUsuario);
		factura.setCodigoRespuestaRapida(armarCadenaQR(factura));
		facturaRegistration.create(factura);

		//------------------------registrar libro de venta----------------------
		Venta libroVenta = new Venta();
		libroVenta.setEmpresa(empresaLogin);
		libroVenta.setEstado("AC");
		libroVenta.setEstadoFactura("V");
		libroVenta.setFechaFactura(fechaFactura);
		libroVenta.setCodigoControl(CC);
		libroVenta.setCorrelativo(0);
		libroVenta.setDebitoFiscal(0);
		libroVenta.setFechaRegistro(new Date());
		libroVenta.setImporteTotal(selectedCotizacion.getTotal());
		libroVenta.setImporteExcentos(0);
		libroVenta.setImporteICE(0);
		libroVenta.setImporteSujetoADebitoFiscal(0);
		//tipo de cliente
		if(cliente.getTipo().equals("NATURAL")){// NATURAL o JURIDICO
			libroVenta.setNitCi(cliente.getCi());
			libroVenta.setRazonSocial(cliente.getNombre());
		}else{
			libroVenta.setNitCi(cliente.getNit());
			libroVenta.setRazonSocial(cliente.getRazonSocial());
		}
		libroVenta.setNumeroAutorizacion(dosificacion.getNumeroAutorizacion());
		libroVenta.setNumeroFactura(String.valueOf(numeroFactura));
		ventaRegistration.create(libroVenta);

		//---------------------registrarcomprobante de pago-------------------

		//modificar Cotizacion
		//estado = procesado
		selectedCotizacion.setEstado("PR");
		selectedCotizacion = cotizacionRegistration.update(selectedCotizacion);

		resetearFitrosTabla("formTableCotizacion:dataTableCotizacion");
		loadValuesDefaul();
	}

	public String armarCadenaQR(Factura factura) {
		String cadenaQR = "";
		try {
			cadenaQR = new String();

			// NIT emisor
			cadenaQR = cadenaQR.concat(empresaLogin.getNit());
			cadenaQR = cadenaQR.concat("|");

			// Numero de Factura
			cadenaQR = cadenaQR.concat(factura.getNumeroFactura());
			cadenaQR = cadenaQR.concat("|");

			// Numero de Autorizacion
			cadenaQR = cadenaQR.concat(factura.getNumeroAutorizacion());
			cadenaQR = cadenaQR.concat("|");

			// Fecha de Emision
			cadenaQR = cadenaQR.concat(obtenerFechaEmision(factura.getFechaFactura()));
			cadenaQR = cadenaQR.concat("|");

			// Total Bs
			cadenaQR = cadenaQR.concat(String.valueOf(factura.getTotalFacturado()));
			cadenaQR = cadenaQR.concat("|");

			// Importe Base para el Credito Fiscal
			cadenaQR = cadenaQR.concat(String.valueOf(factura.getTotalFacturado()));
			cadenaQR = cadenaQR.concat("|");

			// Codigo de Control
			cadenaQR = cadenaQR.concat(factura.getCodigoControl());
			cadenaQR = cadenaQR.concat("|");

			// NIT / CI del Comprador
			cadenaQR = cadenaQR.concat(factura.getNitCi());
			cadenaQR = cadenaQR.concat("|");

			// Importe ICE/IEHD/TASAS [cuando corresponda]
			cadenaQR = cadenaQR.concat("0");
			cadenaQR = cadenaQR.concat("|");

			// Importe por ventas no Gravadas o Gravadas a Tasa Cero [cuando
			// corresponda]
			cadenaQR = cadenaQR.concat("0");
			cadenaQR = cadenaQR.concat("|");

			// Importe no Sujeto a Credito Fiscal [cuando corresponda]
			cadenaQR = cadenaQR.concat("0");
			cadenaQR = cadenaQR.concat("|");

			// Descuentos Bonificaciones y Rebajas Obtenidas [cuando
			// corresponda]
			cadenaQR = cadenaQR.concat("0");

			return  cadenaQR;

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error en armarCadenaQR: " + e.getMessage());
			return  cadenaQR;
		}
	}

	private String obtenerFechaEmision(Date fechaEmision) {
		try {
			String DATE_FORMAT = "dd/MM/yyyy";
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.format(fechaEmision);

		} catch (Exception e) {
			log.error("Error en obtenerFechaEmision: "
					+ e.getMessage());
			return "Error Fecha Emision";
		}
	}

	private String registrarFactura(Dosificacion dosificacion,String nitCi){
		String CC="";
		boolean puedoFacturar = puedoFacturarFechaLimite(dosificacion.getFechaLimiteEmision(), new Date());
		if (puedoFacturar) {
			Date fechaFactura = new Date();
			CodigoControl7 control=new CodigoControl7();
			 CC = control.obtenerCodigoControl(fechaFactura, dosificacion,
					0, nitCi);
			System.out.println("Codigo Control: " + CC);

		}
		return CC;
	}



	private boolean puedoFacturarFechaLimite(Date fechaLimiteEmision,Date fechaActual) {
		try {
			String DATE_FORMAT = "yyyyMMdd";
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			String cadenaFechaLimiteEmision = sdf.format(fechaLimiteEmision);
			String cadenaFechaActual = sdf.format(fechaActual);

			if (Integer.valueOf(cadenaFechaLimiteEmision) >= Integer
					.valueOf(cadenaFechaActual)) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			log.error("Se Produjo un Error puedoFacturarFechaLimite:  "
					+ e.getMessage());
		}
		return false;
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

	private int obtnerNumeroComprobante(Date fechaComprobante,Sucursal selectedSucursal, TipoComprobante selectedTipoComprobante){
		return comprobanteRepository.obtenerNumeroComprobante(fechaComprobante,empresaLogin, selectedSucursal,selectedTipoComprobante);
	}

	// ---- get and set ---

	public String[] getArrayMes() {
		return arrayMes;
	}

	public void setArrayMes(String[] arrayMes) {
		this.arrayMes = arrayMes;
	}

	public boolean isSeleccionado() {
		return seleccionado;
	}

	public void setSeleccionado(boolean seleccionado) {
		this.seleccionado = seleccionado;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getNombreMes() {
		return nombreMes;
	}

	public void setNombreMes(String nombreMes) {
		this.nombreMes = nombreMes;
	}

	public Cotizacion getSelectedCotizacion() {
		return selectedCotizacion;
	}

	public void setSelectedCotizacion(Cotizacion selectedCotizacion) {
		this.selectedCotizacion = selectedCotizacion;
	}

	public List<Cotizacion> getListCotizacion() {
		return listCotizacion;
	}

	public void setListCotizacion(List<Cotizacion> listCotizacion) {
		this.listCotizacion = listCotizacion;
	}

	public Integer getNumeroCotizacion() {
		return numeroCotizacion;
	}

	public void setNumeroCotizacion(Integer numeroCotizacion) {
		this.numeroCotizacion = numeroCotizacion;
	}

	public String getUrlCotizacion() {
		return urlCotizacion;
	}

	public void setUrlCotizacion(String urlCotizacion) {
		this.urlCotizacion = urlCotizacion;
	}

	public boolean isCrear() {
		return crear;
	}

	public void setCrear(boolean crear) {
		this.crear = crear;
	}

	public boolean isSeleccionarCotizacion() {
		return seleccionarCotizacion;
	}

	public void setSeleccionarCotizacion(boolean seleccionarCotizacion) {
		this.seleccionarCotizacion = seleccionarCotizacion;
	}

	public List<Cotizacion> getListFilterCotizacion() {
		return listFilterCotizacion;
	}

	public void setListFilterCotizacion(List<Cotizacion> listFilterCotizacion) {
		this.listFilterCotizacion = listFilterCotizacion;
	}
}
