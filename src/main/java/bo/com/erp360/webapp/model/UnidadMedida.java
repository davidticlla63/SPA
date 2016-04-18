package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.*;
import javax.validation.constraints.Size;

import bo.com.erp360.webapp.model.UnidadMedida;
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
@Table(name = "unidad_medida", catalog = "public")
public class UnidadMedida extends Validator implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String nombre;

	private String sigla;

	@Size(max = 2)
	// AC , IN
	private String state;

	@Column(name = "fecha_registro", nullable = false)
	private Date fechaRegistro;

	@Column(name = "fecha_modificacion", nullable = true)
	private Date fechaModificacion;

	@Column(name = "usuario_registro", nullable = false)
	private String usuarioRegistro;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;

	public UnidadMedida() {
		super();
		this.id = 0;
		this.nombre = "";
		this.state = "AC";
	}

	@Override
	public String toString() {
		return sigla;
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
			if (!(obj instanceof UnidadMedida)) {
				return false;
			} else {
				if (((UnidadMedida) obj).id == this.id) {
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

	public boolean validate(FacesContext facesContext, Empresa empresa,
			Gestion gestion) {
		if (isEmppty(this.nombre)) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"El campo nombre no puede ser vacío!", "" + nombre);
			facesContext.addMessage(null, m);
			return false;
		}

		if (exist("Usuario", "nombre", this.nombre)) {
			FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"nombre Usuario ya existe!", nombre);
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

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getSigla() {
		return sigla;
	}

	public void setSigla(String sigla) {
		this.sigla = sigla;
	}

}
