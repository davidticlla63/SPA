package bo.com.erp360.webapp.model;
// Generated Jul 30, 2014 2:47:16 PM by Hibernate Tools 3.4.0.CR1

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Class TipoCambioUfv
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "tipo_cambio_ufv", schema = "public",uniqueConstraints = @UniqueConstraint(columnNames = {"fecha_literal","id_empresa"}))
public class TipoCambioUfv implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private Date fecha;
	
	@Column(name="fecha_literal",nullable=true )
	private String fechaLiteral;
	
	private double unidad;
	
	@Size(max = 2) //AC , IN
	private String estado;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="id_empresa", nullable=false)
	private Empresa empresa;

	public TipoCambioUfv() {
		super();
		this.id = 0;
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
			if(!(obj instanceof TipoCambioUfv)){
				return false;
			}else{
				if(((TipoCambioUfv)obj).id==this.id){
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

	public double getUnidad() {
		return unidad;
	}

	public void setUnidad(double unidad) {
		this.unidad = unidad;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getFechaLiteral() {
		return fechaLiteral;
	}

	public void setFechaLiteral(String fechaLiteral) {
		this.fechaLiteral = fechaLiteral;
	}

}


