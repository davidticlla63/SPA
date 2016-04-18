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
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.richfaces.cdi.push.Push;

import bo.com.erp360.webapp.data.EmpresaRepository;
import bo.com.erp360.webapp.data.GrupoProductoRepository;
import bo.com.erp360.webapp.data.LineaRepository;
import bo.com.erp360.webapp.data.ProductoRepository;
import bo.com.erp360.webapp.data.ProveedorRepository;
import bo.com.erp360.webapp.data.TipoProductoRepository;
import bo.com.erp360.webapp.data.UnidadMedidaRepository;
import bo.com.erp360.webapp.data.UsuarioRepository;
import bo.com.erp360.webapp.model.BienServicio;
import bo.com.erp360.webapp.model.Empresa;
import bo.com.erp360.webapp.model.GrupoProducto;
import bo.com.erp360.webapp.model.Linea;
import bo.com.erp360.webapp.model.NitCliente;
import bo.com.erp360.webapp.model.Producto;
import bo.com.erp360.webapp.model.Proveedor;
import bo.com.erp360.webapp.model.TipoProducto;
import bo.com.erp360.webapp.model.UnidadMedida;
import bo.com.erp360.webapp.model.Usuario;
import bo.com.erp360.webapp.service.EstadoUsuarioLogin;
import bo.com.erp360.webapp.service.GrupoProductoRegistration;
import bo.com.erp360.webapp.service.LineaRegistration;
import bo.com.erp360.webapp.service.ProductoRegistration;
import bo.com.erp360.webapp.service.ProveedorRegistration;
import bo.com.erp360.webapp.service.TipoProductoRegistration;
import bo.com.erp360.webapp.service.UnidadMedidaRegistration;
import bo.com.erp360.webapp.util.FacesUtil;

@Named(value = "productoSpaController")
@SuppressWarnings("serial")
@ConversationScoped
public class ProductoSpaController implements Serializable {

	public static final String PUSH_CDI_TOPIC = "pushCdi";

	@Inject
	private FacesContext facesContext;

	@Inject
	Conversation conversation;

	@Inject
	private EntityManager em;

	private @Inject EmpresaRepository empresaRepository;

	private @Inject ProductoRegistration productoRegistration;

	private @Inject ProductoRepository productoRepository;

	private @Inject UsuarioRepository usuarioRepository;

	Logger log = Logger.getLogger(ProductoSpaController.class);

	@Inject
	@Push(topic = PUSH_CDI_TOPIC)
	Event<String> pushEventProducto;

	// estados
	private boolean crear;
	private boolean registrar;
	private boolean modificar;
	private boolean seleccionadaDosificacion;
	private boolean estadoButtonDialog;

	private String nombreEstado = "ACTIVO";
	private String tipoColumnTable; // 8

	private List<Producto> listProducto;
	private List<Producto> listFilterProducto;
	private String[] listEstado = { "ACTIVO", "INACTIVO" };
	private String[] listResolucionNormativa = { "NSF-07", "SFV-14" };

	private Producto newProducto;
	private Producto selectedProducto;

	// login
	private Usuario usuario;
	private String nombreUsuario;
	private EstadoUsuarioLogin estadoUsuarioLogin;
	private Empresa empresaLogin;

	private Date fechaMinima;

	// tipo de Producto
	private List<TipoProducto> listTipoProducto = new ArrayList<TipoProducto>();
	private @Inject TipoProductoRepository tipoProductoRepository;

	// tipo de Producto
	private List<GrupoProducto> listGrupoProducto = new ArrayList<GrupoProducto>();
	private @Inject GrupoProductoRepository grupoProductoRepository;

	private List<Linea> listLinea = new ArrayList<Linea>();

	private @Inject LineaRepository lineaRepository;

	// tipo de Producto
	private List<Proveedor> listProveedor = new ArrayList<Proveedor>();
	private @Inject ProveedorRepository proveedorRepository;

	private @Inject ProveedorRegistration proveedorRegistration;

