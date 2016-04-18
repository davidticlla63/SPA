package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Class Dosificacion
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "dosificacion", schema = "public")
public class Dosificacion implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name="numero_tramite",nullable=false )
	private String numeroTramite;
	
	@Column(name="numero_autorizacion",nullable=false )
	private String numeroAutorizacion;
	
	@Column(name="cantidad_dosificacion",nullable=false )
	private Integer cantidadDosificacion=0; 
	
	@Column(name="numero_inicial",nullable=false )
	private Integer numeroInicial=0;
	
	@Column(name="numero_secuencia",nullable=false)
	private Integer numeroSecuencia=0;
	
	@Column(name="norma_aplicada",nullable=false)
	private String normaAplicada;
	
	@Temporal(TemporalType.DATE)
	@Column(name="fecha_limite_emision")
	private Date fechaLimiteEmision;
	
	@Column(name="llave_control",nullable=false)
	private String llaveControl;
	
	@Column(name="actividad_economica",nullable=false)
	private String actividadEconomica="ACTIVIDAD";
	
	@Column(name="leyenda_inferior1",nullable=false)
	private String leyendaInferior1="leyendaInferior1";
	
	@Column(name="leyenda_inferior2",nullable=false)
	private String leyendaInferior2="leyendaInferior2";
	
	@Column(name="tipo_dosificacion")
	private String tipoDosificacion="CANTIDAD";
	
	private Boolean activo;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="id_sucursal", nullable=false)
	private Sucursal sucursal;
	
	@Size(max = 2) //AC , IN
	private String estado;
	
	@Column(name="fecha_registro",nullable=false )
	private Date fechaRegistro;
	
	@Column(name="fecha_modificacion",nullable=true )
	private Date fechaModificacion;
	//paginacion simple; paginando vistas blaces , EAGERdatamodel,tablespace
	@Column(name="usuario_registro",nullable=false )
	private String usuarioRegistro;

	public Dosificacion() {
		super();
		this.id= 0 ;
		this.tipoDosificacion = "CANTIDAD";
		this.normaAplicada = "NSF-07";
	}

	

	@Override
	public String toString() {
		return "Dosificacion [id=" + id + ", numeroTramite=" + numeroTramite
				+ ", numeroAutorizacion=" + numeroAutorizacion
				+ ", cantidadDosificacion=" + cantidadDosificacion
				+ ", numeroInicial=" + numeroInicial + ", numeroSecuencia="
				+ numeroSecuencia + ", normaAplicada=" + normaAplicada
				+ ", fechaLimiteEmision=" + fechaLimiteEmision
				+ ", llaveControl=" + llaveControl + ", actividadEconomica="
				+ actividadEconomica + ", leyendaInferior1=" + leyendaInferior1
				+ ", leyendaInferior2=" + leyendaInferior2
				+ ", tipoDosificacion=" + tipoDosificacion + ", activo="
				+ activo + ", sucursal=" + sucursal + ", estado=" + estado
				+ ", fechaRegistro=" + fechaRegistro + ", fechaModificacion="
				+ fechaModificacion + ", usuarioRegistro=" + usuarioRegistro
				+ "]";
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
			if(!(obj instanceof Dosificacion)){
				return false;
			}else{
				if(((Dosificacion)obj).id==this.id){
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
	
	public String getNumeroTramite() {
		return numeroTramite;
	}

	public void setNumeroTramite(String numeroTramite) {
		this.numeroTramite = numeroTramite;
	}

	public String getNumeroAutorizacion() {
		return numeroAutorizacion;
	}

	public void setNumeroAutorizacion(String numeroAutorizacion) {
		this.numeroAutorizacion = numeroAutorizacion;
	}

	public Integer getCantidadDosificacion() {
		return cantidadDosificacion;
	}

	public void setCantidadDosificacion(Integer cantidadDosificacion) {
		this.cantidadDosificacion = cantidadDosificacion;
	}

	public Integer getNumeroInicial() {
		return numeroInicial;
	}

	public void setNumeroInicial(Integer numeroInicial) {
		this.numeroInicial = numeroInicial;
	}

	public Integer getNumeroSecuencia() {
		return numeroSecuencia;
	}

	public void setNumeroSecuencia(Integer numeroSecuencia) {
		this.numeroSecuencia = numeroSecuencia;
	}

	public String getNormaAplicada() {
		return normaAplicada;
	}

	public void setNormaAplicada(String normaAplicada) {
		this.normaAplicada = normaAplicada;
	}

	public Date getFechaLimiteEmision() {
		return fechaLimiteEmision;
	}

	public void setFechaLimiteEmision(Date fechaLimiteEmision) {
		this.fechaLimiteEmision = fechaLimiteEmision;
	}

	public String getLlaveControl() {
		return llaveControl;
	}

	public void setLlaveControl(String llaveControl) {
		this.llaveControl = llaveControl;
	}

	public String getActividadEconomica() {
		return actividadEconomica;
	}

	public void setActividadEconomica(String actividadEconomica) {
		this.actividadEconomica = actividadEconomica;
	}

	public String getLeyendaInferior1() {
		return leyendaInferior1;
	}

	public void setLeyendaInferior1(String leyendaInferior1) {
		this.leyendaInferior1 = leyendaInferior1;
	}

	public String getLeyendaInferior2() {
		return leyendaInferior2;
	}

	public void setLeyendaInferior2(String leyendaInferior2) {
		this.leyendaInferior2 = leyendaInferior2;
	}

	public String getTipoDosificacion() {
		return tipoDosificacion;
	}

	public void setTipoDosificacion(String tipoDosificacion) {
		this.tipoDosificacion = tipoDosificacion;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
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

	public String getUsuarioRegistro() {
		return usuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		this.usuarioRegistro = usuarioRegistro;
	}

}


