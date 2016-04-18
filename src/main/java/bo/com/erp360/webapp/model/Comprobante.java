package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Class Comprobante
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@Table(name = "comprobante", schema = "public")
public class Comprobante implements Serializable {	

	private static final long serialVersionUID = 9123962611753602798L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private Integer numero;
	private String nombre;
	private String glosa;
	
	@Temporal(TemporalType.DATE)
	@Column(name="fecha")
	private Date fecha;
	
	@Column(name="importe_total_debe_nacional",nullable=true )
	private double importeTotalDebeNacional;
	
	@Column(name="importe_total_haber_nacional",nullable=true )
	private double importeTotalHaberNacional;
	
	@Column(name="importe_literal_nacional",nullable=true )
	private String importeLiteralNacional;
	
	@Column(name="importe_total_debe_extranjero",nullable=true )
	private double importeTotalDebeExtranjero;
	
	@Column(name="importe_total_haber_extranjero",nullable=true )
	private double importeTotalHaberExtranjero;
	
	@Column(name="importe_literal_extranjero",nullable=true )
	private String importeLiteralExtranjero;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="id_sucursal", nullable=false)
	private Sucursal sucursal;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_moneda_empresa", nullable=true)
	private MonedaEmpresa monedaEmpresa;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="id_tipo_comprobante", nullable=false)
	private TipoComprobante tipoComprobante;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_empresa",nullable=true )
	private Empresa empresa;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_gestion",nullable=true )
	private Gestion gestion;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_tipo_cambio",nullable=true )
	private TipoCambio tipoCambio;

	@Size(max = 2) //AC , IN
	private String estado;
	
	@Column(name="usuario_registro",nullable=false )
	private String usuarioRegistro;
	
	@Column(name="fecha_registro",nullable=false )
	private Date fechaRegistro;
	
	@Column(name="fecha_modificacion",nullable=true )
	private Date fechaModificacion;
	
	private String correlativo;
	
	@Column(name="correlativo_transaccional",nullable=true )
	private String correlativoTransaccional;
	
	public Comprobante() {
		super();
		this.id = 0;
		this.glosa   = "" ;
		this.nombre  = "" ;
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
		if(obj==null){
			return false;
		}else{
			if(!(obj instanceof Comprobante)){
				return false;
			}else{
				if(((Comprobante)obj).id==this.id){
					return true;
				}else{
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

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}
	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getGlosa() {
		return glosa;
	}

	public void setGlosa(String glosa) {
		this.glosa = glosa;
	}

	public MonedaEmpresa getMonedaEmpresa() {
		return monedaEmpresa;
	}

	public void setMonedaEmpresa(MonedaEmpresa monedaEmpresa) {
		this.monedaEmpresa = monedaEmpresa;
	}

	public TipoComprobante getTipoComprobante() {
		return tipoComprobante;
	}

	public void setTipoComprobante(TipoComprobante tipoComprobante) {
		this.tipoComprobante = tipoComprobante;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
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
	
	public double getImporteTotalDebeNacional() {
		return importeTotalDebeNacional;
	}

	public void setImporteTotalDebeNacional(double importeTotalDebeNacional) {
		this.importeTotalDebeNacional = importeTotalDebeNacional;
	}

	public double getImporteTotalHaberNacional() {
		return importeTotalHaberNacional;
	}

	public void setImporteTotalHaberNacional(double importeTotalHaberNacional) {
		this.importeTotalHaberNacional = importeTotalHaberNacional;
	}
	
	public double getImporteTotalDebeExtranjero() {
		return importeTotalDebeExtranjero;
	}

	public void setImporteTotalDebeExtranjero(double importeTotalDebeExtranjero) {
		this.importeTotalDebeExtranjero = importeTotalDebeExtranjero;
	}

	public double getImporteTotalHaberExtranjero() {
		return importeTotalHaberExtranjero;
	}
	
	public void setImporteTotalHaberExtranjero(double importeTotalHaberExtranjero) {
		this.importeTotalHaberExtranjero = importeTotalHaberExtranjero;
	}

	public String getImporteLiteralNacional() {
		return importeLiteralNacional;
	}

	public void setImporteLiteralNacional(String importeLiteralNacional) {
		this.importeLiteralNacional = importeLiteralNacional;
	}

	public String getImporteLiteralExtranjero() {
		return importeLiteralExtranjero;
	}

	public void setImporteLiteralExtranjero(String importeLiteralExtranjero) {
		this.importeLiteralExtranjero = importeLiteralExtranjero;
	}

	public Gestion getGestion() {
		return gestion;
	}

	public void setGestion(Gestion gestion) {
		this.gestion = gestion;
	}

	public String getUsuarioRegistro() {
		return usuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		this.usuarioRegistro = usuarioRegistro;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public TipoCambio getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(TipoCambio tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public String getCorrelativo() {
		return correlativo;
	}

	public void setCorrelativo(String correlativo) {
		this.correlativo = correlativo;
	}

	public Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public String getCorrelativoTransaccional() {
		return correlativoTransaccional;
	}

	public void setCorrelativoTransaccional(String correlativoTransaccional) {
		this.correlativoTransaccional = correlativoTransaccional;
	}
}