	private Proveedor proveedor;

	private String textoAutoCompleteGrupo;

	private TipoProducto tipoProducto;

	private @Inject TipoProductoRegistration tipoProductoRegistration;

	// DATOS DE LINEA PRODUCTO

	private Linea linea;
	private @Inject LineaRegistration lineaRegistration;

	// DATOS GRUPO DE PRODUCTOS

	private GrupoProducto grupoProducto;
	private @Inject GrupoProductoRegistration grupoProductoRegistration;

	// unidad de medida

	private String textoAutoCompleteMedida;

	private UnidadMedida unidadMedida;

	private List<UnidadMedida> listUnidadMedida = new ArrayList<UnidadMedida>();

	private @Inject UnidadMedidaRepository unidadMedidaRepository;

	private @Inject UnidadMedidaRegistration unidadMedidaRegistration;

	@PostConstruct
	public void initNewProducto() {
		log.info(" init new initNewProducto controller");
		beginConversation();
		estadoUsuarioLogin = new EstadoUsuarioLogin(facesContext);
		usuario = estadoUsuarioLogin.getUsuarioSession(usuarioRepository);
		setNombreUsuario(estadoUsuarioLogin.getNombreUsuarioSession());
		empresaLogin = estadoUsuarioLogin.getEmpresaSession(empresaRepository);

		fechaMinima = new Date();
		loadDefault();
	}

	public void loadDefault() {
		crear = true;
		registrar = false;
		modificar = false;
		seleccionadaDosificacion = false;
		estadoButtonDialog = true;
		tipoColumnTable = "col-md-12";
		linea = new Linea();
		newProducto = new Producto();
		selectedProducto = new Producto();

		// traer todos las productoes
		listProducto = productoRepository.findAllActivas(empresaLogin);

		setListLinea(lineaRepository.findAllActivasByEmpresa(empresaLogin));

		// traer todos los tipo de productos activos
		listTipoProducto = tipoProductoRepository
				.findAllActivasByEmpresa(empresaLogin);

		listGrupoProducto = grupoProductoRepository
				.findAllActivasByEmpresa(empresaLogin);
		proveedor = new Proveedor();
		listProveedor = proveedorRepository
				.findAllActivasByEmpresa(empresaLogin);
		textoAutoCompleteGrupo = "";
		tipoProducto = new TipoProducto();
		listUnidadMedida = unidadMedidaRepository
				.findAllActivasByEmpresa(empresaLogin);

	}

	public void crearProveedor() {
		try {
			log.info("Ingreso a crearProveedor");
			proveedor = new Proveedor();
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgCliente').show();");
		} catch (Exception e) {
			log.error("Error en crearProveedor : " + e.getMessage());
		}
	}

	public void crearLinea() {
		try {
			log.info("Ingreso a crearLinea");
			linea = new Linea();
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgLinea').show();");
		} catch (Exception e) {
			log.error("Error en crearLinea : " + e.getMessage());
		}
	}

	public void registrarProveedor() {
		try {
			// proveedor
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			proveedor.setEstado(estado);
			proveedor.setPlanCuenta(null);
			proveedor.setPlanCuentaAnticipo(null);
			proveedor.setCiudad(null);
			proveedor.setEmpresa(empresaLogin);
			proveedor.setUsuarioRegistro(nombreUsuario);
			proveedor.setFechaRegistro(new Date());
			proveedor = proveedorRegistration.create(proveedor);
			newProducto.setProveedor(proveedor);

			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Proveedor Registrado!", proveedor.getNombre() + "!");
			facesContext.addMessage(null, m);
			listProveedor = proveedorRepository
					.findAllActivasByEmpresa(empresaLogin);
			obtenerProveedor();
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgCliente').hide();");
		} catch (Exception e) {
			log.info("registrar() ERROR: " + e.getMessage());
		}
	}

