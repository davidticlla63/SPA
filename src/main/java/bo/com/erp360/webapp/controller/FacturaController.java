package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import org.apache.log4j.Logger;
import org.primefaces.component.api.UIData;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.ClienteRepository;
import bo.com.erp360.webapp.data.DosificacionRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.FacturaRepository;
import bo.com.erp360.webapp.data.FormatoHojaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.data.NitClienteRepository;
import bo.com.erp360.webapp.data.ProductoRepository;
import bo.com.erp360.webapp.data.ServicioRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.TamanoHojaRepository;
import bo.com.erp360.webapp.data.TipoComprobanteRepository;
import bo.com.erp360.webapp.data.TipoProductoRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.DetalleFactura;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.FormatoHoja;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.NitCliente;
import bo.com.erp360.webapp.model.PlanCuenta;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.Servicio;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TamanoHoja;
import bo.com.erp360.webapp.model.TipoProducto;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.ClienteRegistration;
import bo.com.erp360.webapp.service.DetalleFacturaRegistration;
import bo.com.erp360.webapp.service.DosificacionRegistration;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.FacturaRegistration;
import bo.com.erp360.webapp.service.NitClienteRegistration;
import bo.com.erp360.webapp.service.ProductoRegistration;
import bo.com.erp360.webapp.util.CodigoControl7;
import bo.com.erp360.webapp.util.NumerosToLetras;
import bo.com.erp360.webapp.util.Time;
import bo.com.erp360.webapp.util.UtilidadesFacturacion;

