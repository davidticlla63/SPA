package bo.com.erp360.webapp.controller;

import java.io.Serializable;
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

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.ClienteRepository;
import bo.com.erp360.webapp.data.DetalleOrdenProductoRepository;
import bo.com.erp360.webapp.data.DetalleOrdenServicioRepository;
import bo.com.erp360.webapp.data.EmpleadoRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.OrdenVentaRepository;
import bo.com.erp360.webapp.data.ProductoRepository;
import bo.com.erp360.webapp.data.ServicioRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.UsuarioEmpresaRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.CentroCosto;
import bo.com.erp360.webapp.model.Cliente;
import bo.com.erp360.webapp.model.DetalleOrdenProducto;
import bo.com.erp360.webapp.model.DetalleOrdenServicio;
import bo.com.erp360.webapp.model.Empleado;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.NitCliente;
import bo.com.erp360.webapp.model.OrdenVenta;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.Servicio;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.model.UsuarioEmpresa;
import bo.com.erp360.webapp.service.ClienteRegistration;
import bo.com.erp360.webapp.service.DetalleOrdenProductoRegistration;
import bo.com.erp360.webapp.service.DetalleOrdenServicioRegistration;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.NitClienteRegistration;
import bo.com.erp360.webapp.service.OrdenVentaRegistration;
import bo.com.erp360.webapp.util.EDCentroCosto;
import bo.com.erp360.webapp.util.SessionMain;