	public void registrarLinea() {
		try {
			linea.setState("AC");
			linea.setEmpresa(empresaLogin);
			linea.setUsuarioRegistro(nombreUsuario);
			linea.setFechaRegistro(new Date());
			linea = lineaRegistration.create(linea);

			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Linea Producto Registrado!", linea.getNombre() + "!");
			facesContext.addMessage(null, m);
			listLinea = lineaRepository.findAllActivasByEmpresa(empresaLogin);
			listGrupoProducto = grupoProductoRepository
					.findAllActivasByEmpresaForLinea(empresaLogin, linea);
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgLinea').hide();");
		} catch (Exception e) {
			log.info("registrar() ERROR: " + e.getMessage());
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

	private boolean existeTipoProducto() {
		try {
			log.info("Ingreso a existeTipoProducto");

			List<TipoProducto> list = tipoProductoRepository
					.findAllActivasByEmpresa(empresaLogin,
							textoAutoCompleteGrupo.toUpperCase());
			if (list.size() > 0) {
				tipoProducto = list.get(0);
			}
			return list.size() > 0;
		} catch (Exception e) {
			log.error("Error en existeTipoProducto : " + e.getMessage());
		}
		return false;
	}

	private boolean existeUnidadMedida() {
		try {
			log.info("Ingreso a existeUnidadMedida");

			List<UnidadMedida> list = unidadMedidaRepository
					.findAllActivasByEmpresa(empresaLogin,
							textoAutoCompleteMedida.toUpperCase());
			if (list.size() > 0) {
				unidadMedida = list.get(0);
			}
			return list.size() > 0;
		} catch (Exception e) {
			log.error("Error en existeUnidadMedida : " + e.getMessage());
		}
		return false;
	}

	private boolean existeGrupoProducto() {
		try {
			log.info("Ingreso a existeTipoProducto");

			List<GrupoProducto> list = grupoProductoRepository
					.findAllActivasByEmpresaForLinea(empresaLogin, linea,
							textoAutoCompleteClasificacion.toUpperCase());
			if (list.size() > 0) {
				newProducto.setGrupoProducto(list.get(0));
				newProducto.getGrupoProducto().setLinea(linea);
			}
			return list.size() > 0;
		} catch (Exception e) {
			log.error("Error en existeTipoProducto : " + e.getMessage());
		}
		return false;
	}

	// ----- metodos producto ---------------

	public void registrarProducto() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			if (textoAutoCompleteGrupo.trim().length() == 0) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Revisar y llenar", "Grupo de Producto!");
				facesContext.addMessage(null, m);
				return;
			}
			if (!existeTipoProducto()) {
				tipoProducto = new TipoProducto();
				tipoProducto.setNombre(textoAutoCompleteGrupo);
				tipoProducto.setEmpresa(empresaLogin);
				tipoProducto.setState("AC");
				tipoProducto.setUsuarioRegistro(nombreUsuario);
				tipoProducto.setFechaRegistro(new Date());
				tipoProducto = tipoProductoRegistration.create(tipoProducto);
			}

			if (!existeGrupoProducto()) {
				grupoProducto = new GrupoProducto();
				grupoProducto.setNombre(textoAutoCompleteClasificacion);
				grupoProducto.setEmpresa(empresaLogin);
				grupoProducto.setState("AC");
				grupoProducto.setLinea(linea);
				grupoProducto.setUsuarioRegistro(nombreUsuario);
				grupoProducto.setFechaRegistro(new Date());
				grupoProducto = grupoProductoRegistration.create(grupoProducto);
			}

			log.info("Contenido Neto : " + newProducto.getContenidoNeto());
			if (newProducto.getContenidoNeto() != null
					&& newProducto.getContenidoNeto().intValue() > 0) {
				if (!existeUnidadMedida()) {
					unidadMedida = new UnidadMedida();
					unidadMedida.setNombre(textoAutoCompleteMedida);
					unidadMedida.setEmpresa(empresaLogin);
					unidadMedida.setState("AC");
					unidadMedida.setUsuarioRegistro(nombreUsuario);
					unidadMedida.setFechaRegistro(new Date());
					unidadMedida = unidadMedidaRegistration
							.create(unidadMedida);
				}
				newProducto.setUnidadMedida(unidadMedida);
			} else {
				newProducto.setUnidadMedida(null);
				newProducto.setContenidoNeto(null);
			}
			newProducto.setGrupoProducto(grupoProducto);
			newProducto.setEstado(estado);
			newProducto.setTipoProducto(tipoProducto);
			newProducto.setUsuarioRegistro(nombreUsuario);
			newProducto.setFechaRegistro(new Date());
			newProducto.setEmpresa(empresaLogin);
			newProducto = productoRegistration.create(newProducto);

			FacesUtil.infoMessage("Producto Registrada!",
					newProducto.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo registrar la producto.!");
		}
	}

