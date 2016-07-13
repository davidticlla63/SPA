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

/**
 * Class DetalleFactura
 * 
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "sub_detalle_factura", schema = "public")
public class SubDetalleFactura implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;
	
	private int correlativo=0;

	private int secuencia=0;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_detalle_factura", nullable = false)
	private DetalleFactura detalleFactura;

	@Column(name = "concepto", nullable = false, length = 2000)
	private String concepto;

	@Column(name = "cantidad", nullable = false)
	private double cantidad;

	@Column(name = "precio_unitario", nullable = false, precision = 8, scale = 8)
	private double precioUnitario;

	@Column(name = "precio_total", nullable = false, precision = 8, scale = 8)
	private double precioTotal;
	
	private String moneda="Bolivianos";

	@Size(max = 2)
	// AC , IN
	private String estado;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha_registro", length = 29)
	private Date fechaRegistro;

	@Column(name = "usuario_registro", length = 30)
	private String usuarioRegistro;

	public SubDetalleFactura() {
		super();
		this.id = 0;
		this.cantidad=0;
		this.precioTotal=0;
		this.precioUnitario=0;
		
	}
	

	public SubDetalleFactura(int correlativo,
			DetalleFactura detalleFactura, String concepto, double precioTotal,
			String usuarioRegistro) {
		super();
		this.correlativo = correlativo;
		this.detalleFactura = detalleFactura;
		this.concepto = concepto;
		this.precioTotal = precioTotal;
		this.usuarioRegistro = usuarioRegistro;
	}


	public SubDetalleFactura(Integer id, String concepto,
			double cantidad, double precioUnitario, double precioTotal) {
		this.id = id;
		this.concepto = concepto;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
		this.precioTotal = precioTotal;
	}

	public SubDetalleFactura(Integer id, 
			String codigoProducto, String concepto, double cantidad,
			double precioUnitario, double precioTotal, double descuentos,
			String estado, Date fechaRegistro, String usuarioRegistro) {
		this.id = id;
		this.concepto = concepto;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
		this.precioTotal = precioTotal;
		this.estado = estado;
		this.fechaRegistro = fechaRegistro;
		this.usuarioRegistro = usuarioRegistro;
	}

	@Override
	public String toString() {
		return String.valueOf(id);
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
			if (!(obj instanceof SubDetalleFactura)) {
				return false;
			} else {
				if (((SubDetalleFactura) obj).id == this.id) {
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


	public String getConcepto() {
		return this.concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public double getCantidad() {
		return this.cantidad;
	}

	public void setCantidad(double cantidad) {
		this.cantidad = cantidad;
	}

	public double getPrecioUnitario() {
		precioUnitario = round(precioUnitario, 2);
		return this.precioUnitario;
	}

	public void setPrecioUnitario(double precioUnitario) {
		this.precioUnitario = precioUnitario;
	}

	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public double getPrecioTotal() {
		precioTotal = round(precioTotal, 2);
		return this.precioTotal;
	}

	public void setPrecioTotal(double precioTotal) {
		this.precioTotal = precioTotal;
	}

	public String getEstado() {
		return this.estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Date getFechaRegistro() {
		return this.fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public String getUsuarioRegistro() {
		return this.usuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		this.usuarioRegistro = usuarioRegistro;
	}

	public DetalleFactura getDetalleFactura() {
		return detalleFactura;
	}

	public void setDetalleFactura(DetalleFactura detalleFactura) {
		this.detalleFactura = detalleFactura;
	}

	public int getCorrelativo() {
		return correlativo;
	}

	public void setCorrelativo(int correlativo) {
		this.correlativo = correlativo;
	}


	public String getMoneda() {
		return moneda;
	}


	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}


	public int getSecuencia() {
		return secuencia;
	}


	public void setSecuencia(int secuencia) {
		this.secuencia = secuencia;
	}

}
