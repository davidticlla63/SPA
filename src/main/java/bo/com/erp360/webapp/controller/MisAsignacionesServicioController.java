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
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.DetalleOrdenProductoRepository;
import bo.com.erp360.webapp.data.DetalleOrdenServicioRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.ProductoRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.DetalleOrdenProducto;
import bo.com.erp360.webapp.model.DetalleOrdenServicio;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.DetalleOrdenProductoRegistration;
import bo.com.erp360.webapp.service.DetalleOrdenServicioRegistration;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.OrdenVentaRegistration;

@Named(value = "misAsignacionesServicioController")
@SuppressWarnings("serial")
@ConversationScoped
public class MisAsignacionesServicioController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;

	private @Inject UsuarioRepository usuarioRepository;

	// DetalleOrdenServicios
	private List<DetalleOrdenServicio> listDetalleOrdenServicio = new ArrayList<DetalleOrdenServicio>();

	private DetalleOrdenServicio detalleOrdenServicio;

	private DetalleOrdenServicio selectedDetalleOrdenServicio;

	private @Inject DetalleOrdenServicioRepository detalleOrdenServicioRepository;

	// detalle orden de producto

	private List<DetalleOrdenProducto> listDetalleOrdenProducto = new ArrayList<DetalleOrdenProducto>();
	private @Inject DetalleOrdenProductoRepository detalleOrdenProductoRepository;

	Logger log = Logger.getLogger(MisAsignacionesServicioController.class);

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

	// Empleado

	private List<Producto> listProducto = new ArrayList<Producto>();

	private @Inject ProductoRepository productoRepository;

	private @Inject DetalleOrdenProductoRegistration detalleOrdenProductoRegistration;

	private Date fecha;

	private @Inject OrdenVentaRegistration ordenVentaRegistration;

	private @Inject DetalleOrdenServicioRegistration detalleOrdenServicioRegistration;

	private Producto selectedProducto = new Producto();

	private int cantidad = 1;
	private double precio = 0;
	private double total = 0;
	private double totals = 0;

	private String textoAutoProducto;// AUTOCOMPLETE

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
		fecha = new Date();
		loadDefault();

	}

	public void loadDefault() {
		modificar = false;
		estadoButtonDialog = true;
		tipoColumnTable = "col-md-12";
		listProducto = productoRepository.findAllActivas(empresaLogin);
		listDetalleOrdenServicio.clear();
		consultarOrdenVentaPorFecha();
		cantidad = 1;
		precio = 0;
		total = 0;
		
	}

	public void consultarOrdenVentaPorFecha() {
		listDetalleOrdenServicio = detalleOrdenServicioRepository
				.findAllActivasForDate(empresaLogin, sucursalLogin, usuario,
						fecha);
	}

	private void calcularTotals() {
		setTotals(0);
		for (DetalleOrdenProducto detalleOrdenProducto : listDetalleOrdenProducto) {
			totals += detalleOrdenProducto.getTotal();
		}
	}

	// EMPLEADO
	public List<Producto> completeTextProducto(String query) {
		String upperQuery = query.toUpperCase();
		List<Producto> results = new ArrayList<Producto>();
		for (Producto i : listProducto) {
			if (i.getNombre().toUpperCase().startsWith(upperQuery)) {
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
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_INFO, "Empleado Seleccionado",
						selectedProducto.getNombre());
				FacesContext.getCurrentInstance().addMessage(null, message);

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
			detalleOrdenProducto.setNombre(textoAutoProducto);
			detalleOrdenProducto.setOrdenVenta(detalleOrdenServicio
					.getOrdenVenta());
			detalleOrdenProducto.setTotal(total);
			detalleOrdenProducto.setFechaRegistro(new Date());
			detalleOrdenProducto.setUsuarioRegistro(nombreUsuario);
			detalleOrdenProducto.setVendedor(null);
			detalleOrdenProducto.setProducto(selectedProducto);

			// Registra Detalle
			detalleOrdenProductoRegistration.create(detalleOrdenProducto);
			listDetalleOrdenProducto = detalleOrdenProductoRepository
					.findAllActivasByOrdenVenta(
							detalleOrdenServicio.getOrdenVenta(), usuario);
			// Actualiza Orden de Venta
			detalleOrdenServicio.getOrdenVenta().setTotal(
					detalleOrdenServicio.getOrdenVenta().getTotal()
							+ detalleOrdenProducto.getTotal());
			ordenVentaRegistration.update(detalleOrdenServicio.getOrdenVenta());
			clearFields();
			calcularTotals();
		} catch (Exception e) {
			log.error("Error en agregarProducto: " + e.getLocalizedMessage());
		}
	}

	private void eliminarProducto(DetalleOrdenProducto detalleOrdenProducto) {
		try {
			log.info("Ingreso a eliminarProducto");
			detalleOrdenProducto.setEstado("RM");
			detalleOrdenProductoRegistration.update(detalleOrdenProducto);
			// Actualiza Orden de Venta
			detalleOrdenServicio.getOrdenVenta().setTotal(
					detalleOrdenServicio.getOrdenVenta().getTotal()
							- detalleOrdenProducto.getTotal());
			ordenVentaRegistration.update(detalleOrdenServicio.getOrdenVenta());
			listDetalleOrdenProducto= detalleOrdenProductoRepository.findAllActivasByOrdenVenta(detalleOrdenServicio.getOrdenVenta(), usuario);
			calcularTotals();
			FacesMessage message = new FacesMessage(
					FacesMessage.SEVERITY_INFO,
					"Fue Eliminado correctamente",
					detalleOrdenProducto.getNombre());
			FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			log.error("Error en eliminarProducto : " + e.getMessage());
		}
	}

	private void clearFields() {
		cantidad = 0;
		precio = 0;
		textoAutoProducto = "";
		total = 0;
		selectedProducto = new Producto();
	}

	public void cambioEstado(String estado) {
		try {
			log.info("Ingreso a cambioEstado ..");
			detalleOrdenServicio.setProceso(estado);
			detalleOrdenServicioRegistration.update(detalleOrdenServicio);
		} catch (Exception e) {
			log.error("Error en cambioEstado : " + e.getMessage());
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
		listDetalleOrdenProducto = detalleOrdenProductoRepository
				.findAllActivasByOrdenVenta(
						detalleOrdenServicio.getOrdenVenta(), usuario);
		calcularTotals();
		modificar = true;
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

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public DetalleOrdenServicio getDetalleOrdenServicio() {
		return detalleOrdenServicio;
	}

	public void setDetalleOrdenServicio(
			DetalleOrdenServicio detalleOrdenServicio) {
		this.detalleOrdenServicio = detalleOrdenServicio;
	}

	public Sucursal getSucursalLogin() {
		return sucursalLogin;
	}

	public void setSucursalLogin(Sucursal sucursalLogin) {
		this.sucursalLogin = sucursalLogin;
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

	public double getTotals() {
		return totals;
	}

	public void setTotals(double totals) {
		this.totals = totals;
	}

}