	public void modificarProducto() {
		try {
			String estado = nombreEstado.equals("ACTIVO") ? "AC" : "IN";
			if (textoAutoCompleteGrupo.trim().length() == 0) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Revisar y llenar", "Grupo de Producto!");
				facesContext.addMessage(null, m);
				return;
			}
			if (!existeTipoProducto()) {
				tipoProducto = new TipoProducto();
				tipoProducto.setNombre(textoAutoCompleteGrupo);
				tipoProducto.setEmpresa(empresaLogin);
				tipoProducto.setState("AC");
				tipoProducto.setUsuarioRegistro(nombreUsuario);
				tipoProducto.setFechaRegistro(new Date());
				tipoProducto = tipoProductoRegistration.create(tipoProducto);
			}

			if (!existeGrupoProducto()) {
				grupoProducto = new GrupoProducto();
				grupoProducto.setNombre(textoAutoCompleteClasificacion);
				grupoProducto.setEmpresa(empresaLogin);
				grupoProducto.setState("AC");
				grupoProducto.setLinea(linea);
				grupoProducto.setUsuarioRegistro(nombreUsuario);
				grupoProducto.setFechaRegistro(new Date());
				grupoProducto = grupoProductoRegistration.create(grupoProducto);
			}

			log.info("Contenido Neto : " + newProducto.getContenidoNeto());
			if (newProducto.getContenidoNeto() != null
					&& newProducto.getContenidoNeto().intValue() > 0) {
				if (!existeUnidadMedida()) {
					unidadMedida = new UnidadMedida();
					unidadMedida.setNombre(textoAutoCompleteMedida);
					unidadMedida.setEmpresa(empresaLogin);
					unidadMedida.setState("AC");
					unidadMedida.setUsuarioRegistro(nombreUsuario);
					unidadMedida.setFechaRegistro(new Date());
					unidadMedida = unidadMedidaRegistration
							.create(unidadMedida);
				}
				newProducto.setUnidadMedida(unidadMedida);
			} else {
				newProducto.setUnidadMedida(null);
				newProducto.setContenidoNeto(null);
			}
			newProducto.setGrupoProducto(grupoProducto);
			newProducto.setEstado(estado);
			newProducto.setTipoProducto(tipoProducto);
			newProducto.setEmpresa(empresaLogin);
			productoRegistration.update(newProducto);

			FacesUtil.infoMessage("Producto Modificada!",
					newProducto.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo modificar la producto.!");
		}
	}

	public void eliminarProducto() {
		try {
			newProducto.setEstado("RM");
			productoRegistration.update(newProducto);

			FacesUtil.infoMessage("Producto Eliminada!",
					newProducto.getNombre());
			loadDefault();
		} catch (Exception e) {
			FacesUtil.warnMessage("No se pudo eliminar la producto.!");
		}

	}