@Named(value = "asignacionServicioController")
@SuppressWarnings("serial")
@ConversationScoped
public class AsignacionServicioController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

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

	Logger log = Logger.getLogger(AsignacionServicioController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventOrdenVenta;

	// estados
	private boolean modificar;
	private boolean estadoButtonDialog;

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

	private List<Empleado> listEmpleado = new ArrayList<Empleado>();

	private Date fecha;

	private Empleado selectedEmpleado = new Empleado();
	
	private @Inject EmpleadoRepository empleadoRepository;

	private String textoAutoCompleteEmpleado;

	private Cliente cliente;
	private String textoAutoCliente;

	private List<Cliente> listCliente = new ArrayList<Cliente>();

	private @Inject ClienteRepository clienteRepository;

	private @Inject DetalleOrdenServicioRegistration detalleOrdenServicioRegistration;
	private @Inject OrdenVentaRegistration ordenVentaRegistration;

	private double totalServicio = 0;

	private boolean estadoFinalizado = false;

	private String selectedVista = "servicio"; // false = servicio,
												// true=producto

	// AGREGACION DE PRODUCTO

	private @Inject DetalleOrdenProductoRegistration detalleOrdenProductoRegistration;
	private @Inject ProductoRepository productoRepository;

	private int cantidad = 1;
	private double precio = 0;
	private double total = 0;
	private double totals = 0;

	private Producto selectedProducto = new Producto();
	private List<Producto> listProducto = new ArrayList<Producto>();

	private String textoAutoProducto;// AUTOCOMPLETE

	// AGREGACION DE SERVICIO

	private int cantidadServicio = 1;
	private double precioServicio = 0;
	private double totalsServicio = 0;

	private Servicio selectedServicio = new Servicio();
	private List<Servicio> listServicio = new ArrayList<Servicio>();

	private String textoAutoServicio;// AUTOCOMPLETE
	
	private String nombre="";

	private @Inject ServicioRepository servicioRepository;

	// PARA VERIFICAR EL ESTADO

	private int cantservicio = 0;
	private int cantproducto = 0;
	
	//CLIENTE
	
	private Cliente newClientes;
	
	private String clienteEstado="AC";
	
	private boolean permitirCredito= false;
	
	@Inject
	private ClienteRegistration clientesRegistration;
	
	private @Inject SessionMain sessionMain; //variable del login
	
	private Gestion gestionLogin;

	@PostConstruct
	public void initNewProducto() {
		log.info(" init new initNewProducto controller");
		beginConversation();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		usuario = estadoUsuarioLogin.getUsuarioSession(usuarioRepository);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);
		sucursalLogin = estadoUsuarioLogin.getSucursalSession(
				empresaRepository, sucursalRepository);
		gestionLogin = sessionMain.getGestionLoggin();
		fecha = new Date();
		loadDefault();

	}

	public void loadDefault() {
		modificar = false;
		estadoButtonDialog = true;
		tipoColumnTable = "col-md-12";

		listDetalleOrdenServicio.clear();
		listDetalleOrdenProducto.clear();
		listCliente = clienteRepository.findActivosByEmpresa(empresaLogin);

		consultarOrdenVentaPorFecha();
		totals = 0;
		totalServicio = 0;
		estadoFinalizado = false;
		cantidad = 1;
		precio = 0;
		total = 0;

		cantidadServicio = 1;
		precioServicio = 0;
		totalsServicio = 0;
		permitirCredito= false;
	}

	public void consultarOrdenVentaPorFecha() {
		listOrdenVenta = ordenVentaRepository.findAllActivas(empresaLogin,
				sucursalLogin, fecha);
	}
	
	
	public void crearCliente(){
		try {
			log.info("Ingreso a crearCliente");
			newClientes= new Cliente();
			clienteEstado="AC";
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgCliente').show();");
		} catch (Exception e) {
			log.error("Error en crearCliente : "+e.getMessage());
		}
	}

	public void crearOrdenVenta() {
		try {
			log.info("Ingreso a crearOrdenVenta");
			ordenVenta = new OrdenVenta();
			ordenVenta.setUsuarioRegistro(nombreUsuario);
			ordenVenta.setFechaRegistro(new Date());
			ordenVenta.setEmpresa(empresaLogin);
			ordenVenta.setSucursal(sucursalLogin);
			ordenVenta.setCliente(null);
			ordenVenta.setNumeroSecuencia(ordenVentaRepository.findCorrelativoDiaria(empresaLogin, sucursalLogin, fecha));			
			ordenVenta = ordenVentaRegistration.create(ordenVenta);
			modificar = true;
			listProducto = productoRepository.findAllActivas(empresaLogin);
			listServicio = servicioRepository
					.findAllActivosByEmpresa(empresaLogin);
		} catch (Exception e) {
			log.error("Error en crearOrdenVenta : " + e.getMessage());
		}
	}

	public void listarEmpleado(DetalleOrdenServicio selectedDetalleOrden) {
		try {
			log.info("Ingreso a listarEmpleado()");
			selectedDetalleOrdenServicio = selectedDetalleOrden;
			listEmpleado = empleadoRepository
					.findAllActive(empresaLogin);
			System.out.println("size : " + listEmpleado.size() + " Detalle: "
					+ selectedDetalleOrdenServicio.getNombre());
		} catch (Exception e) {
			log.error("Error en listarEmpleado() : " + e.getStackTrace());
		}
	}

	private DetalleOrdenProducto selectedDetalleOrdenProducto;

	public void listarEmpleadoProducto(DetalleOrdenProducto selectedDetalleOrden) {
		try {
			log.info("Ingreso a listarEmpleadoProducto()");
			selectedDetalleOrdenProducto = selectedDetalleOrden;
			listEmpleado = empleadoRepository
					.findAllActive(empresaLogin);
			System.out.println("size : " + listEmpleado.size() + " Detalle: "
					+ selectedDetalleOrdenProducto.getNombre());
		} catch (Exception e) {
			log.error("Error en listarEmpleadoProducto() : "
					+ e.getStackTrace());
		}
	}

	public void actualizarDatos() {
		try {
			if (ordenVenta == null || modificar == false) {
				return;
			}
			log.info("Ingreso a actualizarDatos");
			listDetalleOrdenServicio = detalleOrdenServicioRepository
					.findAllActivasByOrdenVenta(ordenVenta);
			listDetalleOrdenProducto = detalleOrdenProductoRepository
					.findAllActivasByOrdenVenta(ordenVenta);
			ordenVenta = ordenVentaRepository.findById(ordenVenta.getId());
			calcularTotalServicio();
			calcularTotals();
		} catch (Exception e) {
			log.error("Error en actualizarDatos : " + e.getMessage());
		}
	}

	public void asignarEmpleado() {
		log.info("Ingreso a asignarEmpleado...");
		selectedDetalleOrdenServicio.setVendedor(selectedEmpleado);
		selectedDetalleOrdenServicio.setProceso("INICIADO");
		selectedDetalleOrdenServicio.setAsignado(true);
		detalleOrdenServicioRegistration.update(selectedDetalleOrdenServicio);
		listDetalleOrdenServicio = detalleOrdenServicioRepository
				.findAllActivasByOrdenVenta(ordenVenta);
		listDetalleOrdenProducto = detalleOrdenProductoRepository
				.findAllActivasByOrdenVenta(ordenVenta);

		calcularTotalServicio();
		calcularTotals();
	}

	public void asignarEmpleadoProducto() {
		log.info("Ingreso a asignarEmpleadoProducto...");
		selectedDetalleOrdenProducto.setVendedor(selectedEmpleado);
		selectedDetalleOrdenProducto.setAsignado(true);
		detalleOrdenProductoRegistration.update(selectedDetalleOrdenProducto);
		listDetalleOrdenServicio = detalleOrdenServicioRepository
				.findAllActivasByOrdenVenta(ordenVenta);
		listDetalleOrdenProducto = detalleOrdenProductoRepository
				.findAllActivasByOrdenVenta(ordenVenta);

		calcularTotalServicio();
		calcularTotals();
	}

	public void actualizarCantidad(DetalleOrdenServicio detalleOrden) {
		try {
			log.info("Ingreso a actualizarCantidad");
			detalleOrden.setTotal(detalleOrden.getCantidad()
					* detalleOrden.getPrecio());
			detalleOrdenServicioRegistration.update(detalleOrden);
			calcularTotalServicio();
			calcularTotals();
			ordenVenta.setTotal(totalServicio + totals);
			ordenVenta = ordenVentaRegistration.update(ordenVenta);
		} catch (Exception e) {
			log.error("Error en actualizarCantidad : " + e.getMessage());
		}
	}

	public void actualizarCantidadProducto(DetalleOrdenProducto detalleOrden) {
		try {
			log.info("Ingreso a actualizarCantidad");
			detalleOrden.setTotal(detalleOrden.getCantidad()
					* detalleOrden.getPrecio());
			detalleOrdenProductoRegistration.update(detalleOrden);
			calcularTotalServicio();
			calcularTotals();
			ordenVenta.setTotal(totalServicio + totals);
			ordenVenta = ordenVentaRegistration.update(ordenVenta);
		} catch (Exception e) {
			log.error("Error en actualizarCantidad : " + e.getMessage());
		}
	}

	private void calcularTotalServicio() {
		totalServicio = 0;
		estadoFinalizado = false;
		cantservicio = 0;
		for (DetalleOrdenServicio detalleOrdenServicio : listDetalleOrdenServicio) {
			totalServicio += detalleOrdenServicio.getTotal();
			if (detalleOrdenServicio.isAsignado()) {
				cantservicio++;
			}
		}
		verificarEstadoFinalizado();
	}

	public void cambiarEstadoTerminado() {
		try {
			log.info("Ingreso a cambiarEstadoTerminado");
			if (ordenVenta.getCliente() == null) {
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_ERROR,
						"Por favor asignar el Cliente", ordenVenta.getNombre());
				FacesContext.getCurrentInstance().addMessage(null, message);
				return;
			}
			ordenVenta.setEstado("PR");
			ordenVenta.setTipoTansaccion("NUEVO");
			ordenVenta = ordenVentaRegistration.update(ordenVenta);
			loadDefault();
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"La orden ha sido Procesado", ordenVenta.getNombre());
			FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			log.error("Error en cambiarEstadoTerminado : " + e.getMessage());
		}
	}

	// CLIENTE
	public List<Cliente> completeTextCliente(String query) {
		String upperQuery = query.toUpperCase();
		List<Cliente> results = new ArrayList<Cliente>();
		for (Cliente i : listCliente) {
			if (i.getNombre().toUpperCase().startsWith(upperQuery)) {
				results.add(i);
			}
		}
		return results;
	}

	public void onItemSelectCliente(SelectEvent event) {
		String nombre = event.getObject().toString();
		for (Cliente i : listCliente) {
			if (i.getNombre().equals(nombre)) {
				setCliente(i);
				ordenVenta.setCliente(cliente);
				ordenVentaRegistration.update(ordenVenta);
				verificarEstadoFinalizado();
			}
		}
	}

	// EMPLEADO
	public List<Empleado> completeTextEmpleado(String query) {
		String upperQuery = query.toUpperCase();
		List<Empleado> results = new ArrayList<Empleado>();
		for (Empleado i : listEmpleado) {
			if (i.getNombre().toUpperCase().startsWith(upperQuery)) {
				results.add(i);
			}
		}
		return results;
	}

	

	public void onItemSelectEmpleado(SelectEvent event) {
		String nombre = event.getObject().toString();
		for (Empleado i : listEmpleado) {
			if (i.getNombre().equals(nombre)) {
				selectedEmpleado = i;
				System.out.println("Empleado Seleccionado"
						+ selectedEmpleado.getNombre());
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Empleado Seleccionado",
						selectedEmpleado.getNombre());
				FacesContext.getCurrentInstance().addMessage(null, message);

			}
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

		calcularTotalServicio();
		calcularTotals();

		listProducto = productoRepository.findAllActivasNotList(empresaLogin,
				ordenVenta);
		listServicio = servicioRepository.findAllActivasNotList(empresaLogin,
				ordenVenta);
		log.info("termino onRowSelectOrdenVenta");
	}

	// AGREGACION DE PRODUCTO

	private void verificarEstadoFinalizado() {
		log.info("Ingreso a verificarEstadoFinalizado : " + estadoFinalizado
				+ " , " + cantproducto + " , " + cantservicio);

		if (cantproducto == listDetalleOrdenProducto.size()
				&& cantservicio == listDetalleOrdenServicio.size()
				&& ordenVenta.getCliente() != null) {
			estadoFinalizado = true;
			log.info("cambiado estadoFinalizado : " + estadoFinalizado);
		}
	}

	private void calcularTotals() {
		setTotals(0);
		for (DetalleOrdenProducto detalleOrdenProducto : listDetalleOrdenProducto) {
			totals += detalleOrdenProducto.getTotal();
			if (detalleOrdenProducto.isAsignado()) {
				cantproducto++;
			}
		}
		verificarEstadoFinalizado();

	}

	// EMPLEADO
	public List<Producto> completeTextProducto(String query) {
		String upperQuery = query.toUpperCase();
		List<Producto> results = new ArrayList<Producto>();
		for (Producto i : listProducto) {
			if (i.getNombre().toUpperCase().startsWith(upperQuery) || i.getTipoProducto().getNombre().toUpperCase().startsWith(upperQuery)) {
				results.add(i);
			}
		}
		return results;
	}

	public void onItemSelectProducto(SelectEvent event) {
		String nombre = event.getObject().toString();
		for (Producto i : listProducto) {
			if (i.getNombre().equals(nombre)) {
				selectedProducto = i;
				System.out.println("Producto Seleccionado"
						+ selectedProducto.getNombre());
				precio = selectedProducto.getPrecioVenta();
				cantidad = 1;
				this.nombre = i.getTipoProducto().getNombre()+" - "+ i.getNombre()+" "+i.getContenidoNeto()+i.getUnidadMedida().getNombre();
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Producto Seleccionado",
						selectedProducto.getNombre());
				FacesContext.getCurrentInstance().addMessage(null, message);
				calcularTotal();

			}
		}
	}

	public void calcularTotal() {
		try {
			log.info("Ingreso a calcularTotal");
			setTotal(cantidad * precio);
		} catch (Exception e) {
			log.error("Error en calcularTotal : " + e.getMessage());
		}
	}

	public void eliminarProducto(DetalleOrdenProducto detalleOrdenProducto) {
		try {
			log.info("Ingreso a eliminarProducto");
			detalleOrdenProducto.setEstado("RM");
			detalleOrdenProductoRegistration.update(detalleOrdenProducto);

			listDetalleOrdenProducto = detalleOrdenProductoRepository
					.findAllActivasByOrdenVenta(ordenVenta);
			// Actualiza Orden de Venta
			calcularTotals();
			calcularTotalServicio();
			ordenVenta.setTotal(totalServicio + totals);
			ordenVenta = ordenVentaRegistration.update(ordenVenta);
			listProducto = productoRepository.findAllActivasNotList(
					empresaLogin, ordenVenta);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Fue Eliminado correctamente",
					detalleOrdenProducto.getNombre());
			FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			log.error("Error en eliminarProducto : " + e.getMessage());
		}
	}

	// AGREGAR PRODUCTO

	private void agregarProducto() {
		try {
			log.info("Ingreso a agregarProducto");
			if (textoAutoProducto.isEmpty() || cantidad == 0 || precio == 0
					|| total == 0) {
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_INFO,
						"Revise los campos debe ser llenado correctamente...",
						selectedProducto.getNombre());
				FacesContext.getCurrentInstance().addMessage(null, message);
				return;
			}
			DetalleOrdenProducto detalleOrdenProducto = new DetalleOrdenProducto();
			detalleOrdenProducto.setCantidad(cantidad);
			detalleOrdenProducto.setPrecio(precio);
			detalleOrdenProducto.setNombre(nombre);
			detalleOrdenProducto.setOrdenVenta(ordenVenta);
			detalleOrdenProducto.setTotal(total);
			detalleOrdenProducto.setFechaRegistro(new Date());
			detalleOrdenProducto.setUsuarioRegistro(nombreUsuario);
			detalleOrdenProducto.setVendedor(null);
			detalleOrdenProducto.setProducto(selectedProducto);

			// Registra Detalle
			detalleOrdenProductoRegistration.create(detalleOrdenProducto);
			listDetalleOrdenProducto = detalleOrdenProductoRepository
					.findAllActivasByOrdenVenta(ordenVenta);

			clearFields();
			calcularTotalServicio();
			calcularTotals();
			// Actualiza Orden de Venta
			ordenVenta.setTotal(totalServicio + totals);
			ordenVentaRegistration.update(ordenVenta);
			listProducto = productoRepository.findAllActivasNotList(
					empresaLogin, ordenVenta);
		} catch (Exception e) {
			log.error("Error en agregarProducto: " + e.getLocalizedMessage());
		}
	}

	private void clearFields() {
		cantidad = 0;
		precio = 0;
		textoAutoProducto = "";
		nombre="";
		total = 0;
		selectedProducto = new Producto();
	}

	// BUSQUEDA DE SERVICIO Y AGREGACION
	public List<Servicio> completeTextServicio(String query) {
		String upperQuery = query.toUpperCase();
		List<Servicio> results = new ArrayList<Servicio>();
		for (Servicio i : listServicio) {
			if (i.getNombre().toUpperCase().startsWith(upperQuery) || i.getTipoServicio().getNombre().toUpperCase().startsWith(upperQuery)) {
				results.add(i);
			}
		}
		return results;
	}

	public void onItemSelectServicio(SelectEvent event) {
		String nombre = event.getObject().toString();
		for (Servicio i : listServicio) {
			if (i.getNombre().equals(nombre.toUpperCase())) {
				selectedServicio = i;
				log.info("Servicio Seleccionado"
						+ selectedServicio.getNombre());
				precioServicio = selectedServicio.getPrecioReferencial();
				cantidadServicio = 1;
				this.nombre =i.getTipoServicio().getNombre()+" - "+ i.getNombre();
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Servicio Seleccionado",
						selectedProducto.getNombre());
				FacesContext.getCurrentInstance().addMessage(null, message);
				calculatesTotalServicio();

			}
		}
	}

	public void calculatesTotalServicio() {
		try {
			log.info("Ingreso a calcularTotal");
			setTotalsServicio(cantidadServicio * precioServicio);
		} catch (Exception e) {
			log.error("Error en calcularTotal : " + e.getMessage());
		}
	}

	// AGREGAR PRODUCTO

	private void agregarServicio() {
		try {
			log.info("Ingreso a agregarProducto");
			if (textoAutoServicio.isEmpty() || cantidadServicio == 0
					|| precioServicio == 0 || totalsServicio == 0) {
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_INFO,
						"Revise los campos debe ser llenado correctamente...",
						selectedProducto.getNombre());
				FacesContext.getCurrentInstance().addMessage(null, message);
				return;
			}
			DetalleOrdenServicio detalleOrdenServicio = new DetalleOrdenServicio();
			detalleOrdenServicio.setCantidad(cantidadServicio);
			detalleOrdenServicio.setPrecio(precioServicio);
			detalleOrdenServicio.setNombre(nombre);
			detalleOrdenServicio.setOrdenVenta(ordenVenta);
			detalleOrdenServicio.setTotal(totalsServicio);
			detalleOrdenServicio.setFechaRegistro(new Date());
			detalleOrdenServicio.setUsuarioRegistro(nombreUsuario);
			detalleOrdenServicio.setVendedor(null);
			detalleOrdenServicio.setServicios(selectedServicio);

			// Registra Detalle
			detalleOrdenServicioRegistration.create(detalleOrdenServicio);
			listDetalleOrdenServicio = detalleOrdenServicioRepository
					.findAllActivasByOrdenVenta(ordenVenta);

			clearFieldsServicio();
			calcularTotalServicio();
			calcularTotals();

			// Actualiza Orden de Venta
			ordenVenta.setTotal(totalServicio + totals);
			ordenVenta = ordenVentaRegistration.update(ordenVenta);
			listServicio = servicioRepository.findAllActivasNotList(
					empresaLogin, ordenVenta);
		} catch (Exception e) {
			log.error("Error en agregarProducto: " + e.getLocalizedMessage());
		}
	}

	private void clearFieldsServicio() {
		cantidadServicio = 0;
		precioServicio = 0;
		textoAutoServicio = "";
		nombre="";
		totalsServicio = 0;
		selectedServicio = new Servicio();
	}

	public void eliminarServicio(DetalleOrdenServicio detalleOrden) {
		try {
			log.info("Ingreso a eliminarProducto");
			detalleOrden.setEstado("RM");
			detalleOrdenServicioRegistration.update(detalleOrden);

			listDetalleOrdenServicio = detalleOrdenServicioRepository
					.findAllActivasByOrdenVenta(ordenVenta);
			calcularTotalServicio();
			calcularTotals();
			// Actualiza Orden de Venta
			ordenVenta.setTotal(totalServicio + totals);
			ordenVenta = ordenVentaRegistration.update(ordenVenta);
			listServicio = servicioRepository.findAllActivasNotList(
					empresaLogin, ordenVenta);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Fue Eliminado correctamente",
					detalleOrdenServicio.getNombre());
			FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			log.error("Error en eliminarProducto : " + e.getMessage());
		}
	}
	
	public void resetearFitrosTabla(String id) {
		DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
		table.setSelection(null);
		table.reset();
	}
	
	
	private @Inject NitClienteRegistration nitClienteRegistration;
	public void registrarCliente(){
		try {
			log.info("Ingreso a registrarCliente()");
			String estado = nombreEstado.equals("ACTIVO")?"AC":"IN";
			String credito = permitirCredito?"SI":"NO";
			newClientes.setPermitirCredito(credito);
			newClientes.setEstado(estado);
			newClientes.setFechaRegistro(new Date());
			newClientes.setUsuarioRegistro(nombreUsuario);
			newClientes.setEmpresa(empresaLogin);
			if(!newClientes.validateSpaDate(facesContext, empresaLogin, gestionLogin)){
				resetearFitrosTabla("formModalCliente:dataTableCliente");
				return;
			}
			newClientes=clientesRegistration.create(newClientes);
			
			if (newClientes.getNit().trim().length()>0) {
				NitCliente nitCliente= new NitCliente();
				nitCliente.setCliente(newClientes);
				nitCliente.setNit(newClientes.getCi());
				nitCliente.setUsuarioRegistro(nombreUsuario);
				nitClienteRegistration.create(nitCliente);
				
			}
			ordenVenta.setCliente(newClientes);
			ordenVentaRegistration.update(ordenVenta);
			textoAutoCliente=ordenVenta.getCliente().getNombre();			
		} catch (Exception e) {
			log.error("Error en registrarCliente() : "+e.getMessage());
		}
	}

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

	public boolean isEstadoButtonDialog() {
		return estadoButtonDialog;
	}

	public void setEstadoButtonDialog(boolean estadoButtonDialog) {
		this.estadoButtonDialog = estadoButtonDialog;
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

	public List<Empleado> getListEmpleado() {
		return listEmpleado;
	}

	public void setListEmpleado(List<Empleado> listEmpleado) {
		this.listEmpleado = listEmpleado;
	}

	public Sucursal getSucursalLogin() {
		return sucursalLogin;
	}

	public void setSucursalLogin(Sucursal sucursalLogin) {
		this.sucursalLogin = sucursalLogin;
	}

	public Empleado getSelectedEmpleado() {
		return selectedEmpleado;
	}

	public void setSelectedEmpleado(Empleado selectedEmpleado) {
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

	public double getTotalServicio() {
		return totalServicio;
	}

	public void setTotalServicio(double totalServicio) {
		this.totalServicio = totalServicio;
	}

	public boolean isEstadoFinalizado() {
		return estadoFinalizado;
	}

	public void setEstadoFinalizado(boolean estadoFinalizado) {
		this.estadoFinalizado = estadoFinalizado;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		this.precio = precio;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public List<Producto> getListProducto() {
		return listProducto;
	}

	public void setListProducto(List<Producto> listProducto) {
		this.listProducto = listProducto;
	}

	public Producto getSelectedProducto() {
		return selectedProducto;
	}

	public void setSelectedProducto(Producto selectedProducto) {
		this.selectedProducto = selectedProducto;
	}

	public String getTextoAutoProducto() {
		return textoAutoProducto;
	}

	public void setTextoAutoProducto(String textoAutoProducto) {
		this.textoAutoProducto = textoAutoProducto;
	}

	public int getCantidadServicio() {
		return cantidadServicio;
	}

	public void setCantidadServicio(int cantidadServicio) {
		this.cantidadServicio = cantidadServicio;
	}

	public double getPrecioServicio() {
		return precioServicio;
	}

	public void setPrecioServicio(double precioServicio) {
		this.precioServicio = precioServicio;
	}

	public double getTotalsServicio() {
		return totalsServicio;
	}

	public void setTotalsServicio(double totalsServicio) {
		this.totalsServicio = totalsServicio;
	}

	public Servicio getSelectedServicio() {
		return selectedServicio;
	}

	public void setSelectedServicio(Servicio selectedServicio) {
		this.selectedServicio = selectedServicio;
	}

	public List<Servicio> getListServicio() {
		return listServicio;
	}

	public void setListServicio(List<Servicio> listServicio) {
		this.listServicio = listServicio;
	}

	public String getTextoAutoServicio() {
		return textoAutoServicio;
	}

	public void setTextoAutoServicio(String textoAutoServicio) {
		this.textoAutoServicio = textoAutoServicio;
	}

	public String getSelectedVista() {
		return selectedVista;
	}

	public void setSelectedVista(String selectedVista) {
		this.selectedVista = selectedVista;
	}

	public DetalleOrdenProducto getSelectedDetalleOrdenProducto() {
		return selectedDetalleOrdenProducto;
	}

	public void setSelectedDetalleOrdenProducto(
			DetalleOrdenProducto selectedDetalleOrdenProducto) {
		this.selectedDetalleOrdenProducto = selectedDetalleOrdenProducto;
	}

	public int getCantservicio() {
		return cantservicio;
	}

	public void setCantservicio(int cantservicio) {
		this.cantservicio = cantservicio;
	}

	public int getCantproducto() {
		return cantproducto;
	}

	public void setCantproducto(int cantproducto) {
		this.cantproducto = cantproducto;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Cliente getNewClientes() {
		return newClientes;
	}

	public void setNewClientes(Cliente newClientes) {
		this.newClientes = newClientes;
	}

	public String getClienteEstado() {
		return clienteEstado;
	}

	public void setClienteEstado(String clienteEstado) {
		this.clienteEstado = clienteEstado;
	}

	public boolean isPermitirCredito() {
		return permitirCredito;
	}

	public void setPermitirCredito(boolean permitirCredito) {
		this.permitirCredito = permitirCredito;
	}

}
