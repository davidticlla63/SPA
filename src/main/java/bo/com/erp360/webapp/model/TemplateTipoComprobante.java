package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Class TemplateTipoComprobante
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "template_tipo_comprobante", schema = "public")
public class TemplateTipoComprobante implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private String tipo; //DEBE - HABER
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="id_tipo_comprobante", nullable=false)
	private TipoComprobante tipoComprobante;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="id_plan_cuenta", nullable=false)
	private PlanCuenta planCuenta;
	
	@Size(max = 2) //AC , IN
	private String estado;
	
	@Column(name="usuario_registro",nullable = false )
	private String usuarioRegistro;
	
	@Column(name="fecha_registro",nullable = false )
	private Date fechaRegistro;
	
	@Column(name="fecha_modificacion",nullable=true )
	private Date fechaModificacion;

	public TemplateTipoComprobante() {
		super();
		this.id = 0 ;
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
			if(!(obj instanceof TemplateTipoComprobante)){
				return false;
			}else{
				if(((TemplateTipoComprobante)obj).id==this.id){
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
	
	public PlanCuenta getPlanCuenta() {
		return planCuenta;
	}

	public void setPlanCuenta(PlanCuenta planCuenta) {
		this.planCuenta = planCuenta;
	}

	public TipoComprobante getTipoComprobante() {
		return tipoComprobante;
	}

	public void setTipoComprobante(TipoComprobante tipoComprobante) {
		this.tipoComprobante = tipoComprobante;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
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

	public Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

}


