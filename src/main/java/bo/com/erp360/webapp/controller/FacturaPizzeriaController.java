package bo.com.erp360.webapp.controller;

import java.io.IOException;
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

import bo.com.erp360.webapp.data.CentroCostoRepository;
import bo.com.erp360.webapp.data.ClienteRepository;
import bo.com.erp360.webapp.data.DosificacionRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.FormatoHojaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.MonedaRepository;
import bo.com.erp360.webapp.data.NitClienteRepository;
import bo.com.erp360.webapp.data.ProductoRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.TamanoHojaRepository;
import bo.com.erp360.webapp.data.TipoCambioRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.DetalleFactura;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.FormatoHoja;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.MonedaEmpresa;
import bo.com.erp360.webapp.model.NitCliente;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TamanoHoja;
import bo.com.erp360.webapp.model.TipoCambio;
import bo.com.erp360.webapp.model.TipoProducto;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.ClienteRegistration;
import bo.com.erp360.webapp.service.DetalleFacturaRegistration;
import bo.com.erp360.webapp.service.DosificacionRegistration;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.FacturaRegistration;
import bo.com.erp360.webapp.service.NitClienteRegistration;
import bo.com.erp360.webapp.util.CodigoControl7;
import bo.com.erp360.webapp.util.EDPaginas;
import bo.com.erp360.webapp.util.EDProducto;
import bo.com.erp360.webapp.util.EDProductoProducto;
import bo.com.erp360.webapp.util.EDProductos;
import bo.com.erp360.webapp.util.NumerosToLetras;
import bo.com.erp360.webapp.util.Time;

@Named(value = "facturaPizzeriaController")
@ConversationScoped
public class FacturaPizzeriaController implements Serializable {

	/**
	 * 
	 */

	private static final long serialVersionUID = 711919976233613887L;

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	private @Inject TamanoHojaRepository tamanoHojaRepository;

	@Inject
	Conversation conversation;

	@Inject
	private FacturaRegistration facturaRegistration;

	private @Inject DosificacionRegistration dosificacionRegistration;

	@Inject
	private GestionRepository gestionRepository;

	@Inject
	private CentroCostoRepository centroCostoRepository;

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
	private MonedaRepository monedaRepository;

	@Inject
	private DosificacionRepository dosificacionRepository;

	Logger log = Logger.getLogger(FacturaPizzeriaController.class);

	private String url;

	private @Inject FormatoHojaRepository formatoHojaRepository;
	private FormatoHoja formatoHoja;

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventFactura;

	private Factura newFactura;
	private Dosificacion dosificacion;

	private double efectivoBolivianos = 0;
	private double efectivoDolares = 0;
	private double totalCambio = 0;
	private double totalEfectivo = 0;

	private Cliente busquedaCliente;
	private NitCliente nitCliente;
	private Cliente newClientes;

	private MonedaEmpresa monedaEmpresa;

	private String[] listEstado = { "ACTIVO", "INACTIVO" };

	private List<Usuario> listUsuario = new ArrayList<Usuario>();
	private List<Producto> selectedListProducto = new ArrayList<Producto>();
	private List<Cliente> listCliente = new ArrayList<Cliente>();
	private List<NitCliente> listNitCliente = new ArrayList<NitCliente>();

	private List<DetalleFactura> listDetalleFactura = new ArrayList<DetalleFactura>();

	private List<MonedaEmpresa> listMonedaEmpresa;

	private List<EDProductoProducto> listEdProductos = new ArrayList<EDProductoProducto>();

	private List<EDProductos> listEdProducto = new ArrayList<EDProductos>();

	private List<TipoProducto> listTipoProductos = new ArrayList<TipoProducto>();

	private String tituloPanel = "Registrar Factura";
	private String nombreCliente;
	private String textoAutoCompleteCliente;
	private String textoAutoCompleteNit;
	private String nombreEstado = "ACTIVO";
	private int selectedIdEDFactura;
	private double numeroFactura;

	private List<EDPaginas> listEdPaginas = new ArrayList<EDPaginas>();

	// login
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private String nombreUsuario;
	private Empresa empresaLogin;
	private Gestion gestionLogin;

	// estados
	private boolean modificar = false;
	private boolean error = false;

	private boolean nuevo = true;

