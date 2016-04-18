package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Class Proveedor
 * 
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "configuracion_db", schema = "public")
public class ConfiguracionDB implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "usuario", nullable = false)
	private String user = "spa";
	private String database = "spaDB";
	private String password = "spa";
	private String puerto = "5432";
	private String host = "localhost";

	// DIRECCION DONDE SE HACE LAS COPIAS DE SEGURIDAD
	private String path = "D:\\BACKUP";

	// direccion de bin de POSTGRES
	private String dirBin = "C:\\Program Files\\PostgreSQL\\9.3\\bin";

	@Size(max = 2)
	// AC , IN
	private String estado;

	@Column(name = "fecha_registro", nullable = false)
	private Date fechaRegistro;

	@Column(name = "Usuario_registro", nullable = false)
	private String UsuarioRegistro;

	public ConfiguracionDB() {
		super();
		this.id = 0;
		this.estado = "AC";
	}
	


	@Override
	public String toString() {
		return "ConfiguracionDB [id=" + id + ", user=" + user + ", database="
				+ database + ", password=" + password + ", puerto=" + puerto
				+ ", host=" + host + ", path=" + path + ", dirBin=" + dirBin
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
		if (obj == null) {
			return false;
		} else {
			if (!(obj instanceof ConfiguracionDB)) {
				return false;
			} else {
				if (((ConfiguracionDB) obj).id == this.id) {
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
		return UsuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		UsuarioRegistro = usuarioRegistro;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPuerto() {
		return puerto;
	}

	public void setPuerto(String puerto) {
		this.puerto = puerto;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDirBin() {
		return dirBin;
	}

	public void setDirBin(String dirBin) {
		this.dirBin = dirBin;
	}

}
