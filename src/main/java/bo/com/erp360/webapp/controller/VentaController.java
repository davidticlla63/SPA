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

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.MoveEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import com.ibm.icu.impl.duration.impl.Utils;

import bo.com.erp360.webapp.data.ClienteRepository;
import bo.com.erp360.webapp.data.DetalleNotaVentaRepository;
import bo.com.erp360.webapp.data.DetalleOrdenProductoRepository;
import bo.com.erp360.webapp.data.DetalleOrdenServicioRepository;
import bo.com.erp360.webapp.data.DosificacionRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.FormatoHojaRepository;
import bo.com.erp360.webapp.data.GestionRepository;
import bo.com.erp360.webapp.data.NitClienteRepository;
import bo.com.erp360.webapp.data.OrdenVentaRepository;
import bo.com.erp360.webapp.data.ParametroRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.TamanoHojaRepository;
import bo.com.erp360.webapp.data.TipoCambioRepository;
import bo.com.erp360.webapp.data.UsuarioEmpresaRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.DetalleFactura;
import bo.com.erp360.webapp.model.DetalleNotaVenta;
import bo.com.erp360.webapp.model.DetalleOrdenProducto;
import bo.com.erp360.webapp.model.DetalleOrdenServicio;
import bo.com.erp360.webapp.model.Dosificacion;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Factura;
import bo.com.erp360.webapp.model.FormatoHoja;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.NitCliente;
import bo.com.erp360.webapp.model.NotaVenta;
import bo.com.erp360.webapp.model.OrdenVenta;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.TamanoHoja;
import bo.com.erp360.webapp.model.TipoCambio;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.ClienteRegistration;
import bo.com.erp360.webapp.service.DetalleFacturaRegistration;
import bo.com.erp360.webapp.service.DetalleNotaVentaRegistration;
import bo.com.erp360.webapp.service.DetalleOrdenServicioRegistration;
import bo.com.erp360.webapp.service.DosificacionRegistration;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.FacturaRegistration;
import bo.com.erp360.webapp.service.NitClienteRegistration;
import bo.com.erp360.webapp.service.NotaVentaRegistration;
import bo.com.erp360.webapp.service.OrdenVentaRegistration;
import bo.com.erp360.webapp.util.CodigoControl7;
import bo.com.erp360.webapp.util.NumerosToLetras;
import bo.com.erp360.webapp.util.Time;