@Named(value = "facturaController")
@ConversationScoped
public class FacturaController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7576600832060441894L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	private @Inject TamanoHojaRepository tamanoHojaRepository;

	@Inject
	Conversation conversation;

	@Inject
	private FacturaRepository facturaRepository;

	@Inject
	private FacturaRegistration facturaRegistration;

	private @Inject DosificacionRegistration dosificacionRegistration;

	@Inject
	private GestionRepository gestionRepository;

	@Inject
	private EmpresaRepository empresaRepository;

	@Inject
	private ProductoRepository productoRepository;

	@Inject
	private ClienteRepository clientesRepository;

	@Inject
	private ClienteRegistration clienteRegistration;

	@Inject
	private NitClienteRegistration nitClienteRegistration;

	private @Inject NitClienteRepository nitClienteRepository;

	@Inject
	private ProductoRegistration productoRegistration;

	@Inject
	private MonedaRepository monedaRepository;

	@Inject
	private TipoComprobanteRepository tipoComprobanteRepository;

	@Inject
	private DosificacionRepository dosificacionRepository;

	Logger log = Logger.getLogger(FacturaController.class);

	private String url;

	private @Inject FormatoHojaRepository formatoHojaRepository;
	private FormatoHoja formatoHoja;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventFactura;

	@Produces
	@Named
	private Factura newFactura;
	private Dosificacion dosificacion;

	private double efectivoBolivianos = 0;
	private double efectivoDolares = 0;
	private double totalCambio = 0;
	private double totalEfectivo = 0;

	private Producto selectedProducto;
	private Producto newProducto;
	private Servicio newServicio;
	private Cliente busquedaCliente;
	private NitCliente nitCliente;
	private PlanCuenta busquedaCuenta;
	private CentroCosto busquedaCentroCosto;
	private Cliente newClientes;
	private Producto busquedaProducto;

	private Servicio busquedaServicio;

	private MonedaEmpresa monedaEmpresa;

	private String[] listEstado = { "ACTIVO", "INACTIVO" };

	// tipo de Producto
	private List<TipoProducto> listTipoProducto = new ArrayList<TipoProducto>();
	private @Inject TipoProductoRepository tipoProductoRepository;

	private List<Usuario> listUsuario = new ArrayList<Usuario>();
	private List<Producto> selectedListProducto = new ArrayList<Producto>();
	private List<Cliente> listCliente = new ArrayList<Cliente>();
	private List<NitCliente> listNitCliente = new ArrayList<NitCliente>();
	/* private List<Factura> listFactura = new ArrayList<Factura>(); */

	private List<DetalleFactura> listDetalleFactura = new ArrayList<DetalleFactura>();
	private List<Producto> listProducto = new ArrayList<Producto>();
	private List<Servicio> listServicio = new ArrayList<Servicio>();
	private @Inject ServicioRepository servicioRepository;
	private List<MonedaEmpresa> listMonedaEmpresa;
	private List<CentroCosto> listCentroCosto = new ArrayList<CentroCosto>();
	private List<PlanCuenta> listCuentasAuxiliares = new ArrayList<PlanCuenta>();

	private String tituloPanel = "Registrar Factura";
	private String nombreCliente;
	private String textoAutoCompleteCliente;
	private String textoAutoCompleteNit;
	private String textoAutoCompleteCuenta;
	private String textoAutoCompleteCentroCosto;
	private String nombreEstado = "ACTIVO";
	private int selectedIdEDFactura;
	private double numeroFactura;
	private String nombreMonedaEmpresa;

	private String productoServicio = "producto";

	// login
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private String nombreUsuario;
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	// estados
	private boolean modificar = false;
	private boolean selectedImporte;
	private boolean agregarProducto = false;
	private boolean permitirCredito = true;
	private boolean error = false;

	// autoComplete
	private String texto;
	private double cantidad = 1;
	private double totalImportePorProducto;
	private double descuento;
	private double totalImporte;

	private String textoServicio;

	private @Inject UsuarioRepository usuarioRepository;
	private Usuario usuarioLogin;

	private UIData usersDataTable;

	private @Inject DetalleFacturaRegistration detalleFacturaRegistery;

	private boolean puedofacturar = true;

	private double diasrestantes = 0;

	private Sucursal sucursalLogin;

	private @Inject SucursalRepository sucursalRepository;
	
	private @Inject TipoCambioController tipoCambioController;

	@PostConstruct
	public void initNewFactura() {

		log.info(" init new initNewFactura");
		beginConversation();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		nombreUsuario = estadoUsuarioLogin.getNombreUsuarioSession();
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);
		gestionLogin = estadoUsuarioLogin.getGestionSession(empresaRepository,
				gestionRepository);

		listMonedaEmpresa = monedaRepository
				.findMonedaEmpresaAllByEmpresa(empresaLogin);
		monedaEmpresa = listMonedaEmpresa.get(0);

		sucursalLogin = estadoUsuarioLogin.getSucursalSession(
				empresaRepository, sucursalRepository);

		usuarioLogin = estadoUsuarioLogin.getUsuarioSession(usuarioRepository);

		nombreMonedaEmpresa = monedaEmpresa.getMoneda().getNombre();

		loadValuesDefaul();
	}

	private void loadValuesDefaul() {
		newClientes = new Cliente();
		busquedaProducto = new Producto();
		busquedaServicio = new Servicio();
		busquedaCuenta = new PlanCuenta();
		busquedaCentroCosto = new CentroCosto();

		nitCliente = new NitCliente();
		listDetalleFactura = new ArrayList<DetalleFactura>();

		newProducto = new Producto();
		newFactura = new Factura();
		newFactura.setFechaFactura(new Date());
		newFactura.setSucursal(sucursalLogin);
		newFactura.setEmpresa(empresaLogin);
		dosificacion = dosificacionRepository.findActivaBySucursal(newFactura
				.getSucursal());

		diasrestantes = Math.floor((dosificacion.getFechaLimiteEmision()
				.getTime() - (new Date()).getTime()) / (3600 * 24 * 1000));
		System.out.println("DIAS : " + diasrestantes);

//		setPuedofacturar(dosificacion.getFechaLimiteEmision().getTime() >= (new Date())
//				.getTime());
		setPuedofacturar(diasrestantes>=-1);

		newFactura.setNumeroAutorizacion(dosificacion.getNumeroAutorizacion());

		/*
		 * listFactura = facturaRepository.findAllActivas(nombreUsuario,
		 * empresaLogin);
		 */

		numeroFactura = dosificacion.getNumeroSecuencia();
		totalImporte = 0;

		textoAutoCompleteCliente = "";
		textoAutoCompleteCuenta = "";
		textoAutoCompleteCentroCosto = "";
		textoAutoCompleteNit = "";
		textoServicio = "";

		listCliente = clientesRepository.findActivosByEmpresa(empresaLogin);
		nombreCliente = listCliente.size() > 0 ? listCliente.get(0).getNombre()
				: "";
		listProducto = productoRepository.findAllActivas(empresaLogin);

		listServicio = servicioRepository.findAllActivosByEmpresa(empresaLogin);
		// tipo de producto
		listTipoProducto = tipoProductoRepository
				.findAllActivasByEmpresa(empresaLogin);

		// tituloPanel
		tituloPanel = "Comprobante";
		modificar = false;
	}

	public List<Producto> completeText(String query) {
		String upperQuery = query.toUpperCase();
		List<Producto> results = new ArrayList<Producto>();
		for (Producto i : listProducto) {
			if ((i.getNombre().toUpperCase().startsWith(upperQuery))
					&& !(i.getEstado().equals("RM"))) {
				results.add(i);
			}
		}
		return results;
	}

	public void onItemSelect(SelectEvent event) {
		String nombre = event.getObject().toString();
		for (Producto s : listProducto) {
			if (s.getNombre().equals(nombre)) {
				busquedaProducto = s;
				totalImportePorProducto = s.getPrecioVenta();
			}
		}
	}

	/**
	 * servicio
	 * 
	 * @param query
	 * @return
	 */

	public List<Servicio> completeTextServicio(String query) {
		String upperQuery = query.toUpperCase();

		List<Servicio> results = new ArrayList<Servicio>();
		for (Servicio i : listServicio) {
			if ((i.getNombre().toUpperCase().startsWith(upperQuery))
					&& !(i.getEstado().equals("RM"))) {
				results.add(i);
			}
		}
		return results;
	}

	public void onItemSelectServicio(SelectEvent event) {
		String nombre = event.getObject().toString();
		for (Servicio s : listServicio) {
			if (s.getNombre().equals(nombre)) {
				busquedaServicio = s;
				totalImportePorProducto = s.getPrecioReferencial();
			}
		}
	}

	public void modificarDetalleProducto() {
		totalImportePorProducto = ((busquedaProducto.getPrecioVenta() - (busquedaProducto
				.getPrecioVenta() * descuento) / 100) * cantidad);
	}

	public void modificarDetalleServicio() {
		totalImportePorProducto = ((busquedaServicio.getPrecioReferencial() - (busquedaServicio
				.getPrecioReferencial() * descuento) / 100) * cantidad);
	}

	public int getGestionSession() {
		try {
			HttpSession request1 = (HttpSession) FacesContext
					.getCurrentInstance().getExternalContext()
					.getSession(false);
			return Integer
					.parseInt(request1.getAttribute("gestion").toString());
		} catch (Exception e) {
			log.info("getEmpresaSession() -> error : " + e.getMessage());
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

	public void registrarEImprimir() {

		if (verificarNitCi() && listDetalleFactura.size() > 0) {
			registrarFactura();
			if (!error) {
				RequestContext context = RequestContext.getCurrentInstance();
				context.execute("PF('dlgFacturaVistaPrevia').show();");
			}
		} else {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"A ocurrido un error!", " Revisar Datos del Cliente o Nit");
			facesContext.addMessage(null, m);
		}

	}

	public void dialogClose() {
		System.out.println("Ingreso a dialogClose ");
		if (newFactura.isImpresion() && !newFactura.getEstado().equals("A")) {
			newFactura.setImpresion(false);
			facturaRegistration.update(newFactura);
		}
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgFacturaVistaPrevia').hide();");
		loadValuesDefaul();

	}

	public void registrarFactura() {
		try {

			log.info("Ingreso a registrarFactura...");
			// ----datos para la factrua
			dosificacion = dosificacionRepository
					.findActivaBySucursal(newFactura.getSucursal());
			numeroFactura = dosificacion.getNumeroSecuencia();
			// ---------------------------registro de
			CodigoControl7 control = new CodigoControl7();
			String CC = control.obtenerCodigoControl(
					newFactura.getFechaFactura(), dosificacion,
					newFactura.getTotalPagar(), nitCliente.getNit());
			System.out.println("Codigo de Control : " + CC);
			if (CC.length() == 14 || CC.length() == 11) {
				newFactura.setTipoCambio(tipoCambioController.obtenerTipoCambioActual().getUnidad());
				newFactura.setTotalEfectivo(0);
				newFactura.setCambio(0);
				newFactura.setEstado("V");
				
				newFactura.setCodigoControl(CC);// Codigo de Control
				newFactura.setCliente(busquedaCliente);
				newFactura.setUsuarioRegistro(nombreUsuario);
				newFactura.setTotalFacturado(newFactura.getTotalPagar()); // Total
																			// Bs
				newFactura.setEmpresa(empresaLogin);
				newFactura.setTipoPago("EFECTIVO");

				newFactura = UtilidadesFacturacion.calcularDatosFacturacion(
						newFactura, dosificacion, nitCliente);
				System.out.println("calculado datos ");
				newFactura=facturaRegistration.create(newFactura);
				System.out.println("registrado..");
				dosificacion.setNumeroSecuencia(dosificacion
						.getNumeroSecuencia() + 1);

				dosificacionRegistration.update(dosificacion);
				System.out.println("actulizado dosificacion ..");
				for (DetalleFactura detalleFactura : listDetalleFactura) {
					detalleFactura.setFactura(newFactura);
					detalleFactura.setFechaRegistro(new Date());
					detalleFactura.setUsuarioRegistro(nombreUsuario);
					detalleFacturaRegistery.create(detalleFactura);
				}
				System.out.println("detalle..");
				armarUrl();

				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Factura Guardada!", listDetalleFactura.size()
								+ " productos");
				facesContext.addMessage(null, m);

				/* loadValuesDefaul(); */
			}
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Guardado Incorrecto.");
			facesContext.addMessage(null, m);
			log.error(errorMessage);
		}
	}

	public void modificarFactura() {
		try {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Factura Modificada!", nombreUsuario);
			facesContext.addMessage(null, m);
			loadValuesDefaul();
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Modificado Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	// Calcula el cambio al cliente por el pago realizado
	public void calcularCambioDePago() {
		totalEfectivo = efectivoBolivianos - (efectivoDolares * 0.96);
		totalCambio = totalEfectivo - newFactura.getTotalFacturado();

	}

	

	public void onRowEdit() {
		log.info("Ingreso a onRowEdit");
	}

	public void agregarProducto() {
		try {
			log.info("Ingreso a agregarProducto");

			if (productoServicio.equals("producto")
					&& (cantidad == 0 || texto.trim().length() == 0)) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Revisar la Cantidad o el Producto ",
						"por favor llenar.");
				facesContext.addMessage(null, m);
				log.error("Revisar la Cantidad o el Producto");
				return;
			}

			DetalleFactura detalleFactura = new DetalleFactura();
			detalleFactura.setCantidad(cantidad);
			detalleFactura.setConcepto(texto);
			detalleFactura.setPrecioUnitario(busquedaProducto.getPrecioVenta());
			detalleFactura.setPrecioTotal(totalImportePorProducto);
			detalleFactura.setCodigoProducto("P000" + busquedaProducto.getId());
			listDetalleFactura.add(detalleFactura);
			calcularImporteTotal();
			clearFieldProducto();
		} catch (Exception e) {
			log.error("Error en agregarProducto : " + e.getStackTrace());
		}
	}

	public void agregarServicio() {
		try {
			log.info("Ingreso a agregarProducto");
			if (productoServicio.equals("servicio")
					&& (cantidad == 0 || textoServicio.trim().length() == 0)) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Revisar la Cantidad o el Servicio ",
						"por favor llenar.");
				facesContext.addMessage(null, m);
				log.error("Revisar la Cantidad o el Producto");
				return;
			}
			DetalleFactura detalleFactura = new DetalleFactura();
			detalleFactura.setCantidad(cantidad);
			detalleFactura.setConcepto(textoServicio);
			detalleFactura.setPrecioUnitario(busquedaServicio
					.getPrecioReferencial());
			detalleFactura.setPrecioTotal(totalImportePorProducto);
			detalleFactura.setCodigoProducto("S000" + busquedaServicio.getId());
			listDetalleFactura.add(detalleFactura);
			calcularImporteTotal();
			clearFieldServicio();
		} catch (Exception e) {
			log.error("Error en agregarProducto : " + e.getStackTrace());
		}
	}

	private void clearFieldProducto() {
		cantidad = 0;
		texto = "";
		totalImportePorProducto = 0;
		busquedaProducto = new Producto();
	}

	private void clearFieldServicio() {
		cantidad = 0;
		texto = "";
		totalImportePorProducto = 0;
		busquedaServicio = new Servicio();
	}

	private void calcularImporteTotal() {
		totalImporte = 0;
		for (DetalleFactura detalleFactura : listDetalleFactura) {
			totalImporte += detalleFactura.getPrecioTotal();
		}
		newFactura.setTotalFacturado(totalImporte);
		newFactura.setTotalPagar(totalImporte);
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

	public void onRowSelectProducto(SelectEvent event) {
		log.info("onNodeSelectProducto");
		// selectedProducto = (Producto) event.getObject();
	}

	public void resetDatosProducto() {
		totalImportePorProducto = 0;
		cantidad = 1;
		descuento = 0;
		busquedaProducto = new Producto();
		texto = "";
	}

	public void buttonAgregarProducto() {
		newProducto = new Producto();
		agregarProducto = true;
	}

	public void buttonAgregarServicio() {
		setNewServicio(new Servicio());
		agregarProducto = true;
	}

	public void registrarNuevoProducto() {
		try {
			newProducto.setEstado("AC");
			newProducto.setUsuarioRegistro(nombreUsuario);
			newProducto.setFechaRegistro(new Date());
			newProducto.setEmpresa(empresaLogin);
			newProducto = productoRegistration.create(newProducto);

			agregarProducto = false;
			texto = newProducto.getNombre();
			busquedaProducto = newProducto;
			totalImportePorProducto = newProducto.getPrecioVenta();
			listProducto.add(newProducto);
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void registrarCliente() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			String credito = permitirCredito ? "SI" : "NO";

			/*
			 * if (!nitClienteRepository.ExistNit(nitCliente.getNit())) {
			 * 
			 * }
			 */

			newClientes.setPermitirCredito(credito);
			newClientes.setEstado(estado);
			newClientes.setFechaRegistro(new Date());
			newClientes.setUsuarioRegistro(nombreUsuario);
			newClientes.setEmpresa(empresaLogin);
			clienteRegistration.create(newClientes);

			NitCliente nit = new NitCliente();
			nit.setCliente(newClientes);
			nit.setNit(newClientes.getNit());
			nit.setUsuarioRegistro(nombreUsuario);
			nit.setFechaRegistro(new Date());
			nitClienteRegistration.create(nit);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Cliente Registrado!", newClientes.getNombre());
			facesContext.addMessage(null, m);
			textoAutoCompleteCliente = newClientes.getNombre();
			textoAutoCompleteNit = nit.getNit();
			busquedaCliente = newClientes;
			nitCliente = nit;
			listCliente.add(newClientes);
			newClientes = new Cliente();
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Registro Incorrecto.");
			facesContext.addMessage(null, m);
		}
	}

	public void cancelarAgregarProducto() {
		busquedaProducto = new Producto();
		texto = "";
		totalImportePorProducto = 0;
		cantidad = 1;
		descuento = 0;
	}

	public List<String> completeTextCliente(String query) {
		List<String> results = new ArrayList<String>();
		listNitCliente = nitClienteRepository
				.findNitClienteAllActivasByNombreCliente(query.toUpperCase());
		for (NitCliente i : listNitCliente) {
			results.add(i.getCliente() + "," + i.getNit());
		}
		return results;
	}

	public void onItemSelectCliente(SelectEvent event) {
		String nombre = event.getObject().toString();
		String string = nombre.split(",")[1];
		for (NitCliente s : listNitCliente) {
			if (s.getNit().equals(string)) {
				busquedaCliente = s.getCliente();
				nitCliente = s;
				textoAutoCompleteNit = nitCliente.getNit();
				log.info("cliente encontrado .." + nombre);
			}
		}
		textoAutoCompleteCliente = nitCliente.getCliente().getNombre();
	}

	public List<String> completeTextNit(String query) {
		List<String> results = new ArrayList<String>();
		listNitCliente = nitClienteRepository.findClienteAllByNit(query
				.toUpperCase());
		System.out.println("size : " + listNitCliente.size());
		for (NitCliente i : listNitCliente) {
			results.add(i.getNit() + ":" + i.getCliente().getNombre());
		}
		return results;
	}

	public void onItemSelectNit(SelectEvent event) {
		String nits = event.getObject().toString();
		log.info("nit : " + nits);
		String ni = nits.split(":")[0];
		log.info("nit 2 : " + ni);
		String nombre = nits.split(":")[1];
		log.info("nombre : " + nombre);
		for (NitCliente nit : listNitCliente) {
			if (nit.getNit().equals(ni)
					&& nit.getCliente().getNombre().equals(nombre)) {
				log.info("Nit encontrado .." + ni);
				busquedaCliente = nit.getCliente();
				nitCliente = nit;
				textoAutoCompleteCliente = nitCliente.getCliente().getNombre();
				textoAutoCompleteNit = ni;
				return;
			}
		}
		textoAutoCompleteNit = ni;

	}

	public boolean verificarNitCi() {
		try {
			log.info("Entro a verificarNitCi : " + textoAutoCompleteNit + " - "
					+ textoAutoCompleteCliente);
			if (textoAutoCompleteNit.trim().length() == 0
					|| textoAutoCompleteCliente.trim().length() == 0) {
				return false;
			}
			if (clientesRepository.ExistCliente(textoAutoCompleteCliente)) {
				busquedaCliente = clientesRepository
						.findByNombre(textoAutoCompleteCliente);
				if (nitClienteRepository.ExistNit(busquedaCliente,
						textoAutoCompleteNit.trim())) {
					nitCliente = nitClienteRepository.findNitClienteNit(
							busquedaCliente, textoAutoCompleteNit.trim());
					return true;
				} else {
					nitCliente = new NitCliente();
					nitCliente.setCliente(busquedaCliente);
					nitCliente.setNit(textoAutoCompleteNit);
					nitCliente.setUsuarioRegistro(nombreUsuario);
					nitCliente = nitClienteRegistration.create(nitCliente);
					log.info("Nit registrado...." + nitCliente.getNit());
					return true;
				}
			} else {
				busquedaCliente = new Cliente();
				busquedaCliente.setRazonSocial(textoAutoCompleteCliente);
				busquedaCliente.setNombre(textoAutoCompleteCliente);
				busquedaCliente.setCi(textoAutoCompleteNit);
				busquedaCliente.setEmpresa(empresaLogin);
				busquedaCliente.setUsuarioRegistro(nombreUsuario);
				busquedaCliente = clienteRegistration.create(busquedaCliente);
				log.info("Cliente registrado...." + busquedaCliente.getNombre());

				nitCliente = new NitCliente();
				nitCliente.setCliente(busquedaCliente);
				nitCliente.setNit(textoAutoCompleteNit);
				nitCliente.setUsuarioRegistro(nombreUsuario);
				nitCliente = nitClienteRegistration.create(nitCliente);
				log.info("Nit registrado...." + nitCliente.getNit());
				return true;

			}

		} catch (Exception e) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"A ocurrido un error!", " Revisar Datos del Cliente o Nit "
							+ e.getStackTrace());
			facesContext.addMessage(null, m);
		}
		return false;
	}

	private void armarUrl() {
		try {
			HttpServletRequest request = (HttpServletRequest) facesContext
					.getExternalContext().getRequest();
			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";

			TamanoHoja tamanoHoja = tamanoHojaRepository.traerHojaActiva();
			String tamano = tamanoHoja.getTamano();

			String urlLogo = urlPath + "resources/gfx/"
					+ sucursalLogin.getPathLogo();
			formatoHoja = formatoHojaRepository.findActivosByEmpresa(
					empresaLogin, sucursalLogin).get(0);
			if (formatoHoja.getNombre().equals("COMPLETO")) {
				url = urlPath
						+ "ReportFactura?"
						+UtilidadesFacturacion. urlFacturaServlet(newFactura, dosificacion, urlLogo, tamano);
			}
			if (formatoHoja.getNombre().equals("SIN LOGO")) {
				url = urlPath
						+ "ReportFacturaSinCredFiscal?"
						+UtilidadesFacturacion. urlFacturaServlet(newFactura, dosificacion, urlLogo, tamano);
			}
			if (formatoHoja.getNombre().equals("SIN LOGO, SIN BORDE")) {
				url = urlPath
						+ "ReportFacturaSinCredFiscal?"+UtilidadesFacturacion. urlFacturaServlet(newFactura, dosificacion, urlLogo, tamano);
			}

			log.info("getURL() -> " + url);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void onItemSelectCuenta(SelectEvent event) {
		String nombre = event.getObject().toString();
		for (PlanCuenta s : listCuentasAuxiliares) {
			if (s.getDescripcion().equals(nombre)) {
				setBusquedaCuenta(s);
			}
		}
	}

	public void onItemSelectCentroCosto(SelectEvent event) {
		String nombre = event.getObject().toString();
		for (CentroCosto s : listCentroCosto) {
			if (s.getNombre().equals(nombre)) {
				setBusquedaCentroCosto(s);
			}
		}
	}

	private MonedaEmpresa buscarMonedaEmpresaByLocal(String nombreMonedaEmpresa) {
		for (MonedaEmpresa me : listMonedaEmpresa) {
			if (nombreMonedaEmpresa.equals(me.getMoneda().getNombre())) {
				return me;
			}
		}
		return null;
	}

	// -------------------- get and set -------------------------
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

	public void cambiarModificar() {
		setModificar(false);
	}

	public List<Usuario> getListUsuario() {
		return listUsuario;
	}

	public void setListUsuario(List<Usuario> listUsuario) {
		this.listUsuario = listUsuario;
	}

	public int getSelectedIdEDFactura() {
		return selectedIdEDFactura;
	}

	public void setSelectedIdEDFactura(int selectedIdEDFactura) {
		this.selectedIdEDFactura = selectedIdEDFactura;
	}

	public double getTotalImporte() {
		return totalImporte;
	}

	public void setTotalImporte(double totalImporte) {
		this.totalImporte = totalImporte;
	}

	public Producto getSelectedProducto() {
		return selectedProducto;
	}

	public void setSelectedProducto(Producto selectedProducto) {
		this.selectedProducto = selectedProducto;
	}

	public List<Producto> getSelectedListProducto() {
		return selectedListProducto;
	}

	public void setSelectedListProducto(List<Producto> selectedListProducto) {
		this.selectedListProducto = selectedListProducto;
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

	public List<Producto> getListProducto() {
		return listProducto;
	}

	public void setListProducto(List<Producto> listProducto) {
		this.listProducto = listProducto;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public Producto getBusquedaProducto() {
		return busquedaProducto;
	}

	public void setBusquedaProducto(Producto busquedaProducto) {
		this.busquedaProducto = busquedaProducto;
	}

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}

	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}

	public double getTotalImportePorProducto() {
		return totalImportePorProducto;
	}

	public void setTotalImportePorProducto(double totalImportePorProducto) {
		this.totalImportePorProducto = totalImportePorProducto;
	}

	public UIData getUsersDataTable() {
		return usersDataTable;
	}

	public void setUsersDataTable(UIData usersDataTable) {
		this.usersDataTable = usersDataTable;
	}

	public boolean isAgregarProducto() {
		return agregarProducto;
	}

	public void setAgregarProducto(boolean agregarProducto) {
		this.agregarProducto = agregarProducto;
	}

	public Producto getNewProducto() {
		return newProducto;
	}

	public void setNewProducto(Producto newProducto) {
		this.newProducto = newProducto;
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

	public double getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(double numeroFactura) {
		this.numeroFactura = numeroFactura;
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

	/*
	 * public List<Factura> getListFactura() { return listFactura; }
	 * 
	 * public void setListFactura(List<Factura> listFactura) { this.listFactura
	 * = listFactura; }
	 */

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

	public List<DetalleFactura> getListDetalleFactura() {
		return listDetalleFactura;
	}

	public void setListDetalleFactura(List<DetalleFactura> listDetalleFactura) {
		this.listDetalleFactura = listDetalleFactura;
	}

	public String getTextoAutoCompleteNit() {
		return textoAutoCompleteNit;
	}

	public void setTextoAutoCompleteNit(String textoAutoCompleteNit) {
		this.textoAutoCompleteNit = textoAutoCompleteNit;
	}

	public Usuario getUsuarioLogin() {
		return usuarioLogin;
	}

	public void setUsuarioLogin(Usuario usuarioLogin) {
		this.usuarioLogin = usuarioLogin;
	}

	public String[] getListEstado() {
		return listEstado;
	}

	public void setListEstado(String[] listEstado) {
		this.listEstado = listEstado;
	}

	public List<TipoProducto> getListTipoProducto() {
		return listTipoProducto;
	}

	public void setListTipoProducto(List<TipoProducto> listTipoProducto) {
		this.listTipoProducto = listTipoProducto;
	}

	public NitCliente getNitCliente() {
		return nitCliente;
	}

	public void setNitCliente(NitCliente nitCliente) {
		this.nitCliente = nitCliente;
	}

	public List<NitCliente> getListNitCliente() {
		return listNitCliente;
	}

	public void setListNitCliente(List<NitCliente> listNitCliente) {
		this.listNitCliente = listNitCliente;
	}

	public Dosificacion getDosificacion() {
		return dosificacion;
	}

	public void setDosificacion(Dosificacion dosificacion) {
		this.dosificacion = dosificacion;
	}

	public double getEfectivoBolivianos() {
		return efectivoBolivianos;
	}

	public void setEfectivoBolivianos(double efectivoBolivianos) {
		this.efectivoBolivianos = efectivoBolivianos;
	}

	public double getEfectivoDolares() {
		return efectivoDolares;
	}

	public void setEfectivoDolares(double efectivoDolares) {
		this.efectivoDolares = efectivoDolares;
	}

	public double getTotalCambio() {
		return totalCambio;
	}

	public void setTotalCambio(double totalCambio) {
		this.totalCambio = totalCambio;
	}

	public double getTotalEfectivo() {
		return totalEfectivo;
	}

	public void setTotalEfectivo(double totalEfectivo) {
		this.totalEfectivo = totalEfectivo;
	}

	public String getProductoServicio() {
		return productoServicio;
	}

	public void setProductoServicio(String productoServicio) {
		this.productoServicio = productoServicio;
	}

	public Servicio getBusquedaServicio() {
		return busquedaServicio;
	}

	public void setBusquedaServicio(Servicio busquedaServicio) {
		this.busquedaServicio = busquedaServicio;
	}

	public Servicio getNewServicio() {
		return newServicio;
	}

	public void setNewServicio(Servicio newServicio) {
		this.newServicio = newServicio;
	}

	public List<Servicio> getListServicio() {
		return listServicio;
	}

	public void setListServicio(List<Servicio> listServicio) {
		this.listServicio = listServicio;
	}

	public String getTextoServicio() {
		return textoServicio;
	}

	public void setTextoServicio(String textoServicio) {
		this.textoServicio = textoServicio;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isPuedofacturar() {
		return puedofacturar;
	}

	public void setPuedofacturar(boolean puedofacturar) {
		this.puedofacturar = puedofacturar;
	}

	public double getDiasrestantes() {
		return diasrestantes;
	}

	public void setDiasrestantes(double diasrestantes) {
		this.diasrestantes = diasrestantes;
	}

}
