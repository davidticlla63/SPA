package bo.com.erp360.webapp.model;

// Generated Jul 30, 2014 2:47:16 PM by Hibernate Tools 3.4.0.CR1

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Class Factura
 * 
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@Table(name = "nota_venta", schema = "public")
@SuppressWarnings("serial")
public class NotaVenta implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	

	@Column(name = "numero_ticket", nullable = true)
	private Integer numeroTicket;

	private String concepto;

	@Column(name = "total_efectivo", nullable = false)
	// TOTAL CANCELADO EN EFECTIVO
	private double totalEfectivo=0;

	@Column(name = "total_venta", nullable = false)
	// SIN DESCUENTO
	private double totalVenta=0;

	@Column(name = "total_descuento", nullable = false)
	// DESCUENTO
	private double totaldescuento=0;

	@Column(name = "total_pagar", nullable = false)
	// TOTAL CON DESCUENTO
	private double totalPagar=0;

	private double cambio=0;
	
	@Column(name = "numero_orden", nullable = false)
	private Integer numeroOrden=0;

	private boolean impresion = true;

	@Column(name = "total_literal", nullable = false)
	private String totalLiteral;

	@Column(name = "nit_ci", nullable = true)
	private String nitCi;
	
	@Column(name = "nombre_cliente", nullable = true)
	private String nombreCliente ;//Cliente para la factura
	
	

	@Column(name = "numero_factura", nullable = true)
	private String numeroFactura;

	@Column(name = "tipo_pago", nullable = false)
	private String tipoPago;

	@Column(name = "tipo_cambio", nullable = false)
	private double tipoCambio;
	// ---

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_sucursal", nullable = false)
	private Sucursal sucursal;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)//Cliente orden de venta
	@JoinColumn(name = "id_cliente", nullable = false)
	private Cliente cliente;

	private String gestion;

	@Size(max = 2)
	// AC , IN
	private String estado="AC";

	@Column(name = "fecha_registro", nullable = false)
	private Date fechaRegistro;

	@Column(name = "fecha_modificacion", nullable = true)
	private Date fechaModificacion;

	@Column(name = "usuario_registro", nullable = false)
	private String usuarioRegistro;

	private String mes;

	public NotaVenta() {
		super();
		this.id = 0;
		totaldescuento = 0;
		totalEfectivo = 0;
		totalPagar = 0;
		totalVenta = 0;
		cambio = 0;
		tipoCambio = 0;
		estado = "AC";
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
			if (!(obj instanceof Factura)) {
				return false;
			} else {
				if (((NotaVenta) obj).id == this.id) {
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

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(String numeroFactura) {
		this.numeroFactura = numeroFactura;
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

	public Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public double getTotalEfectivo() {
		return totalEfectivo;
	}

	public void setTotalEfectivo(double totalEfectivo) {
		this.totalEfectivo = totalEfectivo;
	}

	public double getTotalPagar() {
		return totalPagar;
	}

	public void setTotalPagar(double totalPagar) {
		this.totalPagar = totalPagar;
	}

	public double getCambio() {
		return cambio;
	}

	public void setCambio(double cambio) {
		this.cambio = cambio;
	}

	public String getTotalLiteral() {
		return totalLiteral;
	}

	public void setTotalLiteral(String totalLiteral) {
		this.totalLiteral = totalLiteral;
	}

	public String getNitCi() {
		return nitCi;
	}

	public void setNitCi(String nitCi) {
		this.nitCi = nitCi;
	}

	public String getTipoPago() {
		return tipoPago;
	}

	public void setTipoPago(String tipoPago) {
		this.tipoPago = tipoPago;
	}

	public double getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(double tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public String getUsuarioRegistro() {
		return usuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		this.usuarioRegistro = usuarioRegistro;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getGestion() {
		return gestion;
	}

	public void setGestion(String gestion) {
		this.gestion = gestion;
	}

	public String getMes() {
		return mes;
	}

	public void setMes(String mes) {
		this.mes = mes;
	}

	public boolean isImpresion() {
		return impresion;
	}

	public double getTotaldescuento() {
		return totaldescuento;
	}

	public void setTotaldescuento(double totaldescuento) {
		this.totaldescuento = totaldescuento;
	}

	public double getTotalVenta() {
		return totalVenta;
	}

	public void setTotalVenta(double totalVenta) {
		this.totalVenta = totalVenta;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public Integer getNumeroOrden() {
		return numeroOrden;
	}

	public void setNumeroOrden(Integer numeroOrden) {
		this.numeroOrden = numeroOrden;
	}

	public Integer getNumeroTicket() {
		return numeroTicket;
	}

	public void setNumeroTicket(Integer numeroTicket) {
		this.numeroTicket = numeroTicket;
	}
}
