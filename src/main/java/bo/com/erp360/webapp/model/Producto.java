package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Class Proveedor
 * 
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "producto", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = {
		"nombre", "id_empresa","id_proveedor" ,"id_tipo_producto" ,"id_grupo_producto"}))
public class Producto implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String codigo;
	
	private String nombre;
	
	@Column(name = "stock_max", nullable = false)
	private double stockMax=500;
	
	@Column(name = "stock_min", nullable = false)
	private double stockMin=50;
	
	private double stock=0;

	@Column(name = "descripcion", nullable = true)
	private String descripcion;
	
	@Column(name = "margen_utilidad", nullable = false)
	private double margenUtilidad=0;
	
	private double comision=0;

	@Column(name = "precio_venta", nullable = true)
	private double precioVenta = 0;
	
	@Column(name = "contenido_neto", nullable = true)
	private Integer contenidoNeto;

	@Column(name = "precio_compra", nullable = true)
	private double precioCompra = 0;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_tipo_producto", nullable = true)
	private TipoProducto tipoProducto;
	
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_grupo_producto", nullable = true)
	private GrupoProducto grupoProducto;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_proveedor", nullable = true)
	private Proveedor proveedor;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_unidad_medida", nullable = true)
	private UnidadMedida unidadMedida;

	@Size(max = 2)
	// AC , IN
	private String estado;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;

	@Column(name = "fecha_registro", nullable = false)
	private Date fechaRegistro;

	@Column(name = "Usuario_registro", nullable = false)
	private String UsuarioRegistro;
	
	
	private boolean media=false;

	public Producto() {
		super();
		this.id = 0;
		this.estado = "AC";
		this.nombre = "";
		this.descripcion = "";
		this.tipoProducto= new TipoProducto();
		this.grupoProducto= new GrupoProducto();
		this.unidadMedida= new UnidadMedida();
		this.proveedor= new Proveedor();
	}

	
	@Override
	public String toString() {
		return  nombre;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else {
			if (!(obj instanceof Producto)) {
				return false;
			} else {
				if (((Producto) obj).id == this.id) {
					return true;
				} else {
					return false;
				}
			}
		}
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public String getUsuarioRegistro() {
		return UsuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		UsuarioRegistro = usuarioRegistro;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public TipoProducto getTipoProducto() {
		return tipoProducto;
	}

	public void setTipoProducto(TipoProducto tipoProducto) {
		this.tipoProducto = tipoProducto;
	}

	public double getComision() {
		return comision;
	}

	public void setComision(double comision) {
		this.comision = comision;
	}

	public double getPrecioVenta() {
		return precioVenta;
	}

	public void setPrecioVenta(double precioVenta) {
		this.precioVenta = precioVenta;
	}

	public double getStockMax() {
		return stockMax;
	}

	public void setStockMax(double stockMax) {
		this.stockMax = stockMax;
	}

	public double getStockMin() {
		return stockMin;
	}

	public void setStockMin(double stockMin) {
		this.stockMin = stockMin;
	}

	public double getStock() {
		return stock;
	}

	public void setStock(double stock) {
		this.stock = stock;
	}


	public boolean isMedia() {
		return media;
	}


	public void setMedia(boolean media) {
		this.media = media;
	}


	public GrupoProducto getGrupoProducto() {
		return grupoProducto;
	}


	public void setGrupoProducto(GrupoProducto grupoProducto) {
		this.grupoProducto = grupoProducto;
	}


	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}


	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}


	public double getMargenUtilidad() {
		return margenUtilidad;
	}


	public void setMargenUtilidad(double margenUtilidad) {
		this.margenUtilidad = margenUtilidad;
	}


	public Proveedor getProveedor() {
		return proveedor;
	}


	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}


	public double getPrecioCompra() {
		return precioCompra;
	}


	public void setPrecioCompra(double precioCompra) {
		this.precioCompra = precioCompra;
	}


	public Integer getContenidoNeto() {
		return contenidoNeto;
	}


	public void setContenidoNeto(Integer contenidoNeto) {
		this.contenidoNeto = contenidoNeto;
	}

}
