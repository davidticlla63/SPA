package bo.com.erp360.webapp.util;

import bo.com.erp360.webapp.model.PlanCuenta;

public class EDBalanceGeneral {
	
	private PlanCuenta planCuenta;
	private double totalNacional;
	private double totalExtranjero;
	
	public EDBalanceGeneral(){
		
	}

	public PlanCuenta getPlanCuenta() {
		return planCuenta;
	}

	public void setPlanCuenta(PlanCuenta planCuenta) {
		this.planCuenta = planCuenta;
	}

	public double getTotalNacional() {
		return totalNacional;
	}

	public void setTotalNacional(double totalNacional) {
		this.totalNacional = totalNacional;
	}

	public double getTotalExtranjero() {
		return totalExtranjero;
	}

	public void setTotalExtranjero(double totalExtranjero) {
		this.totalExtranjero = totalExtranjero;
	}

}
