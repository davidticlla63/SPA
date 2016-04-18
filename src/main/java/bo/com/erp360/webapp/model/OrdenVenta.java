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
@Table(name = "orden_venta", schema = "public")
public class OrdenVenta implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String codigo;
	private String nombre;

	private double total = 0;

	@Column(name = "descripcion", nullable = true)
	private String descripcion;

	@Size(max = 2)
	// AC , IN
	private String estado;
	
	@Column(name = "numero_secuencia", nullable = true)
	private int numeroSecuencia=0;
	
	@Column(name = "numero_orden", nullable = true)
	private int numeroOrden=0;
	
	@JoinColumn(name = "tipo_transaccion", nullable = true)
	private String tipoTansaccion;

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "id_cliente", nullable = true)
	private Cliente cliente;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_empresa", nullable = false)
	private Empresa empresa;
	
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "id_sucursal", nullable = false)
	private Sucursal sucursal;

	@Column(name = "fecha_registro", nullable = false)
	private Date fechaRegistro;

	@Column(name = "Usuario_registro", nullable = false)
	private String UsuarioRegistro;

	public OrdenVenta() {
		super();
		this.id = 0;
		this.estado = "AC";
		this.nombre = "";
		this.descripcion = "";
		this.empresa = new Empresa();
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
			if (!(obj instanceof OrdenVenta)) {
				return false;
			} else {
				if (((OrdenVenta) obj).id == this.id) {
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

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public String getTipoTansaccion() {
		return tipoTansaccion;
	}

	public void setTipoTansaccion(String tipoTansaccion) {
		this.tipoTansaccion = tipoTansaccion;
	}

	public int getNumeroSecuencia() {
		return numeroSecuencia;
	}

	public void setNumeroSecuencia(int numeroSecuencia) {
		this.numeroSecuencia = numeroSecuencia;
	}

	public int getNumeroOrden() {
		return numeroOrden;
	}

	public void setNumeroOrden(int numeroOrden) {
		this.numeroOrden = numeroOrden;
	}

}