	public void onRowSelectProducto(SelectEvent event) {
		log.info("onRowSelectProducto -> selectedProducto:"
				+ selectedProducto.getNombre());
		crear = false;
		modificar = true;
		registrar = false;
		newProducto = selectedProducto;
		textoAutoCompleteMedida = "";
		textoAutoCompleteClasificacion = "";
		textoAutoCompleteGrupo = "";
		if (newProducto.getTipoProducto() != null) {
			textoAutoCompleteGrupo = newProducto.getTipoProducto().getNombre();
		}
		if (newProducto.getGrupoProducto() != null) {
			textoAutoCompleteClasificacion = newProducto.getGrupoProducto()
					.getNombre();
			linea = newProducto.getGrupoProducto().getLinea();
		}

		if (newProducto.getUnidadMedida() != null) {
			textoAutoCompleteMedida = newProducto.getUnidadMedida().getNombre();
			unidadMedida = newProducto.getUnidadMedida();
		}
		FacesUtil
				.updateComponent("formTableDosificacion:dataTableDosificacion");
	}

	// ------- metodos dosificacion -----------

	private void listarGrupoProducto() {
		try {
			log.info("Ingreso a listarGrupoProducto..");
			listGrupoProducto = grupoProductoRepository
					.findAllActivasByEmpresaForLinea(empresaLogin, linea);
		} catch (Exception e) {
			log.error("Error en listarGrupoProducto : " + e.getMessage());
		}
	}

	/**
	 * 
	 */

	public void obtenerProveedor() {
		try {
			log.info("Ingreso a obtenerProveedor");
			newProducto.setProveedor(em.find(Proveedor.class, newProducto
					.getProveedor().getId()));
			calcularPrecioVenta();
		} catch (Exception e) {
			log.error("Error en obtenerProveedor : " + e.getMessage());
		}
	}

	public void calcularPrecioVenta() {
		try {
			log.info("Ingreso a calcularPrecioVenta");
			double precioVenta = newProducto.getPrecioCompra()
					* ((newProducto.getProveedor().getMargenUtilidad() * 0.01) + (newProducto
							.getComision() * 0.01));
			log.info(precioVenta);
			newProducto.setPrecioVenta(precioVenta
					+ newProducto.getPrecioCompra());
		} catch (Exception e) {
			log.error("Error en calcularPrecioVenta : " + e.getMessage());
		}
	}

	public List<String> completeTextGrupo(String query) {
		List<String> results = new ArrayList<String>();
		listTipoProducto = tipoProductoRepository.findAllActivasByEmpresa(
				empresaLogin, query.toUpperCase());
		System.out.println("size : " + listTipoProducto.size());
		for (TipoProducto i : listTipoProducto) {
			results.add(i.getNombre());
		}
		return results;
	}

	public void onItemSelectGrupo(SelectEvent event) {
		String nombre = event.getObject().toString();
		log.info("nit : " + nombre);

		for (TipoProducto tipoProducto : listTipoProducto) {
			if (tipoProducto.getNombre().equals(nombre)) {
				log.info("Nit encontrado .." + nombre);
				textoAutoCompleteGrupo = tipoProducto.getNombre();
				this.setTipoProducto(tipoProducto);
				return;
			}
		}
		textoAutoCompleteGrupo = nombre;

	}

	// UNIDAD DE MEDIDA

	public List<String> completeTextUnidadMedida(String query) {
		List<String> results = new ArrayList<String>();
		listUnidadMedida = unidadMedidaRepository.findAllActivasByEmpresa(
				empresaLogin, query.toUpperCase());
		System.out.println("size : " + listUnidadMedida.size());
		for (UnidadMedida i : listUnidadMedida) {
			results.add(i.getNombre());
		}
		return results;
	}

	public void onItemSelectUnidadMedida(SelectEvent event) {
		String nombre = event.getObject().toString();
		log.info("Unidad Medida : " + nombre);

		for (UnidadMedida unidadMedida : listUnidadMedida) {
			if (unidadMedida.getNombre().equals(nombre)) {
				log.info("Unidad Medida .." + nombre);
				textoAutoCompleteMedida = unidadMedida.getNombre();
				return;
			}
		}
		textoAutoCompleteMedida = nombre;

	}

	// CLASIFICACION

	private String textoAutoCompleteClasificacion;

