package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.Size;

import bo.com.erp360.webapp.validator.Validator;

/**
 * Class Egreso
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@Table(name = "egreso", catalog = "public")
@SuppressWarnings("serial")
public class Egreso extends Validator implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private String codigo;
	
	@Column(name="nro_documento",nullable=true )
	private String nroDocumento;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_plan_cuenta_bancaria", nullable=true)
	private PlanCuentaBancaria planCuentaBancaria;
	
	@ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="id_comprobante", nullable=false)
	private Comprobante comprobante;
	
	@Size(max = 2) //AC , IN
	private String estado;
	
	private Date fechaRegistro;
	
	@Column(name="fecha_modificacion",nullable=true )
	private Date fechaModificacion;
	
	@Column(name="usuario_registro",nullable=false )
	private String usuarioRegistro;

	public Egreso() {
		super();
		this.id = 0;
		this.codigo= "";
	}

	@Override
	public String toString() {
		return codigo;
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
			if(!(obj instanceof Egreso)){
				return false;
			}else{
				if(((Egreso)obj).id==this.id){
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
	
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
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

	public Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public String getNroDocumento() {
		return nroDocumento;
	}

	public void setNroDocumento(String nroDocumento) {
		this.nroDocumento = nroDocumento;
	}

	public PlanCuentaBancaria getPlanCuentaBancaria() {
		return planCuentaBancaria;
	}

	public void setPlanCuentaBancaria(PlanCuentaBancaria planCuentaBancaria) {
		this.planCuentaBancaria = planCuentaBancaria;
	}

	public Comprobante getComprobante() {
		return comprobante;
	}

	public void setComprobante(Comprobante comprobante) {
		this.comprobante = comprobante;
	}

}


