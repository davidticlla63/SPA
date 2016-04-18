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
@Table(name = "factura", schema = "public")
@SuppressWarnings("serial")
public class Factura implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "nombre_factura", nullable = false)
	private String nombreFactura;

	private String concepto;

	@Column(name = "credito_fiscal", nullable = false)
	private String creditoFiscal;

	@Column(name = "numero_autorizacion", nullable = false)
	private String numeroAutorizacion;

	@Column(name = "total_efectivo", nullable = false)
	private double totalEfectivo;

	@Column(name = "total_pagar", nullable = false)
	private double totalPagar;

	private double cambio;

	private boolean impresion = true;

	@Column(name = "total_literal", nullable = false)
	private String totalLiteral;

	@Column(name = "fecha_limite_emision", nullable = false)
	private Date fechaLimiteEmision;

	@Column(name = "fecha_factura", nullable = false)
	private Date fechaFactura;

	@Column(name = "nit_ci", nullable = false)
	private String nitCi;

	@Column(name = "numero_factura", nullable = false)
	private String numeroFactura;

	@Column(name = "total_facturado", nullable = false)
	private double totalFacturado;

	@Column(name = "codigo_control", nullable = false)
	private String codigoControl;

	@Column(name = "codigo_respuesta_rapida", nullable = false)
	private String codigoRespuestaRapida;//

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

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_cliente", nullable = false)
	private Cliente cliente;

	private String gestion;
	
	
	private String direccion;

	@Size(max = 2)
	// AC , IN
	private String estado;

	@Column(name = "fecha_registro", nullable = false)
	private Date fechaRegistro;

	@Column(name = "fecha_modificacion", nullable = true)
	private Date fechaModificacion;

	@Column(name = "usuario_registro", nullable = false)
	private String usuarioRegistro;

	// campos para libro ventas
	private double importeICE;
	private double importeExportaciones;// Excepciones
	private double importeVentasGrabadasTasaCero;// neto
	private double importeSubTotal;
	private double importeDescuentosBonificaciones;
	private double importeBaseDebitoFiscal;
	private double debitoFiscal;// IVA
	private String mes;

	public Factura() {
		super();
		this.id = 0;
		importeICE = 0;
		importeExportaciones = 0;
		importeVentasGrabadasTasaCero = 0;
		importeSubTotal = 0;
		importeDescuentosBonificaciones = 0;
		importeBaseDebitoFiscal = 0;
		debitoFiscal = 0;
	}

	@Override
	public String toString() {
		return "Factura [id=" + id + ", nombreFactura=" + nombreFactura
				+ ", concepto=" + concepto + ", numeroAutorizacion="
				+ numeroAutorizacion + ", totalEfectivo=" + totalEfectivo
				+ ", totalPagar=" + totalPagar + ", cambio=" + cambio
				+ ", totalLiteral=" + totalLiteral + ", fechaLimiteEmision="
				+ fechaLimiteEmision + ", fechaFactura=" + fechaFactura
				+ ", nitCi=" + nitCi + ", numeroFactura=" + numeroFactura
				+ ", totalFacturado=" + totalFacturado + ", codigoControl="
				+ codigoControl + ", codigoRespuestaRapida="
				+ codigoRespuestaRapida + ", tipoPago=" + tipoPago
				+ ", tipoCambio=" + tipoCambio + ", empresa=" + empresa
				+ ", sucursal=" + sucursal + ", cliente=" + cliente
				+ ", estado=" + estado + ", fechaRegistro=" + fechaRegistro
				+ ", fechaModificacion=" + fechaModificacion
				+ ", usuarioRegistro=" + usuarioRegistro + "]";
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
				if (((Factura) obj).id == this.id) {
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

	public Date getFechaFactura() {
		return fechaFactura;
	}

	public void setFechaFactura(Date fechaFactura) {
		this.fechaFactura = fechaFactura;
	}

	public String getNumeroFactura() {
		return numeroFactura;
	}

	public void setNumeroFactura(String numeroFactura) {
		this.numeroFactura = numeroFactura;
	}

	public String getNumeroAutorizacion() {
		return numeroAutorizacion;
	}

	public void setNumeroAutorizacion(String numeroAutorizacion) {
		this.numeroAutorizacion = numeroAutorizacion;
	}

	public String getCodigoControl() {
		return codigoControl;
	}

	public void setCodigoControl(String codigoControl) {
		this.codigoControl = codigoControl;
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

	public String getNombreFactura() {
		return nombreFactura;
	}

	public void setNombreFactura(String nombreFactura) {
		this.nombreFactura = nombreFactura;
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

	public Date getFechaLimiteEmision() {
		return fechaLimiteEmision;
	}

	public void setFechaLimiteEmision(Date fechaLimiteEmision) {
		this.fechaLimiteEmision = fechaLimiteEmision;
	}

	public String getNitCi() {
		return nitCi;
	}

	public void setNitCi(String nitCi) {
		this.nitCi = nitCi;
	}

	public double getTotalFacturado() {
		return totalFacturado;
	}

	public void setTotalFacturado(double totalFacturado) {
		this.totalFacturado = totalFacturado;
	}

	public String getCodigoRespuestaRapida() {
		return codigoRespuestaRapida;
	}

	public void setCodigoRespuestaRapida(String codigoRespuestaRapida) {
		this.codigoRespuestaRapida = codigoRespuestaRapida;
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

	public double getImporteICE() {
		return importeICE;
	}

	public void setImporteICE(double importeICE) {
		this.importeICE = importeICE;
	}

	public double getImporteExportaciones() {
		return importeExportaciones;
	}

	public void setImporteExportaciones(double importeExportaciones) {
		this.importeExportaciones = importeExportaciones;
	}

	public double getImporteVentasGrabadasTasaCero() {
		return importeVentasGrabadasTasaCero;
	}

	public void setImporteVentasGrabadasTasaCero(
			double importeVentasGrabadasTasaCero) {
		this.importeVentasGrabadasTasaCero = importeVentasGrabadasTasaCero;
	}

	public double getImporteSubTotal() {
		return importeSubTotal;
	}

	public void setImporteSubTotal(double importeSubTotal) {
		this.importeSubTotal = importeSubTotal;
	}

	public double getImporteBaseDebitoFiscal() {
		return importeBaseDebitoFiscal;
	}

	public void setImporteBaseDebitoFiscal(double importeBaseDebitoFiscal) {
		this.importeBaseDebitoFiscal = importeBaseDebitoFiscal;
	}

	public double getImporteDescuentosBonificaciones() {
		return importeDescuentosBonificaciones;
	}

	public void setImporteDescuentosBonificaciones(
			double importeDescuentosBonificaciones) {
		this.importeDescuentosBonificaciones = importeDescuentosBonificaciones;
	}

	public String getMes() {
		return mes;
	}

	public void setMes(String mes) {
		this.mes = mes;
	}

	public double getDebitoFiscal() {
		return debitoFiscal;
	}

	public void setDebitoFiscal(double debitoFiscal) {
		this.debitoFiscal = debitoFiscal;
	}

	public boolean isImpresion() {
		return impresion;
	}

	public void setImpresion(boolean impresion) {
		this.impresion = impresion;
	}

	public String getCreditoFiscal() {
		return creditoFiscal;
	}

	public void setCreditoFiscal(String creditoFiscal) {
		this.creditoFiscal = creditoFiscal;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

}
