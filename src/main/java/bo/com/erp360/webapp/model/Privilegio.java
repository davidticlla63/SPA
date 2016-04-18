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
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Class Privilegio
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name="privilegio", catalog="public")
@NamedQuery(name = Privilegio.ALL, query = "SELECT r FROM Privilegio r")
public class Privilegio implements java.io.Serializable { //extends BaseEntity

	public final static String ALL = "Privilegio.findAll";	

	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique=true, nullable=false)
	private Integer id;

	private String lectura;
	private String escritura;

	@Size(max = 2) //AC , IN
	private String estado;
	
	@Column(name="usuario_registro",nullable=false )
	private String usuarioRegistro;
	
	@Column(name="fecha_registro",nullable=false )
	private Date fechaRegistro; 

	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="id_permiso", nullable=false)
	private Permiso permiso;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="id_roles", nullable=false)
	private Roles roles;

	public Privilegio() {
		super();
		this.id = 0;
		lectura = "IN";
		escritura = "IN";
		permiso = new Permiso();
		roles = new Roles();
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			if(((Privilegio)obj).id==this.id){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLectura() {
		return this.lectura;
	}

	public void setLectura(String lectura) {
		this.lectura = lectura;
	}

	public String getEscritura() {
		return escritura;
	}

	public void setEscritura(String escritura) {
		this.escritura = escritura;
	}

	public Permiso getPermiso() {
		return permiso;
	}

	public void setPermiso(Permiso permiso) {
		this.permiso = permiso;
	}

	public Roles getRoles() {
		return roles;
	}

	public void setRoles(Roles roles) {
		this.roles = roles;
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

}