@Named(value = "ventaController")
@SuppressWarnings("serial")
@ConversationScoped
public class VentaController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	private String url;

	@Inject
	Conversation conversation;

	private @Inject FormatoHojaRepository formatoHojaRepository;
	private FormatoHoja formatoHoja;

	private @Inject EmpresaRepository empresaRepository;

	private @Inject OrdenVentaRepository ordenVentaRepository;

	private @Inject UsuarioRepository usuarioRepository;

	private @Inject UsuarioEmpresaRepository usuarioEmpresaRepository;

	// DetalleOrdenServicios
	private List<DetalleOrdenServicio> listDetalleOrdenServicio = new ArrayList<DetalleOrdenServicio>();

	private DetalleOrdenServicio detalleOrdenServicio;

	private DetalleOrdenServicio selectedDetalleOrdenServicio;

	private @Inject DetalleOrdenServicioRepository detalleOrdenServicioRepository;

	// detalle orden de producto

	private List<DetalleOrdenProducto> listDetalleOrdenProducto = new ArrayList<DetalleOrdenProducto>();
	private @Inject DetalleOrdenProductoRepository detalleOrdenProductoRepository;

	Logger log = Logger.getLogger(VentaController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventVenta;

	// estados
	private boolean modificar;

	private String nombreEstado = "ACTIVO";
	private String tipoColumnTable; // 8

	private List<Producto> listFilterProducto;
	private String[] listEstado = { "ACTIVO", "INACTIVO" };
	private String[] listResolucionNormativa = { "NSF-07", "SFV-14" };

	// login
	private Usuario usuario;
	private String nombreUsuario;
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;
	private Sucursal sucursalLogin;

	private @Inject SucursalRepository sucursalRepository;

	// LISTA DE ORDENES DE VENTA
	private OrdenVenta ordenVenta;

	private OrdenVenta selectedOrdenVenta;
	private List<OrdenVenta> listOrdenVenta = new ArrayList<OrdenVenta>();

	private List<Usuario> listEmpleado = new ArrayList<Usuario>();

	private Date fecha;

	private Usuario selectedEmpleado = new Usuario();

	private String textoAutoCompleteEmpleado;

	private Cliente cliente;
	private String textoAutoCliente;

	private List<Cliente> listCliente = new ArrayList<Cliente>();

	private @Inject OrdenVentaRegistration ordenVentaRegistration;

	private List<DetalleNotaVenta> listDetalleNotaVenta = new ArrayList<DetalleNotaVenta>();

	private @Inject DetalleNotaVentaRepository detalleNotaVentaRepository;

	private @Inject DetalleNotaVentaRegistration detalleNotaVentaRegistration;

	private @Inject NotaVentaRegistration notaVentaRegistration;

	// TIPO DE CAMBIO
	private TipoCambio selectedTipoCambio;

	private @Inject TipoCambioRepository tipoCambioRepository;

	private double totals = 0;

	private String estadoVenta = "NINGUNO";// NINGUNO,FACTURA,NOTAVENTA

	private Factura newFactura;

	private boolean error = false;

	private Gestion gestionLogin;

	@Inject
	private DosificacionRepository dosificacionRepository;

	private Dosificacion dosificacion;

	private boolean puedofacturar = true;

	private @Inject DetalleFacturaRegistration detalleFacturaRegistery;

	private double diasrestantes = 0;

	private int numeroFactura;

	private String textoAutoCompleteNit;
	private NitCliente nitCliente;

	private String textoAutoCompleteCliente;

	private Cliente busquedaCliente;

	private List<NitCliente> listNitCliente = new ArrayList<NitCliente>();

	private @Inject NitClienteRepository nitClienteRepository;

	private @Inject ClienteRepository clienteRepository;

	@Inject
	private ClienteRegistration clienteRegistration;

	@Inject
	private NitClienteRegistration nitClienteRegistration;

	@Inject
	private FacturaRegistration facturaRegistration;

	private @Inject DosificacionRegistration dosificacionRegistration;

	private List<DetalleFactura> listDetalleFactura = new ArrayList<DetalleFactura>();

	@Inject
	private GestionRepository gestionRepository;

	private @Inject TamanoHojaRepository tamanoHojaRepository;

	private double efectivoBolivianos = 0;
	private double efectivoDolares = 0;
	private double totalCambio = 0;
	private double totalEfectivo = 0;

	private double tipocambio = 6.80;
	private List<TipoCambio> listTipoCambio;

	// IVA
	private double iva = 0;
	private @Inject ParametroRepository parametroRepository;

	@PostConstruct
	public void initNewProducto() {
		log.info(" init new initNewProducto controller");
		beginConversation();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		usuario = estadoUsuarioLogin.getUsuarioSession(usuarioRepository);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		gestionLogin = estadoUsuarioLogin.getGestionSession(empresaRepository,
				gestionRepository);
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);
		sucursalLogin = estadoUsuarioLogin.getSucursalSession(
				empresaRepository, sucursalRepository);
		fecha = new Date();

		setSelectedTipoCambio(tipoCambioRepository.findAllByEmpresaAndFecha(
				empresaLogin, new Date()));
		loadDefault();

	}

	public void loadDefault() {
		modificar = false;
		tipoColumnTable = "col-md-12";

		listDetalleOrdenServicio.clear();
		listCliente = clienteRepository.findActivosByEmpresa(empresaLogin);
		consultarOrdenVentaPorFecha();
		totals = 0;
		estadoVenta = "NINGUNO";
		error = false;
		listTipoCambio = tipoCambioRepository.findAllByEmpresa(empresaLogin);
		tipocambio = listTipoCambio.get(0).getUnidad();

		iva = parametroRepository
				.findAllActivasByEmpresaForSucursal(empresaLogin,
						sucursalLogin, "IVA").get(0).getValor();
	}

	public void consultarOrdenVentaPorFecha() {
		listOrdenVenta = ordenVentaRepository.findAllActivasProcesadas(
				empresaLogin, sucursalLogin, fecha);
	}

	public void crearFactura() {
		newFactura = new Factura();
		newFactura.setFechaFactura(new Date());
		newFactura.setSucursal(sucursalLogin);
		newFactura.setEmpresa(empresaLogin);
		newFactura.setTotalPagar(ordenVenta.getTotal());

		dosificacion = dosificacionRepository.findActivaBySucursal(newFactura
				.getSucursal());

		diasrestantes = Math.floor((dosificacion.getFechaLimiteEmision()
				.getTime() - (new Date()).getTime()) / (3600 * 24 * 1000));

		setPuedofacturar(dosificacion.getFechaLimiteEmision().getTime() >= (new Date())
				.getTime());

		newFactura.setNumeroAutorizacion(dosificacion.getNumeroAutorizacion());

		numeroFactura = dosificacion.getNumeroSecuencia();
		estadoVenta = "FACTURA";

		textoAutoCompleteNit = ordenVenta.getCliente().getNit();
		textoAutoCompleteCliente = ordenVenta.getCliente().getNombre();

		llenarDetalleFactura();
	}

	public void crearNotaVenta() {
		estadoVenta = "NOTAVENTA";
		textoAutoCompleteNit = ordenVenta.getCliente().getNit();
		textoAutoCompleteCliente = ordenVenta.getCliente().getNombre();
		llenarDetalle();
	}

	private void llenarDetalle() {
		try {
			log.info("Ingreso a llenarDetalle");
			listDetalleNotaVenta.clear();
			for (DetalleOrdenServicio detalleOrdenServicio : listDetalleOrdenServicio) {
				DetalleNotaVenta detalleNotaVenta = new DetalleNotaVenta();
				detalleNotaVenta.setCodigo("SERV00-"
						+ detalleOrdenServicio.getServicios().getId());
				detalleNotaVenta
						.setEmpleado(detalleOrdenServicio.getVendedor());
				detalleNotaVenta
						.setCantidad(detalleOrdenServicio.getCantidad());
				detalleNotaVenta.setConcepto(detalleOrdenServicio.getNombre());
				detalleNotaVenta.setPrecioUnitario(detalleOrdenServicio
						.getPrecio());
				detalleNotaVenta.setPorcentajeComision(detalleOrdenServicio
						.getServicios().getComision() / 100);
				detalleNotaVenta.setComision(detalleNotaVenta
						.getPorcentajeComision()
						* detalleNotaVenta.getPrecioTotal());
				detalleNotaVenta
						.setPrecioTotal(detalleOrdenServicio.getTotal());
				detalleNotaVenta.setTipoCambio(selectedTipoCambio.getUnidad());
				listDetalleNotaVenta.add(detalleNotaVenta);
			}
			for (DetalleOrdenProducto detalleOrdenProducto : listDetalleOrdenProducto) {
				DetalleNotaVenta detalleNotaVenta = new DetalleNotaVenta();
				detalleNotaVenta.setCodigo("PRO00-"
						+ detalleOrdenProducto.getProducto().getId());
				detalleNotaVenta
						.setEmpleado(detalleOrdenProducto.getVendedor());
				detalleNotaVenta
						.setCantidad(detalleOrdenProducto.getCantidad());
				detalleNotaVenta.setConcepto(detalleOrdenProducto.getNombre());
				detalleNotaVenta.setPrecioUnitario(detalleOrdenProducto
						.getPrecio());
				detalleNotaVenta.setPorcentajeComision(detalleOrdenProducto
						.getProducto().getComision() / 100);
				detalleNotaVenta.setComision(detalleNotaVenta
						.getPorcentajeComision()
						* detalleNotaVenta.getPrecioTotal());
				detalleNotaVenta

				.setPrecioTotal(detalleOrdenProducto.getTotal());
				detalleNotaVenta.setTipoCambio(selectedTipoCambio.getUnidad());
				listDetalleNotaVenta.add(detalleNotaVenta);
			}
			calcularTotal();
		} catch (Exception e) {
			log.error("Error en llenarDetalle : " + e.getMessage());
		}
	}

	private void llenarDetalleFactura() {
		try {
			log.info("Ingreso a llenarDetalleFactura");
			listDetalleNotaVenta.clear();

			double total = 0;
			for (DetalleOrdenServicio detalleOrdenServicio : listDetalleOrdenServicio) {
				DetalleNotaVenta detalleNotaVenta = new DetalleNotaVenta();
				detalleNotaVenta.setCodigo("SERV00-"
						+ detalleOrdenServicio.getServicios().getId());
				detalleNotaVenta
						.setEmpleado(detalleOrdenServicio.getVendedor());
				detalleNotaVenta
						.setCantidad(detalleOrdenServicio.getCantidad());
				detalleNotaVenta.setConcepto(detalleOrdenServicio.getNombre());
				detalleNotaVenta.setPrecioUnitario(detalleOrdenServicio
						.getPrecio());

				detalleNotaVenta.setPorcentajeComision((detalleOrdenServicio
						.getServicios().getComision() / 100));
				detalleNotaVenta
						.setPrecioTotal(detalleOrdenServicio.getTotal());
				total = detalleOrdenServicio.getTotal()
						- (detalleOrdenServicio.getTotal() * (iva * 0.01));
				detalleNotaVenta.setComision(detalleNotaVenta
						.getPorcentajeComision() * total);
				detalleNotaVenta.setTipoCambio(selectedTipoCambio.getUnidad());
				listDetalleNotaVenta.add(detalleNotaVenta);
			}
			for (DetalleOrdenProducto detalleOrdenProducto : listDetalleOrdenProducto) {
				DetalleNotaVenta detalleNotaVenta = new DetalleNotaVenta();
				detalleNotaVenta.setCodigo("PRO00-"
						+ detalleOrdenProducto.getProducto().getId());
				detalleNotaVenta
						.setEmpleado(detalleOrdenProducto.getVendedor());
				detalleNotaVenta
						.setCantidad(detalleOrdenProducto.getCantidad());
				detalleNotaVenta.setConcepto(detalleOrdenProducto.getNombre());
				detalleNotaVenta.setPrecioUnitario(detalleOrdenProducto
						.getPrecio());
				detalleNotaVenta.setPorcentajeComision((detalleOrdenProducto
						.getProducto().getComision() / 100));
				detalleNotaVenta
						.setPrecioTotal(detalleOrdenProducto.getTotal());
				total = detalleOrdenProducto.getTotal()
						- (detalleOrdenProducto.getTotal() * (iva * 0.01));
				detalleNotaVenta.setComision(detalleNotaVenta
						.getPorcentajeComision() * total);
				detalleNotaVenta.setTipoCambio(selectedTipoCambio.getUnidad());
				listDetalleNotaVenta.add(detalleNotaVenta);
			}
			calcularTotal();
		} catch (Exception e) {
			log.error("Error en llenarDetalleFactura : " + e.getMessage());
		}
	}

	private void calcularTotal() {
		totals = 0;
		for (DetalleNotaVenta detalleNotaVenta : listDetalleNotaVenta) {
			totals += detalleNotaVenta.getPrecioTotal();
		}
	}

	public boolean verificarNitCi() {
		try {
			log.info("Entro a verificarNitCi : " + textoAutoCompleteNit + " - "
					+ textoAutoCompleteCliente);
			if (textoAutoCompleteNit.trim().length() == 0
					|| textoAutoCompleteCliente.trim().length() == 0) {
				return false;
			}
			if (clienteRepository.ExistCliente(textoAutoCompleteCliente)) {
				busquedaCliente = clienteRepository
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

	private void copyDetalle() {
		listDetalleFactura.clear();
		for (DetalleNotaVenta detalleNotaVenta : listDetalleNotaVenta) {
			DetalleFactura detalleFactura = new DetalleFactura();
			detalleFactura.setCantidad(detalleNotaVenta.getCantidad());
			detalleFactura.setConcepto(detalleNotaVenta.getConcepto());
			detalleFactura.setPrecioUnitario(detalleNotaVenta
					.getPrecioUnitario());
			detalleFactura.setPrecioTotal(detalleNotaVenta.getPrecioTotal());
			detalleFactura.setCodigoProducto(detalleNotaVenta.getCodigo());
			listDetalleFactura.add(detalleFactura);
		}
	}

	// Calcula el cambio al cliente por el pago realizado
	public void calcularCambioDePago() {
		totalEfectivo = efectivoBolivianos + (efectivoDolares * tipocambio);
		totalCambio = totalEfectivo - totals;

	}

	public void registrarEImprimirNotaVenta() {
		if (verificarNitCi()) {
			registrarNotaVentaSinFactura();
			if (!error) {
				RequestContext context = RequestContext.getCurrentInstance();
				context.execute("PF('dlgNotaVistaPrevia').show();");
			}
		} else {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					"A ocurrido un error!", " Revisar Datos del Cliente o Nit");
			facesContext.addMessage(null, m);
		}

	}

	private void registrarNotaVentaSinFactura() {
		try {
			log.info("Ingreso a registrarNotaVentaSinFactura");
			NotaVenta notaVenta = new NotaVenta();
			notaVenta.setCambio(0);
			notaVenta.setCliente(ordenVenta.getCliente());
			notaVenta.setEmpresa(empresaLogin);
			notaVenta.setNombreCliente(textoAutoCompleteCliente);
			notaVenta.setConcepto("NOTA VENTA");
			notaVenta.setEstado("AC");
			notaVenta.setGestion(Time.obtenerFormatoYYYY(new Date()));
			notaVenta.setSucursal(sucursalLogin);
			notaVenta.setMes(Time.obtenerFormatoMM(new Date()));
			notaVenta.setFechaRegistro(new Date());
			notaVenta.setNitCi(nitCliente.getNit());
			notaVenta
					.setTotalLiteral(obtenerMontoLiteral(ordenVenta.getTotal()));
			notaVenta.setTipoCambio(selectedTipoCambio.getUnidad());
			notaVenta.setUsuarioRegistro(nombreUsuario);
			notaVenta.setTotalPagar(ordenVenta.getTotal());
			notaVenta.setTipoPago("EFECTIVO");
			notaVenta.setTotalVenta(ordenVenta.getTotal());
			notaVenta.setNumeroOrden(ordenVenta.getNumeroSecuencia());
			notaVenta.setNumeroTicket(ordenVenta.getNumeroOrden());
			notaVenta = notaVentaRegistration.create(notaVenta);

			for (DetalleNotaVenta detalleNotaVenta : listDetalleNotaVenta) {
				detalleNotaVenta.setNotaVenta(notaVenta);
				detalleNotaVenta.setFechaRegistro(new Date());
				detalleNotaVenta.setNumeroOrden(notaVenta.getNumeroOrden());
				detalleNotaVenta.setUsuarioRegistro(nombreUsuario);
				detalleNotaVenta.setEstado("AC");
				detalleNotaVenta.setComision(detalleNotaVenta.getPrecioTotal()
						* detalleNotaVenta.getPorcentajeComision());
				detalleNotaVentaRegistration.create(detalleNotaVenta);
			}
			armarUrlNotaVenta(notaVenta);
			ordenVenta.setEstado("PR");
			ordenVenta.setTipoTansaccion("NOTA VENTA");
			ordenVentaRegistration.update(ordenVenta);

			loadDefault();
		} catch (Exception e) {
			error = true;
			log.error("Error en registrarNotaVentaSinFactura : "
					+ e.getMessage());
		}
	}

	private void registrarNotaVenta() {
		try {
			log.info("Ingreso a registrarNotaVenta");
			NotaVenta notaVenta = new NotaVenta();
			notaVenta.setCambio(0);
			notaVenta.setCliente(ordenVenta.getCliente());
			notaVenta.setEmpresa(empresaLogin);
			notaVenta.setNombreCliente(textoAutoCompleteCliente);
			notaVenta.setConcepto("FACTURA");
			notaVenta.setEstado("AC");
			notaVenta.setGestion(Time.obtenerFormatoYYYY(new Date()));
			notaVenta.setSucursal(sucursalLogin);
			notaVenta.setMes(Time.obtenerFormatoMM(new Date()));
			notaVenta.setFechaRegistro(new Date());
			notaVenta.setNitCi(nitCliente.getNit());
			notaVenta.setTotalLiteral(obtenerMontoLiteral(newFactura
					.getTotalFacturado()));
			notaVenta.setNumeroFactura(newFactura.getNumeroFactura());
			notaVenta.setTipoCambio(selectedTipoCambio.getUnidad());
			notaVenta.setUsuarioRegistro(nombreUsuario);
			notaVenta.setTotalPagar(ordenVenta.getTotal());
			notaVenta.setTipoPago("EFECTIVO");
			notaVenta.setTotalVenta(ordenVenta.getTotal());
			notaVenta.setNumeroOrden(ordenVenta.getNumeroSecuencia());
			notaVenta.setNumeroTicket(ordenVenta.getNumeroOrden());
			notaVenta = notaVentaRegistration.create(notaVenta);

			for (DetalleNotaVenta detalleNotaVenta : listDetalleNotaVenta) {
				detalleNotaVenta.setNotaVenta(notaVenta);
				detalleNotaVenta.setNumeroOrden(notaVenta.getNumeroOrden());
				detalleNotaVenta.setFechaRegistro(new Date());
				detalleNotaVenta.setUsuarioRegistro(nombreUsuario);
				detalleNotaVenta.setEstado("AC");
				detalleNotaVentaRegistration.create(detalleNotaVenta);
			}
		} catch (Exception e) {
			log.error("Error en registrarNotaVenta : " + e.getMessage());
		}
	}

	public void registrarEImprimir() {
		if (verificarNitCi()) {
			copyDetalle();
			registrarFactura();
			registrarNotaVenta();
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

				newFactura.setTotalEfectivo(totalEfectivo);
				newFactura.setCambio(totalCambio);
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

				newFactura.setTipoCambio(selectedTipoCambio.getUnidad());// cambiar

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
				iva = parametroRepository
						.findAllActivasByEmpresaForSucursal(empresaLogin,
								sucursalLogin, "IVA").get(0).getValor();
				if (sucursalLogin.isCreditoFiscal()) {
					newFactura.setDebitoFiscal(newFactura
							.getImporteBaseDebitoFiscal() * (iva * 0.01));
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
						"Factura Guardada!", newFactura.getConcepto());
				facesContext.addMessage(null, m);
				ordenVenta.setEstado("PR");
				ordenVenta.setTipoTansaccion("FACTURADO");
				ordenVentaRegistration.update(ordenVenta);
				loadDefault();
			}
		} catch (Exception e) {
			String errorMessage = getRootErrorMessage(e);
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_ERROR,
					errorMessage, "Guardado Incorrecto.");
			facesContext.addMessage(null, m);
			error = true;
			log.error(errorMessage);
		}
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

	private String urlNotaVenta;

	private void armarUrlNotaVenta(NotaVenta notaVenta) {
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

		/*
		 * formatoHoja =
		 * formatoHojaRepository.findActivosByEmpresa(empresaLogin) .get(0);
		 */
		urlNotaVenta = urlPath + "ReportNotaVenta?pIdNotaVenta="
				+ notaVenta.getId() + "&pPais=BOLIVIA&pLogo=" + urlLogo
				+ "&pTamano=" + tamano;
		/*
		 * if (formatoHoja.getNombre().equals("COMPLETO")) { urlNotaVenta =
		 * urlPath + "ReportFacturaSinCredFiscal?pIdFactura=" +
		 * newFactura.getId() + "&pEmpresa=" + empresaLogin.getRazonSocial() +
		 * "&pCiudad=" + empresaLogin.getCiudad() + "&pPais=BOLIVIA&pLogo=" +
		 * urlLogo + "&pNit=" + empresaLogin.getNit() + "&pQr=" +
		 * newFactura.getCodigoRespuestaRapida() + "&pLeyenda=" +
		 * dosificacion.getLeyendaInferior2() + "&pInpresion=" +
		 * newFactura.isImpresion() + "&pTamano=" + tamano; } if
		 * (formatoHoja.getNombre().equals("SIN LOGO")) { urlNotaVenta = urlPath
		 * + "ReportFacturaSinCredFiscal?pIdFactura=" + newFactura.getId() +
		 * "&pEmpresa=" + empresaLogin.getRazonSocial() + "&pCiudad=" +
		 * empresaLogin.getCiudad() + "&pPais=BOLIVIA&pLogo=" + urlLogo +
		 * "&pNit=" + empresaLogin.getNit() + "&pQr=" +
		 * newFactura.getCodigoRespuestaRapida() + "&pLeyenda=" +
		 * dosificacion.getLeyendaInferior2() + "&pInpresion=" +
		 * newFactura.isImpresion() + "&pTamano=" + tamano; } if
		 * (formatoHoja.getNombre().equals("SIN LOGO, SIN BORDE")) {
		 * urlNotaVenta = urlPath + "ReportFacturaSinCredFiscal?pIdFactura=" +
		 * newFactura.getId() + "&pEmpresa=" + empresaLogin.getRazonSocial() +
		 * "&pCiudad=" + empresaLogin.getCiudad() + "&pPais=BOLIVIA&pLogo=" +
		 * urlLogo + "&pNit=" + empresaLogin.getNit() + "&pQr=" +
		 * newFactura.getCodigoRespuestaRapida() + "&pLeyenda=" +
		 * dosificacion.getLeyendaInferior2() + "&pInpresion=" +
		 * newFactura.isImpresion() + "&pTamano=" + tamano; }
		 */

		log.info("getURL() -> " + urlNotaVenta);
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
				setBusquedaCliente(s.getCliente());
				nitCliente = s;
				textoAutoCompleteNit = nitCliente.getNit();
				log.info("cliente encontrado .." + nombre);
			}
		}
		setTextoAutoCompleteCliente(nitCliente.getCliente().getNombre());
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

	public void dialogClose() {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgFacturaVistaPrevia').hide();");
		if (newFactura.isImpresion()&& !newFactura.getEstado().equals("A")) {
			newFactura.setImpresion(false);
			facturaRegistration.update(newFactura);
		}
		HttpServletRequest request = (HttpServletRequest) facesContext
				.getExternalContext().getRequest();
		String navigateString = request.getContextPath()
				+ "/pages/formulario/facturacion2.xhtml";

		System.out.println(navigateString);
		try {
			facesContext.getExternalContext().redirect(navigateString);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public void onRowDetalleOrdenServicio(SelectEvent event) {
		log.info("onRowSelectOrdenVenta -> onRowSelectOrdenVenta:");
		detalleOrdenServicio = (DetalleOrdenServicio) event.getObject();
	}

	public void onRowSelectOrdenVenta(SelectEvent event) {
		log.info("onRowSelectOrdenVenta -> onRowSelectOrdenVenta:");
		ordenVenta = (OrdenVenta) event.getObject();

		modificar = true;
		if (ordenVenta.getCliente() != null) {
			textoAutoCliente = ordenVenta.getCliente().getNombre();
			cliente = ordenVenta.getCliente();
		}
		listDetalleOrdenServicio = detalleOrdenServicioRepository
				.findAllActivasByOrdenVenta(ordenVenta);

		listDetalleOrdenProducto = detalleOrdenProductoRepository
				.findAllActivasByOrdenVenta(ordenVenta);

		log.info("termino onRowSelectOrdenVenta");
	}
/*
	public void handleClose(CloseEvent event) {
		addMessage(event.getComponent().getId() + " Close ",
				"Si usted no Gusta?");
	}

	public void handleMove(MoveEvent event) {
		addMessage(event.getComponent().getId() + " Close ",
				"Left: " + event.getLeft() + " , Top " + event.getTop());
	}

	public void addMessage(String summary, String detalle) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
				summary, detalle);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}*/

	// -------- acciones para la vista----------

	public void cambiarAspecto() {
		modificar = false;

	}

	// -------- get and set---------------------

	public boolean isModificar() {
		return modificar;
	}

	public void setModificar(boolean modificar) {
		this.modificar = modificar;
	}

	public String getTest() {
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

	public List<Producto> getListFilterProducto() {
		return listFilterProducto;
	}

	public void setListFilterProducto(List<Producto> listFilterProducto) {
		this.listFilterProducto = listFilterProducto;
	}

	public String getTipoColumnTable() {
		return tipoColumnTable;
	}

	public void setTipoColumnTable(String tipoColumnTable) {
		this.tipoColumnTable = tipoColumnTable;
	}

	public String[] getListResolucionNormativa() {
		return listResolucionNormativa;
	}

	public void setListResolucionNormativa(String[] listResolucionNormativa) {
		this.listResolucionNormativa = listResolucionNormativa;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public List<DetalleOrdenServicio> getListDetalleOrdenServicio() {
		return listDetalleOrdenServicio;
	}

	public void setListDetalleOrdenServicio(
			List<DetalleOrdenServicio> listDetalleOrdenServicio) {
		this.listDetalleOrdenServicio = listDetalleOrdenServicio;
	}

	public List<OrdenVenta> getListOrdenVenta() {
		return listOrdenVenta;
	}

	public void setListOrdenVenta(List<OrdenVenta> listOrdenVenta) {
		this.listOrdenVenta = listOrdenVenta;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public OrdenVenta getOrdenVenta() {
		return ordenVenta;
	}

	public void setOrdenVenta(OrdenVenta ordenVenta) {
		this.ordenVenta = ordenVenta;
	}

	public OrdenVenta getSelectedOrdenVenta() {
		return selectedOrdenVenta;
	}

	public void setSelectedOrdenVenta(OrdenVenta selectedOrdenVenta) {
		this.selectedOrdenVenta = selectedOrdenVenta;
	}

	public DetalleOrdenServicio getDetalleOrdenServicio() {
		return detalleOrdenServicio;
	}

	public void setDetalleOrdenServicio(
			DetalleOrdenServicio detalleOrdenServicio) {
		this.detalleOrdenServicio = detalleOrdenServicio;
	}

	public List<Usuario> getListEmpleado() {
		return listEmpleado;
	}

	public void setListEmpleado(List<Usuario> listEmpleado) {
		this.listEmpleado = listEmpleado;
	}

	public Sucursal getSucursalLogin() {
		return sucursalLogin;
	}

	public void setSucursalLogin(Sucursal sucursalLogin) {
		this.sucursalLogin = sucursalLogin;
	}

	public Usuario getSelectedEmpleado() {
		return selectedEmpleado;
	}

	public void setSelectedEmpleado(Usuario selectedEmpleado) {
		this.selectedEmpleado = selectedEmpleado;
	}

	public String getTextoAutoCompleteEmpleado() {
		return textoAutoCompleteEmpleado;
	}

	public void setTextoAutoCompleteEmpleado(String textoAutoCompleteEmpleado) {
		this.textoAutoCompleteEmpleado = textoAutoCompleteEmpleado;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getTextoAutoCliente() {
		return textoAutoCliente;
	}

	public void setTextoAutoCliente(String textoAutoCliente) {
		this.textoAutoCliente = textoAutoCliente;
	}

	public List<Cliente> getListCliente() {
		return listCliente;
	}

	public void setListCliente(List<Cliente> listCliente) {
		this.listCliente = listCliente;
	}

	public DetalleOrdenServicio getSelectedDetalleOrdenServicio() {
		return selectedDetalleOrdenServicio;
	}

	public void setSelectedDetalleOrdenServicio(
			DetalleOrdenServicio selectedDetalleOrdenServicio) {
		this.selectedDetalleOrdenServicio = selectedDetalleOrdenServicio;
	}

	public List<DetalleOrdenProducto> getListDetalleOrdenProducto() {
		return listDetalleOrdenProducto;
	}

	public void setListDetalleOrdenProducto(
			List<DetalleOrdenProducto> listDetalleOrdenProducto) {
		this.listDetalleOrdenProducto = listDetalleOrdenProducto;
	}

	public double getTotals() {
		return totals;
	}

	public void setTotals(double totals) {
		this.totals = totals;
	}

	public List<DetalleNotaVenta> getListDetalleNotaVenta() {
		return listDetalleNotaVenta;
	}

	public void setListDetalleNotaVenta(
			List<DetalleNotaVenta> listDetalleNotaVenta) {
		this.listDetalleNotaVenta = listDetalleNotaVenta;
	}

	public TipoCambio getSelectedTipoCambio() {
		return selectedTipoCambio;
	}

	public void setSelectedTipoCambio(TipoCambio selectedTipoCambio) {
		this.selectedTipoCambio = selectedTipoCambio;
	}

	public String getEstadoVenta() {
		return estadoVenta;
	}

	public void setEstadoVenta(String estadoVenta) {
		this.estadoVenta = estadoVenta;
	}

	public Factura getNewFactura() {
		return newFactura;
	}

	public void setNewFactura(Factura newFactura) {
		this.newFactura = newFactura;
	}

	public Dosificacion getDosificacion() {
		return dosificacion;
	}

	public void setDosificacion(Dosificacion dosificacion) {
		this.dosificacion = dosificacion;
	}

	public double getDiasrestantes() {
		return diasrestantes;
	}

	public void setDiasrestantes(double diasrestantes) {
		this.diasrestantes = diasrestantes;
	}

	public int getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(int numeroFactura) {
		this.numeroFactura = numeroFactura;
	}

	public String getTextoAutoCompleteNit() {
		return textoAutoCompleteNit;
	}

	public void setTextoAutoCompleteNit(String textoAutoCompleteNit) {
		this.textoAutoCompleteNit = textoAutoCompleteNit;
	}

	public List<NitCliente> getListNitCliente() {
		return listNitCliente;
	}

	public void setListNitCliente(List<NitCliente> listNitCliente) {
		this.listNitCliente = listNitCliente;
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

	public NitCliente getNitCliente() {
		return nitCliente;
	}

	public void setNitCliente(NitCliente nitCliente) {
		this.nitCliente = nitCliente;
	}

	public List<DetalleFactura> getListDetalleFactura() {
		return listDetalleFactura;
	}

	public void setListDetalleFactura(List<DetalleFactura> listDetalleFactura) {
		this.listDetalleFactura = listDetalleFactura;
	}

	public Gestion getGestionLogin() {
		return gestionLogin;
	}

	public void setGestionLogin(Gestion gestionLogin) {
		this.gestionLogin = gestionLogin;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public boolean isPuedofacturar() {
		return puedofacturar;
	}

	public void setPuedofacturar(boolean puedofacturar) {
		this.puedofacturar = puedofacturar;
	}

	public String getUrlNotaVenta() {
		return urlNotaVenta;
	}

	public void setUrlNotaVenta(String urlNotaVenta) {
		this.urlNotaVenta = urlNotaVenta;
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

	public List<TipoCambio> getListTipoCambio() {
		return listTipoCambio;
	}

	public void setListTipoCambio(List<TipoCambio> listTipoCambio) {
		this.listTipoCambio = listTipoCambio;
	}
}