	public List<String> completeTextClasificacion(String query) {
		List<String> results = new ArrayList<String>();

		listGrupoProducto = grupoProductoRepository
				.findAllActivasByEmpresaForLinea(empresaLogin, linea,
						query.toUpperCase());
		System.out.println("size : " + listGrupoProducto.size());
		for (GrupoProducto i : listGrupoProducto) {
			results.add(i.getNombre());
		}
		return results;
	}

	public void onItemSelectClasificacion(SelectEvent event) {
		String nombre = event.getObject().toString();
		log.info("nit : " + nombre);

		for (GrupoProducto grupoProducto : listGrupoProducto) {
			if (grupoProducto.getNombre().equals(nombre)) {
				log.info("Nit encontrado .." + nombre);
				textoAutoCompleteClasificacion = grupoProducto.getNombre();
				newProducto.setGrupoProducto(grupoProducto);
				return;
			}
		}
		textoAutoCompleteClasificacion = nombre;

	}

	// -------- acciones para la vista----------

	public void cambiarAspecto() {
		crear = false;
		registrar = true;
		modificar = false;

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

	// -------- get and set---------------------

	public boolean isModificar() {
		return modificar;
	}

	public void setModificar(boolean modificar) {
		this.modificar = modificar;
	}

	public Producto getSelectedProducto() {
		return selectedProducto;
	}

	public void setSelectedProducto(Producto selectedProducto) {
		this.selectedProducto = selectedProducto;
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

	public boolean isSeleccionadaDosificacion() {
		return seleccionadaDosificacion;
	}

	public void setSeleccionadaDosificacion(boolean seleccionadaDosificacion) {
		this.seleccionadaDosificacion = seleccionadaDosificacion;
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

	public Producto getNewProducto() {
		return newProducto;
	}

	public void setNewProducto(Producto newProducto) {
		this.newProducto = newProducto;
	}

	public List<Producto> getListProducto() {
		return listProducto;
	}

	public void setListProducto(List<Producto> listProducto) {
		this.listProducto = listProducto;
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

	public List<TipoProducto> getListTipoProducto() {
		return listTipoProducto;
	}

	public void setListTipoProducto(List<TipoProducto> listTipoProducto) {
		this.listTipoProducto = listTipoProducto;
	}

	public List<GrupoProducto> getListGrupoProducto() {
		return listGrupoProducto;
	}

	public void setListGrupoProducto(List<GrupoProducto> listGrupoProducto) {
		this.listGrupoProducto = listGrupoProducto;
	}

	public List<Linea> getListLinea() {
		return listLinea;
	}

	public void setListLinea(List<Linea> listLinea) {
		this.listLinea = listLinea;
	}

	public Linea getLinea() {
		return linea;
	}

	public void setLinea(Linea linea) {
		this.linea = linea;
	}

	public List<Proveedor> getListProveedor() {
		return listProveedor;
	}

	public void setListProveedor(List<Proveedor> listProveedor) {
		this.listProveedor = listProveedor;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public String getTextoAutoCompleteGrupo() {
		return textoAutoCompleteGrupo;
	}

	public void setTextoAutoCompleteGrupo(String textoAutoCompleteGrupo) {
		this.textoAutoCompleteGrupo = textoAutoCompleteGrupo;
	}

	public TipoProducto getTipoProducto() {
		return tipoProducto;
	}

	public void setTipoProducto(TipoProducto tipoProducto) {
		this.tipoProducto = tipoProducto;
	}

	public String getTextoAutoCompleteClasificacion() {
		return textoAutoCompleteClasificacion;
	}

	public void setTextoAutoCompleteClasificacion(
			String textoAutoCompleteClasificacion) {
		this.textoAutoCompleteClasificacion = textoAutoCompleteClasificacion;
	}

	public String getTextoAutoCompleteMedida() {
		return textoAutoCompleteMedida;
	}

	public void setTextoAutoCompleteMedida(String textoAutoCompleteMedida) {
		this.textoAutoCompleteMedida = textoAutoCompleteMedida;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

}