	private double totalImporte;

	private @Inject UsuarioRepository usuarioRepository;
	private Usuario usuarioLogin;

	private UIData usersDataTable;

	private @Inject DetalleFacturaRegistration detalleFacturaRegistery;

	private boolean puedofacturar = true;

	private double diasrestantes = 0;

	private Sucursal sucursalLogin;

	private boolean impresion = false;

	private @Inject SucursalRepository sucursalRepository;

	private String pagina;

	private int numpag = 0;

	private double tipocambio = 6.80;
	private List<TipoCambio> listTipoCambio;
	@Inject
	private TipoCambioRepository tipoCambioRepository;

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

		loadValuesDefaul();
	}

	private void loadValuesDefaul() {
		newClientes = new Cliente();
		nuevo = true;
		nitCliente = new NitCliente();
		listDetalleFactura = new ArrayList<DetalleFactura>();

		newFactura = new Factura();
		newFactura.setFechaFactura(new Date());
		newFactura.setSucursal(sucursalLogin);
		newFactura.setEmpresa(empresaLogin);
		dosificacion = dosificacionRepository.findActivaBySucursal(newFactura
				.getSucursal());

		listTipoCambio = tipoCambioRepository.findAllByEmpresa(empresaLogin);
		tipocambio = listTipoCambio.get(0).getUnidad();
		diasrestantes = Math.floor((dosificacion.getFechaLimiteEmision()
				.getTime() - (new Date()).getTime()) / (3600 * 24 * 1000));
		System.out.println("DIAS : " + diasrestantes);

		setPuedofacturar(dosificacion.getFechaLimiteEmision().getTime() >= (new Date())
				.getTime());

		newFactura.setNumeroAutorizacion(dosificacion.getNumeroAutorizacion());

		numeroFactura = dosificacion.getNumeroSecuencia();
		totalImporte = 0;

		textoAutoCompleteCliente = "";

		listCliente = clientesRepository.findActivosByEmpresa(empresaLogin);
		nombreCliente = listCliente.size() > 0 ? listCliente.get(0).getNombre()
				: "";

		// tituloPanel
		tituloPanel = "Comprobante";
		modificar = false;
		impresion = false;
		// llenarEDProducto();
		llenarListaProductos();
	}

	private void llenarListaProductos() {
		try {
			log.info("Ingreso a llenarEDProducto ");
			listTipoProductos = productoRepository
					.findAllActivasTipoProducto(empresaLogin);
			List<Producto> listProductos = productoRepository
					.findAllActivasForStructure(empresaLogin);
			if (listTipoProductos.size() > 0) {
				pagina = listTipoProductos.get(0).getNombre();
			}
			if (listProductos.size() == 0) {
				return;
			}
			listEdProducto.clear();
			for (Producto producto : listProductos) {
				listEdProducto.add(new EDProductos(producto, 0, false));
			}
		} catch (Exception e) {
			log.error("Error en llenarEDProducto " + e.getMessage());
		}

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

	public void hacerPedido2() {
		try {
			log.info("Ingreso a hacerPedido()");
			listDetalleFactura.clear();

			for (EDProductos edProducto : listEdProducto) {
				double precio = edProducto.getProducto().getPrecioVenta();
				String nombre = edProducto.getProducto().getNombre();
				if (edProducto.isMedia()) {
					precio = precio / 2;
					if (edProducto.getProducto().getGrupoProducto().getNombre()
							.equals("PIZZA")) {

						nombre = "PIZZETA - " + nombre;
					} else {
						if (edProducto.getProducto().getGrupoProducto()
								.getNombre().equals("RECARGO")) {
							nombre = "REGARGO DE - " + nombre;
						}
					}
					DetalleFactura detalleFactura = new DetalleFactura();
					detalleFactura.setCantidad(1);
					detalleFactura.setConcepto(nombre);
					detalleFactura.setPrecioUnitario(precio);
					detalleFactura.setPrecioTotal(1 * precio);
					detalleFactura.setCodigoProducto("P000"
							+ edProducto.getProducto().getId());
					listDetalleFactura.add(detalleFactura);
				}
				nombre = edProducto.getProducto().getNombre();
				precio = edProducto.getProducto().getPrecioVenta();
				if (edProducto.getCantidad() > 0) {

					if (edProducto.getProducto().getGrupoProducto().getNombre()
							.equals("PIZZA")) {

						nombre = "PIZZA - " + nombre;
					} else {
						if (edProducto.getProducto().getGrupoProducto()
								.getNombre().equals("RECARGO")) {
							nombre = "REGARGO DE - " + nombre;
						}
					}
					DetalleFactura detalleFactura = new DetalleFactura();
					detalleFactura.setCantidad(edProducto.getCantidad());
					detalleFactura.setConcepto(nombre);
					detalleFactura.setPrecioUnitario(precio);
					detalleFactura.setPrecioTotal(edProducto.getCantidad()
							* precio);
					detalleFactura.setCodigoProducto("P000"
							+ edProducto.getProducto().getId());
					listDetalleFactura.add(detalleFactura);
				}
			}
			if (listDetalleFactura.size() == 0) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"No ha elegido ningun Menu !!!",
						" Por favor elegir su Menu...");
				facesContext.addMessage(null, m);
				return;
			}
			calcularImporteTotal();			
		} catch (Exception e) {
			log.error("Error en hacerPedido() : " + e.getMessage());
		}
	}

	public void pedido() {
		log.info("Ingreso a pedido");
		nuevo = false;
		llenarListaProductos();
	}

	public void registrarEImprimir() {
		/*if (totalEfectivo < totalImporte) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"Por Favor llenar Pagos!", "Cancelar con efectivo");
			facesContext.addMessage(null, m);
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgProcesar').show();");
			return;
		}*/
		if (verificarNitCi()) {
			registrarFactura();
			if (!error) {
				impresion = true;
				/*
				 * RequestContext context = RequestContext.getCurrentInstance();
				 * context.execute("PF('dlgFacturaVistaPrevia').show();");
				 */
			}
		} else {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"A ocurrido un error!", " Revisar Datos del Cliente o Nit");
			facesContext.addMessage(null, m);
		}

	}

	public void dialogClose() {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgFacturaVistaPrevia').hide();");

		HttpServletRequest request = (HttpServletRequest) facesContext
				.getExternalContext().getRequest();
		String navigateString = request.getContextPath()
				+ "/pages/formulario/facturacion-pizza.xhtml";

		System.out.println(navigateString);
		try {
			facesContext.getExternalContext().redirect(navigateString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void registrarFactura() {
		try {

			log.info("Ingreso a registrarFactura...");
			// ----datos para la factrua
			Date fechaFactura = new Date();
			dosificacion = dosificacionRepository
					.findActivaBySucursal(newFactura.getSucursal());
			numeroFactura = dosificacion.getNumeroSecuencia();
			// ---------------------------registro de
			CodigoControl7 control = new CodigoControl7();
			String CC = control.obtenerCodigoControl(fechaFactura,
					dosificacion, newFactura.getTotalPagar(),
					nitCliente.getNit());
			System.out.println("Codigo de Control : " + CC);
			if (CC.length() == 14 || CC.length() == 11) {
				// factura------------------------------------
				newFactura.setCambio(6.93);
				// Numero
				// de
				// Factura

				newFactura.setTotalEfectivo(0);
				newFactura.setCambio(0);
				newFactura.setEstado("V");
				newFactura.setFechaLimiteEmision(dosificacion
						.getFechaLimiteEmision()); // Fecha de Emision
				newFactura.setTotalFacturado(newFactura.getTotalPagar()); // Total
																			// Bs
				newFactura.setTotalLiteral(obtenerMontoLiteral(newFactura
						.getTotalFacturado()));
				newFactura.setCodigoControl(CC);// Codigo de Control
				// tipo de cliente
				if (nitCliente.getCliente().getTipo().equals("NATURAL")) { // NAURAL
																			// o
																			// JURIDICO
					newFactura.setNitCi(nitCliente.getCliente().getCi());// CI
																			// del
																			// Comprador
				} else {
					newFactura.setNitCi(nitCliente.getNit());// NIT del
																// Comprador
				}
				newFactura.setTotalEfectivo(totalEfectivo);
				newFactura.setCambio(totalCambio);
				newFactura.setCliente(busquedaCliente);
				newFactura.setConcepto("Venta: " + numeroFactura);
				newFactura.setEmpresa(empresaLogin);
				newFactura.setFechaRegistro(new Date());
				newFactura.setTipoPago("EFECTIVO");
				newFactura.setUsuarioRegistro(nombreUsuario);
				newFactura.setNumeroFactura(""
						+ dosificacion.getNumeroSecuencia());
				newFactura.setNitCi(nitCliente.getNit());
				newFactura
						.setNombreFactura(nitCliente.getCliente().getNombre());
				newFactura.setCodigoRespuestaRapida(armarCadenaQR(newFactura));
				newFactura.setId(null);

				newFactura.setTipoCambio(tipocambio);// cambiar

				// LIBRO DE VENTA
				newFactura.setImporteICE(0);
				newFactura.setImporteExportaciones(0);
				newFactura.setImporteVentasGrabadasTasaCero(0);
				newFactura.setImporteSubTotal(newFactura.getTotalFacturado()
						- newFactura.getImporteICE()
						- newFactura.getImporteExportaciones()
						- newFactura.getImporteVentasGrabadasTasaCero());
				newFactura.setImporteDescuentosBonificaciones(0);
				newFactura.setImporteBaseDebitoFiscal(newFactura
						.getImporteSubTotal()
						- newFactura.getImporteDescuentosBonificaciones());
				if (sucursalLogin.isCreditoFiscal()) {
					newFactura.setDebitoFiscal(newFactura
							.getImporteBaseDebitoFiscal() * 0.13);
					newFactura.setCreditoFiscal("");
				} else {
					newFactura.setImporteBaseDebitoFiscal(0);
					newFactura.setDebitoFiscal(0);
					newFactura.setCreditoFiscal("Sin Derecho a Credito Fiscal");
				}
				newFactura.setGestion(Time.obtenerFormatoYYYY(new Date()));
				newFactura.setMes(Time.obtenerFormatoMM(newFactura
						.getFechaRegistro()));
				facturaRegistration.create(newFactura);
				armarUrl();
				dosificacion.setNumeroSecuencia(dosificacion
						.getNumeroSecuencia() + 1);

				dosificacionRegistration.update(dosificacion);

				for (DetalleFactura detalleFactura : listDetalleFactura) {
					detalleFactura.setFactura(newFactura);
					detalleFactura.setFechaRegistro(new Date());
					detalleFactura.setUsuarioRegistro(nombreUsuario);
					detalleFacturaRegistery.create(detalleFactura);
				}

				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Factura Guardada!", listDetalleFactura.size()
								+ " productos");
				facesContext.addMessage(null, m);

				loadValuesDefaul();
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
		totalEfectivo = efectivoBolivianos + (efectivoDolares * tipocambio);
		totalCambio = totalEfectivo - totalImporte;

	}

	public String obtenerMontoLiteral(double totalFactura) {
		log.info("Total Entero Factura >>>>> " + totalFactura);
		NumerosToLetras convert = new NumerosToLetras();
		String totalLiteral;
		try {
			totalLiteral = convert.convertNumberToLetter(totalFactura);
			return totalLiteral;
		} catch (Exception e) {
			log.info("Error en obtenerMontoLiteral: " + e.getMessage());
			return "Error Literal";
		}
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
			cadenaQR = cadenaQR.concat(obtenerFechaEmision(factura
					.getFechaFactura()));
			cadenaQR = cadenaQR.concat("|");

			// Total Bs
			cadenaQR = cadenaQR.concat(String.valueOf(factura
					.getTotalFacturado()));
			cadenaQR = cadenaQR.concat("|");

			// Importe Base para el Credito Fiscal
			cadenaQR = cadenaQR.concat(String.valueOf(factura
					.getTotalFacturado()));
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

			return cadenaQR;

		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error en armarCadenaQR: " + e.getMessage());
			return cadenaQR;
		}
	}

	private String obtenerFechaEmision(Date fechaEmision) {
		try {
			String DATE_FORMAT = "dd/MM/yyyy";
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.format(fechaEmision);

		} catch (Exception e) {
			log.error("Error en obtenerFechaEmision: " + e.getMessage());
			return "Error Fecha Emision";
		}
	}

	public void onRowEdit() {
		log.info("Ingreso a onRowEdit");
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
						+ "ReportFactura?pIdFactura="
						+ newFactura.getId()
						+ "&pEmpresa="
						+ empresaLogin.getRazonSocial()
						+ "&pCiudad="
						+ empresaLogin.getCiudad()
						+ "&pPais=BOLIVIA&pLogo="
						+ urlLogo
						+ "&pNit="
						+ empresaLogin.getNit()
						+ "&pQr="
						+ newFactura.getCodigoRespuestaRapida()
						+ "&pLeyenda="
						+ URLEncoder.encode(dosificacion.getLeyendaInferior2(),
								"ISO-8859-1") + "&pInpresion="
						+ newFactura.isImpresion() + "&pTamano=" + tamano;
			}
			if (formatoHoja.getNombre().equals("SIN LOGO")) {
				url = urlPath
						+ "ReportFacturaSinCredFiscal?pIdFactura="
						+ newFactura.getId()
						+ "&pEmpresa="
						+ empresaLogin.getRazonSocial()
						+ "&pCiudad="
						+ empresaLogin.getCiudad()
						+ "&pPais=BOLIVIA&pLogo="
						+ urlLogo
						+ "&pNit="
						+ empresaLogin.getNit()
						+ "&pQr="
						+ newFactura.getCodigoRespuestaRapida()
						+ "&pLeyenda="
						+ URLEncoder.encode(dosificacion.getLeyendaInferior2(),
								"ISO-8859-1") + "&pInpresion="
						+ newFactura.isImpresion() + "&pTamano=" + tamano;
			}
			if (formatoHoja.getNombre().equals("SIN LOGO, SIN BORDE")) {
				url = urlPath
						+ "ReportFacturaSinCredFiscal?pIdFactura="
						+ newFactura.getId()
						+ "&pEmpresa="
						+ empresaLogin.getRazonSocial()
						+ "&pCiudad="
						+ empresaLogin.getCiudad()
						+ "&pPais=BOLIVIA&pLogo="
						+ urlLogo
						+ "&pNit="
						+ empresaLogin.getNit()
						+ "&pQr="
						+ newFactura.getCodigoRespuestaRapida()
						+ "&pLeyenda="
						+ URLEncoder.encode(dosificacion.getLeyendaInferior2(),
								"ISO-8859-1") + "&pInpresion="
						+ newFactura.isImpresion() + "&pTamano=" + tamano;
			}

			if (newFactura.isImpresion()) {
				newFactura.setImpresion(false);
				facturaRegistration.update(newFactura);
			}
			log.info("getURL() -> " + url);
		} catch (Exception e) {
			// TODO: handle exception
		}
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

	public UIData getUsersDataTable() {
		return usersDataTable;
	}

	public void setUsersDataTable(UIData usersDataTable) {
		this.usersDataTable = usersDataTable;
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

	public List<EDProductoProducto> getListEdProductos() {
		return listEdProductos;
	}

	public void setListEdProductos(List<EDProductoProducto> listEdProductos) {
		this.listEdProductos = listEdProductos;
	}

	public boolean isNuevo() {
		return nuevo;
	}

	public void setNuevo(boolean nuevo) {
		this.nuevo = nuevo;
	}

	public String getPagina() {
		return pagina;
	}

	public void setPagina(String pagina) {
		this.pagina = pagina;
	}

	public List<EDPaginas> getListEdPaginas() {
		return listEdPaginas;
	}

	public void setListEdPaginas(List<EDPaginas> listEdPaginas) {
		this.listEdPaginas = listEdPaginas;
	}

	public int getNumpag() {
		return numpag;
	}

	public void setNumpag(int numpag) {
		this.numpag = numpag;
	}

	public List<EDProductos> getListEdProducto() {
		return listEdProducto;
	}

	public void setListEdProducto(List<EDProductos> listEdProducto) {
		this.listEdProducto = listEdProducto;
	}

	public List<TipoProducto> getListTipoProductos() {
		return listTipoProductos;
	}

	public void setListTipoProductos(List<TipoProducto> listTipoProductos) {
		this.listTipoProductos = listTipoProductos;
	}

	public boolean isImpresion() {
		return impresion;
	}

	public void setImpresion(boolean impresion) {
		this.impresion = impresion;
	}

}
