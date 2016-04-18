package bo.com.erp360.webapp.util;

import bo.com.erp360.webapp.model.Gestion;
import bo.com.erp360.webapp.model.PlanCuenta;

public class EDSumasSaldos {
	
	private Integer id;
	private PlanCuenta planCuenta;
	private double debe;
	private double haber;
	private double deudor;
	private double acreedor;
	
	public EDSumasSaldos(){
		
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
			if(!(obj instanceof EDSumasSaldos)){
				return false;
			}else{
				if(((EDSumasSaldos)obj).id==this.id){
					return true;
				}else{
					return false;
				}
			}
		}
	}
	
	public Integer getId(){
		return id;
	}
	
	public void setId(Integer id){
		this.id = id;
	}

	public PlanCuenta getPlanCuenta() {
		return planCuenta;
	}

	public void setPlanCuenta(PlanCuenta planCuenta) {
		this.planCuenta = planCuenta;
	}

	public double getDebe() {
		return debe;
	}

	public void setDebe(double debe) {
		this.debe = debe;
	}

	public double getHaber() {
		return haber;
	}

	public void setHaber(double haber) {
		this.haber = haber;
	}

	public double getDeudor() {
		return deudor;
	}

	public void setDeudor(double deudor) {
		this.deudor = deudor;
	}

	public double getAcreedor() {
		return acreedor;
	}

	public void setAcreedor(double acreedor) {
		this.acreedor = acreedor;
	}
	
	
	

}
