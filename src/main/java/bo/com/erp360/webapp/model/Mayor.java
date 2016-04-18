package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.*;
import javax.validation.constraints.Size;

import bo.com.erp360.webapp.validator.Validator;

/**
 * Class Mayor
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "mayor", schema = "public")
public class Mayor extends Validator implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private String nombre;
	
	@Temporal(TemporalType.DATE)
	@Column(name="fecha",nullable=true)
	private Date fecha;
	
	@Column(name="debito_nacional",nullable=true )
	private double debitoNacional;
	
	@Column(name="credito_nacional",nullable=true )
	private double creditoNacional;
	
	@Column(name="debito_extranjero",nullable=true )
	private double debitoExtranjero;
	
	@Column(name="credito_extranjero",nullable=true )
	private double creditoExtranjero;
	
	@Column(name="saldo_nacional",nullable=true )
	private double saldoNacional;
	
	@Column(name="saldo_extranjero",nullable=true )
	private double saldoExtranjero;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_plan_cuenta",nullable=false )
	private PlanCuenta planCuenta;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_asiento_contable",nullable=false )
	private AsientoContable asientoContable;
	
	@Size(max = 2) //AC , IN
	private String estado;
	
	@Column(name="fecha_registro",nullable=false )
	private Date fechaRegistro;
	
	@Column(name="fecha_modificacion",nullable=true )
	private Date fechaModificacion;
	
	@Column(name="usuario_registro",nullable=false )
	private String usuarioRegistro;

	public Mayor() {
		super();
		this.id = 0;
		this.nombre= "";
	}

	@Override
	public String toString() {
		return nombre;
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
			if(!(obj instanceof Mayor)){
				return false;
			}else{
				if(((Mayor)obj).id==this.id){
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

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
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
	
	public boolean validate(FacesContext facesContext,Empresa empresa , Gestion gestion){
		if(isEmppty(this.nombre)){
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"El campo nombre no puede ser vacío!",""+nombre);
			facesContext.addMessage(null, m);
			return false;
		}
		return true;
	}

	public Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public AsientoContable getAsientoContable() {
		return asientoContable;
	}

	public void setAsientoContable(AsientoContable asientoContable) {
		this.asientoContable = asientoContable;
	}

	public double getDebitoNacional() {
		return debitoNacional;
	}

	public void setDebitoNacional(double debitoNacional) {
		this.debitoNacional = debitoNacional;
	}

	public double getCreditoNacional() {
		return creditoNacional;
	}

	public void setCreditoNacional(double creditoNacional) {
		this.creditoNacional = creditoNacional;
	}

	public double getDebitoExtranjero() {
		return debitoExtranjero;
	}

	public void setDebitoExtranjero(double debitoExtranjero) {
		this.debitoExtranjero = debitoExtranjero;
	}

	public double getCreditoExtranjero() {
		return creditoExtranjero;
	}

	public void setCreditoExtranjero(double creditoExtranjero) {
		this.creditoExtranjero = creditoExtranjero;
	}

	public double getSaldoNacional() {
		return saldoNacional;
	}

	public void setSaldoNacional(double saldoNacional) {
		this.saldoNacional = saldoNacional;
	}

	public double getSaldoExtranjero() {
		return saldoExtranjero;
	}

	public void setSaldoExtranjero(double saldoExtranjero) {
		this.saldoExtranjero = saldoExtranjero;
	}

	public PlanCuenta getPlanCuenta() {
		return planCuenta;
	}

	public void setPlanCuenta(PlanCuenta planCuenta) {
		this.planCuenta = planCuenta;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

}


