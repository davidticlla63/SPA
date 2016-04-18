package bo.com.erp360.webapp.util;

import bo.com.erp360.webapp.model.PlanCuenta;

public class EDPlanCuenta {
	
	private Integer id;
	private String codigo;
	private String cuenta;
	private String clase;
	private String moneda;
	
	private PlanCuenta pc;
	
	
	public EDPlanCuenta() {
		super();
	}
	
	public EDPlanCuenta(Integer id, String codigo, String cuenta, String clase,String moneda,PlanCuenta pc) {
		super();
		this.id = id;
		this.codigo = codigo;
		this.cuenta = cuenta;
		this.clase = clase;
		this.moneda = moneda;
		this.pc = pc;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getCuenta() {
		return cuenta;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	@Override
	public String toString() {
		return codigo ;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public PlanCuenta getPc() {
		return pc;
	}

	public void setPc(PlanCuenta pc) {
		this.pc = pc;
	}


	
}
