package bo.com.erp360.webapp.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.DetalleOrdenProductoRepository;
import bo.com.erp360.webapp.data.DetalleOrdenServicioRepository;
import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.OrdenVentaRepository;
import bo.com.erp360.webapp.data.ProductoRepository;
import bo.com.erp360.webapp.data.ServicioRepository;
import bo.com.erp360.webapp.data.SucursalRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.DetalleOrdenProducto;
import bo.com.erp360.webapp.model.DetalleOrdenServicio;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.OrdenVenta;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.Servicio;
import bo.com.erp360.webapp.model.Sucursal;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.DetalleOrdenProductoRegistration;
import bo.com.erp360.webapp.service.DetalleOrdenServicioRegistration;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.OrdenVentaRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "generarTicketsController")
@SuppressWarnings("serial")
@ConversationScoped
public class GenerarTicketsController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	private @Inject EmpresaRepository empresaRepository;

	private @Inject OrdenVentaRegistration ordenVentaRegistration;

	private @Inject OrdenVentaRepository ordenVentaRepository;

	private @Inject UsuarioRepository usuarioRepository;

	private String urlTicket;

	private String selectedVista = "servicio";

	private HttpServletRequest request;

	// Servicios
	private List<Servicio> listServicios = new ArrayList<Servicio>();
	private @Inject ServicioRepository serviciosRepository;
	private Servicio[] selectedServicios;

	// DetalleOrdenServicios
	private List<DetalleOrdenServicio> listDetalleOrdenServicio = new ArrayList<DetalleOrdenServicio>();
	private @Inject DetalleOrdenServicioRepository detalleOrdenServicioRepository;
	private DetalleOrdenServicio[] selectedDetalleOrdenServicio;
	private @Inject DetalleOrdenServicioRegistration detalleOrdenServicioRegistration;

	// Producto

	private List<Producto> listProducto = new ArrayList<Producto>();
	private @Inject ProductoRepository productoRepository;
	private Producto[] selectedProducto;

	// DetalleOrdenPruducto
	private List<DetalleOrdenProducto> listDetalleOrdenPruducto = new ArrayList<DetalleOrdenProducto>();
	private @Inject DetalleOrdenProductoRepository detalleOrdenProductoRepository;
	private DetalleOrdenProducto[] selectedDetalleOrdenProducto;
	private @Inject DetalleOrdenProductoRegistration detalleOrdenProductoRegistration;

	Logger log = Logger.getLogger(GenerarTicketsController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventOrdenVenta;

	// estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean estadoButtonDialog;

	private String nombreEstado = "ACTIVO";
	private String tipoColumnTable; // 8

	private List<Producto> listFilterProducto;
	private String[] listEstado = { "ACTIVO", "INACTIVO" };
	private String[] listResolucionNormativa = { "NSF-07", "SFV-14" };

	private OrdenVenta newOrdenVenta;

	// login
	private Usuario usuario;
	private String nombreUsuario;
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	private Date fechaMinima;

	private Sucursal sucursalLogin;

	private @Inject SucursalRepository sucursalRepository;

	private double total = 0;
	private double totalProducto = 0;

	@PostConstruct
	public void initNewProducto() {
		log.info(" init new initNewProducto controller");
		beginConversation();

		request = (HttpServletRequest) facesContext.getExternalContext()
				.getRequest();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		usuario = estadoUsuarioLogin.getUsuarioSession(usuarioRepository);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);
		sucursalLogin = estadoUsuarioLogin.getSucursalSession(
				empresaRepository, sucursalRepository);
		fechaMinima = new Date();
		loadDefault();
	}

	public void loadDefault() {
		crear = true;
		registrar = false;
		modificar = false;
		estadoButtonDialog = true;
		tipoColumnTable = "col-md-12";
		newOrdenVenta = new OrdenVenta();

		listDetalleOrdenServicio.clear();
		listDetalleOrdenPruducto.clear();
		selectedDetalleOrdenProducto = null;
		selectedProducto = null;
		selectedDetalleOrdenServicio = null;
		selectedServicios = null;
		selectedVista = "servicio";

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

	public void nuevaOrden() {
		try {
			log.info("Ingreso a nuevaOrden");
			newOrdenVenta = new OrdenVenta();
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			newOrdenVenta.setEstado(estado);
			newOrdenVenta.setUsuarioRegistro(nombreUsuario);
			newOrdenVenta.setFechaRegistro(new Date());
			newOrdenVenta.setEmpresa(empresaLogin);
			newOrdenVenta.setSucursal(sucursalLogin);
			// traer todos las sevicioses
			listServicios = serviciosRepository
					.findAllActivosByEmpresa(empresaLogin);
			listProducto = productoRepository.findAllActivas(empresaLogin);
			crear = false;
		} catch (Exception e) {
			log.error("Error en nuevaOrden :  " + e.getMessage());
		}

	}

	// ----- metodos sevicios ---------------

	public void registrarProducto() {
		try {

			newOrdenVenta.setNumeroSecuencia(ordenVentaRepository
					.findCorrelativoDiaria(empresaLogin, sucursalLogin,
							new Date()));
			newOrdenVenta = ordenVentaRegistration.create(newOrdenVenta);

			for (DetalleOrdenServicio detalleOrdenServicio : listDetalleOrdenServicio) {
				detalleOrdenServicio.setFechaRegistro(new Date());
				detalleOrdenServicio.setUsuarioRegistro(nombreUsuario);
				detalleOrdenServicio.setOrdenVenta(newOrdenVenta);
				detalleOrdenServicioRegistration.create(detalleOrdenServicio);
			}

			for (DetalleOrdenProducto detalleOrdenProducto : listDetalleOrdenPruducto) {
				detalleOrdenProducto.setFechaRegistro(new Date());
				detalleOrdenProducto.setNombre(detalleOrdenProducto
						.getProducto().getTipoProducto().getNombre()
						+ " - "
						+ detalleOrdenProducto.getProducto().getNombre()
						+ " "
						+ detalleOrdenProducto.getProducto().getContenidoNeto()
						+ detalleOrdenProducto.getProducto().getUnidadMedida()
								.getNombre());
				detalleOrdenProducto.setUsuarioRegistro(nombreUsuario);
				detalleOrdenProducto.setOrdenVenta(newOrdenVenta);
				detalleOrdenProductoRegistration.create(detalleOrdenProducto);
			}

			FacesUtil.infoMessage("Producto Registrada!",
					newOrdenVenta.getNombre());
			armarURLTicket();
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgVistaPreviaTicket').show();");
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo registrar la sevicios.!");
		}
	}

	public void modificarProducto() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			newOrdenVenta.setEstado(estado);
			newOrdenVenta.setEmpresa(empresaLogin);
			ordenVentaRegistration.update(newOrdenVenta);

			FacesUtil.infoMessage("Producto Modificada!",
					newOrdenVenta.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo modificar la sevicios.!");
		}
	}

	public void eliminarProducto() {
		try {
			newOrdenVenta.setEstado("RM");
			ordenVentaRegistration.update(newOrdenVenta);

			FacesUtil.infoMessage("Producto Eliminada!",
					newOrdenVenta.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo eliminar la sevicios.!");
		}

	}

	public void totalCalulated() {
		total = 0;
		totalProducto = 0;
		for (DetalleOrdenServicio value : listDetalleOrdenServicio) {
			total += value.getPrecio() * value.getCantidad();
			value.setTotal(value.getPrecio() * value.getCantidad());

		}

		for (DetalleOrdenProducto value : listDetalleOrdenPruducto) {
			totalProducto += value.getPrecio() * value.getCantidad();
			value.setTotal(value.getPrecio() * value.getCantidad());

		}
		newOrdenVenta.setTotal(total + totalProducto);
	}

	// ADICIONAR SERVICIOS SELECCIONADOS
	public void add() {
		for (Servicio value : selectedServicios) {
			DetalleOrdenServicio detalleOrdenServicio = new DetalleOrdenServicio();
			detalleOrdenServicio.setServicios(value);
			detalleOrdenServicio.setNombre(value.getNombre());
			detalleOrdenServicio.setPrecio(value.getPrecioReferencial());
			detalleOrdenServicio.setCantidad(1);
			listDetalleOrdenServicio.add(detalleOrdenServicio);
			removeItemServicio(value);
		}
		totalCalulated();
	}

	// ADICIONAR TODO LOS SERVICIOS
	public void addAll() {
		for (Servicio value : listServicios) {
			DetalleOrdenServicio detalleOrdenServicio = new DetalleOrdenServicio();
			detalleOrdenServicio.setServicios(value);
			detalleOrdenServicio.setNombre(value.getNombre());
			detalleOrdenServicio.setPrecio(value.getPrecioReferencial());
			detalleOrdenServicio.setCantidad(1);
			listDetalleOrdenServicio.add(detalleOrdenServicio);

		}
		listServicios.clear();
		totalCalulated();
	}

	// elimina un item de la lista de Servicios
	private void removeItemServicio(Servicio servicios) {
		for (int i = 0; i < listServicios.size(); i++) {
			if (listServicios.get(i).getId().doubleValue() == servicios.getId()
					.doubleValue()) {
				listServicios.remove(i);
				return;
			}
		}
	}

	// elimina un item de la lista de detalle Orden Servicios
	private void removeItemOrdenServicio(DetalleOrdenServicio servicios) {
		for (int i = 0; i < listDetalleOrdenServicio.size(); i++) {
			if (listDetalleOrdenServicio.get(i).getServicios().getId()
					.doubleValue() == servicios.getId().doubleValue()) {
				listDetalleOrdenServicio.remove(i);
				selectedDetalleOrdenServicio = null;
				return;
			}
		}
	}

	// REMOVER SERVICIOS SELECCIONADOS
	public void remove() {
		for (DetalleOrdenServicio value : selectedDetalleOrdenServicio) {
			removeItemOrdenServicio(value);
		}
		totalCalulated();
	}

	// ADICIONAR TODO LOS SERVICIOS
	public void removeAll() {
		listServicios = serviciosRepository
				.findAllActivosByEmpresa(empresaLogin);
		listDetalleOrdenServicio.clear();
		totalCalulated();
	}

	// ADICIONAR PRODUCTO SELECCIONADOS
	public void addProdcucto() {
		for (Producto value : selectedProducto) {
			DetalleOrdenProducto detalleOrdenProducto = new DetalleOrdenProducto();
			detalleOrdenProducto.setProducto(value);
			detalleOrdenProducto.setNombre(value.getNombre());
			detalleOrdenProducto.setPrecio(value.getPrecioVenta());
			detalleOrdenProducto.setCantidad(1);
			listDetalleOrdenPruducto.add(detalleOrdenProducto);
			removeItemProducto(value);
		}
		totalCalulated();
	}

	// ADICIONAR TODO LOS SERVICIOS
	public void addAllProdcucto() {
		for (Producto value : listProducto) {
			DetalleOrdenProducto detalleOrdenProducto = new DetalleOrdenProducto();
			detalleOrdenProducto.setProducto(value);
			detalleOrdenProducto.setNombre(value.getNombre());
			detalleOrdenProducto.setPrecio(value.getPrecioVenta());
			detalleOrdenProducto.setCantidad(1);
			listDetalleOrdenPruducto.add(detalleOrdenProducto);

		}
		listProducto.clear();
		totalCalulated();
	}

	// elimina un item de la lista de Servicios
	private void removeItemProducto(Producto servicios) {
		for (int i = 0; i < listProducto.size(); i++) {
			if (listProducto.get(i).getId().doubleValue() == servicios.getId()
					.doubleValue()) {
				listProducto.remove(i);
				return;
			}
		}
	}

	// elimina un item de la lista de detalle Orden Servicios
	private void removeItemOrdenProducto(DetalleOrdenProducto servicios) {
		for (int i = 0; i < listDetalleOrdenPruducto.size(); i++) {
			if (listDetalleOrdenPruducto.get(i).getProducto().getId()
					.doubleValue() == servicios.getId().doubleValue()) {
				listDetalleOrdenPruducto.remove(i);
				selectedDetalleOrdenProducto = null;
				return;
			}
		}
	}

	// REMOVER SERVICIOS SELECCIONADOS
	public void removeProducto() {
		for (DetalleOrdenProducto value : selectedDetalleOrdenProducto) {
			removeItemOrdenProducto(value);
		}
		totalCalulated();
	}

	// ADICIONAR TODO LOS SERVICIOS
	public void removeAllProducto() {
		listProducto = productoRepository.findAllActivas(empresaLogin);
		listDetalleOrdenPruducto.clear();
		totalCalulated();
	}

	// -------- acciones para la vista----------

	public void cambiarAspecto() {
		crear = false;
		registrar = true;
		modificar = false;

	}

	/**
	 * 
	 * @return
	 */

	public void armarURLTicket() {
		try {
			log.info("Ingreso a armarURLTicket...");

			String urlPath = request.getRequestURL().toString();
			urlPath = urlPath.substring(0, urlPath.length()
					- request.getRequestURI().length())
					+ request.getContextPath() + "/";
			log.info("urlPath >> " + urlPath);
			// CAPTURANDO PARAMETROS

			String urlLogo = urlPath + "resources/gfx/logoERP.PNG";
			urlTicket = urlPath + "ReporteTicket?pIdOrden="
					+ this.newOrdenVenta.getId() + "&pEmpresa="
					+ empresaLogin.getRazonSocial() + "&pCiudad="
					+ empresaLogin.getCiudad() + "&pPais=BOLIVIA&pLogo="
					+ urlLogo;

			log.info("URL Reporte urlTicket: " + urlTicket);

		} catch (Exception e) {
			// TODO: handle exception
			log.info("Error en armarURLTicket: " + e.getMessage());
		}
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

	public String[] getListResolucionNormativa() {
		return listResolucionNormativa;
	}

	public void setListResolucionNormativa(String[] listResolucionNormativa) {
		this.listResolucionNormativa = listResolucionNormativa;
	}

	public Date getFechaMinima() {
		return fechaMinima;
	}

	public void setFechaMinima(Date fechaMinima) {
		this.fechaMinima = fechaMinima;
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

	public List<Servicio> getListServicios() {
		return listServicios;
	}

	public void setListServicios(List<Servicio> listServicios) {
		this.listServicios = listServicios;
	}

	public Servicio[] getSelectedServicios() {
		return selectedServicios;
	}

	public void setSelectedServicios(Servicio[] selectedServicios) {
		this.selectedServicios = selectedServicios;
	}

	public List<DetalleOrdenServicio> getListDetalleOrdenServicio() {
		return listDetalleOrdenServicio;
	}

	public void setListDetalleOrdenServicio(
			List<DetalleOrdenServicio> listDetalleOrdenServicio) {
		this.listDetalleOrdenServicio = listDetalleOrdenServicio;
	}

	public DetalleOrdenServicio[] getSelectedDetalleOrdenServicio() {
		return selectedDetalleOrdenServicio;
	}

	public void setSelectedDetalleOrdenServicio(
			DetalleOrdenServicio[] selectedDetalleOrdenServicio) {
		this.selectedDetalleOrdenServicio = selectedDetalleOrdenServicio;
	}

	public OrdenVenta getNewOrdenVenta() {
		return newOrdenVenta;
	}

	public void setNewOrdenVenta(OrdenVenta newOrdenVenta) {
		this.newOrdenVenta = newOrdenVenta;
	}

	public String getUrlTicket() {
		return urlTicket;
	}

	public void setUrlTicket(String urlTicket) {
		this.urlTicket = urlTicket;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public Sucursal getSucursalLogin() {
		return sucursalLogin;
	}

	public void setSucursalLogin(Sucursal sucursalLogin) {
		this.sucursalLogin = sucursalLogin;
	}

	public String getSelectedVista() {
		return selectedVista;
	}

	public void setSelectedVista(String selectedVista) {
		this.selectedVista = selectedVista;
	}

	public List<Producto> getListProducto() {
		return listProducto;
	}

	public void setListProducto(List<Producto> listProducto) {
		this.listProducto = listProducto;
	}

	public Producto[] getSelectedProducto() {
		return selectedProducto;
	}

	public void setSelectedProducto(Producto[] selectedProducto) {
		this.selectedProducto = selectedProducto;
	}

	public List<DetalleOrdenProducto> getListDetalleOrdenPruducto() {
		return listDetalleOrdenPruducto;
	}

	public void setListDetalleOrdenPruducto(
			List<DetalleOrdenProducto> listDetalleOrdenPruducto) {
		this.listDetalleOrdenPruducto = listDetalleOrdenPruducto;
	}

	public DetalleOrdenProducto[] getSelectedDetalleOrdenProducto() {
		return selectedDetalleOrdenProducto;
	}

	public void setSelectedDetalleOrdenProducto(
			DetalleOrdenProducto[] selectedDetalleOrdenProducto) {
		this.selectedDetalleOrdenProducto = selectedDetalleOrdenProducto;
	}

	public double getTotalProducto() {
		return totalProducto;
	}

	public void setTotalProducto(double totalProducto) {
		this.totalProducto = totalProducto;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

}
