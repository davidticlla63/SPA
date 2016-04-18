package bo.com.erp360.webapp.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

@Entity
@SuppressWarnings("serial")
@Table(name = "detalle_nota_venta", schema = "public")
public class DetalleNotaVenta implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_nota_venta", nullable = false)
	private NotaVenta notaVenta;

	@Column(name = "codigo", length = 20)
	private String codigo;

	@Column(name = "concepto", nullable = false, length = 200)
	private String concepto;

	@Column(name = "cantidad", nullable = false)
	private double cantidad;

	@Column(name = "precio_unitario", nullable = false, precision = 8, scale = 8)
	private double precioUnitario;

	@Column(name = "precio_total", nullable = false, precision = 8, scale = 8)
	private double precioTotal;

	@Column(name = "descuentos", precision = 8, scale = 8)
	private double descuentos;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_empleado", nullable = false)
	private Empleado empleado;

	@Column(name = "porcentaje_comision", precision = 8, scale = 8)
	private double porcentajeComision = 0;

	private double comision = 0;
	
	@Column(name = "numero_orden", nullable = false)
	private Integer numeroOrden=0;

	@Size(max = 2)
	// AC , IN
	private String estado;

	@Column(name = "tipo_cambio",  precision = 8, scale = 8)
	private double tipoCambio = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha_registro", length = 29)
	private Date fechaRegistro;

	@Column(name = "usuario_registro", length = 30)
	private String usuarioRegistro;

	public double getPorcentajeComision() {
		return porcentajeComision;
	}

	public void setPorcentajeComision(double porcentajeComision) {
		this.porcentajeComision = porcentajeComision;
	}

	public double getComision() {
		return comision;
	}

	public void setComision(double comision) {
		this.comision = comision;
	}

	public double getDescuentos() {
		return descuentos;
	}

	public void setDescuentos(double descuentos) {
		this.descuentos = descuentos;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public NotaVenta getNotaVenta() {
		return notaVenta;
	}

	public void setNotaVenta(NotaVenta notaVenta) {
		this.notaVenta = notaVenta;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public double getCantidad() {
		return cantidad;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}

	public double getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(double precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public double getPrecioTotal() {
		return precioTotal;
	}

	public void setPrecioTotal(double precioTotal) {
		this.precioTotal = precioTotal;
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
		return usuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		this.usuarioRegistro = usuarioRegistro;
	}

	public Empleado getEmpleado() {
		return empleado;
	}

	public void setEmpleado(Empleado empleado) {
		this.empleado = empleado;
	}

	public double getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(double tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public Integer getNumeroOrden() {
		return numeroOrden;
	}

	public void setNumeroOrden(Integer numeroOrden) {
		this.numeroOrden = numeroOrden;
	}

}
