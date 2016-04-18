package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.*;
import javax.validation.constraints.Size;

import bo.com.erp360.webapp.model.Empleado;
import bo.com.erp360.webapp.validator.Validator;

/**
 * Class Usuario
 * 
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "empleado", catalog = "public", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
public class Empleado extends Validator implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String nombre;
	private String direccion;
	private String telefono;
	private String ci;
	private String email;

	@Size(max = 2)
	// AC , IN
	private String state;

	@Column(name = "fecha_registro", nullable = false)
	private Date fechaRegistro;

	@Column(name = "fecha_modificacion", nullable = true)
	private Date fechaModificacion;

	@Column(name = "foto_perfil", nullable = true)
	private byte[] fotoPerfil;

	@Column(name = "peso_foto", nullable = true)
	private int pesoFoto;

	@Column(name = "usuario_registro", nullable = false)
	private String usuarioRegistro;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_cargo", nullable = false)
	private Cargo cargo;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;

	public Empleado() {
		super();
		this.id = 0;
		this.nombre = "";
		this.email = " ";
		this.state = "AC";
		this.usuarioRegistro = "";
		this.fechaRegistro = new Date();
		this.cargo = new Cargo();
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
		if (obj == null) {
			return false;
		} else {
			if (!(obj instanceof Empleado)) {
				return false;
			} else {
				if (((Empleado) obj).id == this.id) {
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

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public byte[] getFotoPerfil() {
		return fotoPerfil;
	}

	public void setFotoPerfil(byte[] fotoPerfil) {
		this.fotoPerfil = fotoPerfil;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getUsuarioRegistro() {
		return usuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		this.usuarioRegistro = usuarioRegistro;
	}

	public int getPesoFoto() {
		return pesoFoto;
	}

	public void setPesoFoto(int pesoFoto) {
		this.pesoFoto = pesoFoto;
	}

	public boolean validate(FacesContext facesContext, Empresa empresa,
			Gestion gestion) {
		if (isEmppty(this.nombre)) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"El campo nombre no puede ser vacío!", "" + nombre);
			facesContext.addMessage(null, m);
			return false;
		}
		if (isEmppty(this.email)) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"El campo email no puede ser vacío!", "");
			facesContext.addMessage(null, m);
			return false;
		}

		if (exist("Empleado", "nombre", this.nombre)) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"nombre Usuario ya existe!", nombre);
			facesContext.addMessage(null, m);
			return false;
		}
		if (isValidateEmail(email)) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"email no válido!", "Ej: email@dominio.com");
			facesContext.addMessage(null, m);
			return false;
		}
		return true;
	}

	public boolean validate2(FacesContext facesContext, Empresa empresa,
			Gestion gestion) {
		if (isEmppty(this.nombre)) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"El campo nombre no puede ser vacío!", "" + nombre);
			facesContext.addMessage(null, m);
			return false;
		}

		if (exist("Empleado", "nombre", this.nombre)) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"nombre Usuario ya existe!", nombre);
			facesContext.addMessage(null, m);
			return false;
		}
		if (isEmppty(this.email)) {
			if (isValidateEmail(email)) {
				FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"email no válido!", "Ej: email@dominio.com");
				facesContext.addMessage(null, m);
				return false;
			}
		}
		return true;
	}

	public Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getCi() {
		return ci;
	}

	public void setCi(String ci) {
		this.ci = ci;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

}
