package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.*;
import javax.validation.constraints.Size;

import bo.com.erp360.webapp.validator.Validator;

/**
 * Class Servicio
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "servicio", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = {"nombre","id_empresa","id_tipo_servicio"}))
public class Servicio extends Validator implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private String nombre;
	
	@Column(name="precio_referencial",nullable=true )
	private double precioReferencial;
	
	@Size(max = 2) //AC , IN
	private String estado;
	
	private double comision=50;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="id_empresa", nullable=false)
	private Empresa empresa;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
    @JoinColumn(name="id_tipo_servicio", nullable=true)
	private TipoServicio tipoServicio;
	
	@Column(name="fecha_registro",nullable=false )
	private Date fechaRegistro;
	
	@Column(name="usuario_registro",nullable=false )
	private String usuarioRegistro;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_cuenta",nullable=true )
	private PlanCuenta cuenta;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_centro_costo",nullable=true )
	private CentroCosto centroCosto;

	
	
	public Servicio() {
		this.nombre = "";
		this.precioReferencial = 0;
		this.cuenta = new PlanCuenta();
		this.centroCosto = new CentroCosto();
		this.comision=50;
	}

	@Override
	public String toString() {
		return nombre ;
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
			if(!(obj instanceof Servicio)){
				return false;
			}else{
				if(((Servicio)obj).id==this.id){
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

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public double getPrecioReferencial() {
		return precioReferencial;
	}

	public void setPrecioReferencial(double precioReferencial) {
		this.precioReferencial = precioReferencial;
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

	public PlanCuenta getCuenta() {
		return cuenta;
	}

	public void setCuenta(PlanCuenta cuenta) {
		this.cuenta = cuenta;
	}

	public CentroCosto getCentroCosto() {
		return centroCosto;
	}

	public void setCentroCosto(CentroCosto centroCosto) {
		this.centroCosto = centroCosto;
	}
	
	public boolean validate(FacesContext facesContext,Empresa empresa , Gestion gestion){
		if(isEmppty(this.nombre)){
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"El campo nombre no puede ser vacío!","");
			facesContext.addMessage(null, m);
			return false;
		}
		if(existByEmpresa("Servicio", "nombre", this.nombre,empresa)){
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"nombre Servicio ya existe!", nombre);
			facesContext.addMessage(null, m);
			return false;
		}
		return true;
	}

	public double getComision() {
		return comision;
	}

	public void setComision(double comision) {
		this.comision = comision;
	}

	public TipoServicio getTipoServicio() {
		return tipoServicio;
	}

	public void setTipoServicio(TipoServicio tipoServicio) {
		this.tipoServicio = tipoServicio;
	}

}


