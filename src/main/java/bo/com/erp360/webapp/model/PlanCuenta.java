package bo.com.erp360.webapp.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Class PlanCuenta
 * @author David.Ticlla.Felipe
 * @version v1.0
 * 
 */
@Entity
@SuppressWarnings("serial")
@Table(name = "plan_cuenta", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = {"codigo_auxiliar","id_empresa"}))
public class PlanCuenta implements Serializable {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	private String codigo;
	
	private String descripcion;
	private String clase;
	
	@Column(name="codigo_auxiliar",nullable=true )
	private String codigoAuxiliar;
	
	@Column(name="ufv",nullable=true )
	private String ufv;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_tipo_cuenta",nullable=true )
	private TipoCuenta tipoCuenta;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_plan_cuenta_padre",nullable=true )
	private PlanCuenta planCuentaPadre;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_moneda_empresa",nullable=true )
	private MonedaEmpresa monedaEmpresa;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_empresa",nullable=true )
	private Empresa empresa;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="id_nivel",nullable=false )
	private Nivel nivel;

	private Date fecha;
	
	@Size(max = 2) //AC , IN
	private String estado;
	
	@Column(name="usuario_registro",nullable=false )
	private String usuarioRegistro;

	public PlanCuenta() {
		super();
		this.id = 0;
	}
	
	@Override
	public String toString() {
		return descripcion;   //codigoAuxiliar+" | "+descripcion;
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
			if(!(obj instanceof PlanCuenta)){
				return false;
			}else{
				if(((PlanCuenta)obj).id==this.id){
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

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public PlanCuenta getPlanCuentaPadre() {
		return planCuentaPadre;
	}

	public void setPlanCuentaPadre(PlanCuenta planCuentaPadre) {
		this.planCuentaPadre = planCuentaPadre;
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

	public String getUsuarioRegistro() {
		return usuarioRegistro;
	}

	public void setUsuarioRegistro(String usuarioRegistro) {
		this.usuarioRegistro = usuarioRegistro;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	public TipoCuenta getTipoCuenta() {
		return tipoCuenta;
	}

	public void setTipoCuenta(TipoCuenta TipoCuenta) {
		this.tipoCuenta = TipoCuenta;
	}

	public Nivel getNivel() {
		return nivel;
	}

	public void setNivel(Nivel nivel) {
		this.nivel = nivel;
	}

	public String getUfv() {
		return ufv;
	}

	public void setUfv(String ufv) {
		this.ufv = ufv;
	}
	
	public MonedaEmpresa getMonedaEmpresa() {
		return monedaEmpresa;
	}

	public void setMonedaEmpresa(MonedaEmpresa monedaEmpresa) {
		this.monedaEmpresa = monedaEmpresa;
	}

	public String getCodigoAuxiliar() {
		return codigoAuxiliar;
	}

	public void setCodigoAuxiliar(String codigoAuxiliar) {
		this.codigoAuxiliar = codigoAuxiliar;
	}

}


